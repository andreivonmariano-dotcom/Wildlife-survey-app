package org.phwildlife.survey;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Living Atlas v1: read-only bridge over the already verified offline Book 01
 * and habitat reference assets. No user database tables, reference imports,
 * network requests, or name-based survey-record rewrites.
 *
 * A book01@edition identifier is an EDITION-SCOPED lookup token. It is not an
 * accepted scientific name, a durable taxon UUID, or an automatic crosswalk
 * to field_records.taxon_key. Revisions require an explicit reviewed crosswalk.
 */
public final class LivingAtlasDataLayer {
    public static final String INDEX_EDITION = "living-atlas-data-v1";
    public static final String BOOK_EDITION = "1.82";
    public static final String REGION_ID = "greater-mindanao";
    private static final String REGISTRY = "living_atlas_sources_v1.json";
    private static final String HABITATS = "offline_habitat_profiles_v1.json";
    private static final String EDGES = "rc8_species_habitat_links_v1.json";
    private static Snapshot cached;

    private LivingAtlasDataLayer() {}

    private static String normalized(String value) {
        return value == null ? "" :
            value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public static String editionScopedId(String scientificName) {
        String value = normalized(scientificName);
        if (value.isEmpty()) return "";
        return "book01@" + BOOK_EDITION + ":" + value.replace(' ', '_');
    }

    /**
     * Only a fully approved scientific AND habitat crosswalk may become an
     * Ecological Threads edge. Site-occurrence data is never an atlas edge.
     */
    public static boolean approvedHabitatLink(String packReview,
            String scientificReview, String habitatReview, boolean siteOccurrence) {
        return !siteOccurrence && "approved".equals(normalized(packReview))
            && "approved".equals(normalized(scientificReview))
            && "approved".equals(normalized(habitatReview));
    }

    public static final class Taxon {
        public final String editionScopedId;
        public final String scientificName;
        public final String commonName;
        public final String group;
        public final String order;
        public final String family;
        private final String searchText;
        private Taxon(JSONObject account) {
            scientificName = account.optString("s");
            editionScopedId = LivingAtlasDataLayer.editionScopedId(scientificName);
            commonName = account.optString("c");
            group = account.optString("g");
            order = account.optString("o");
            family = account.optString("f");
            searchText = normalized(scientificName + " " + commonName + " " +
                group + " " + order + " " + family);
        }
        public String sourceEdition() { return BOOK_EDITION; }
    }

    public static final class Habitat {
        public final String id;
        public final String name;
        private Habitat(String id, String name) { this.id=id;this.name=name; }
    }

    public static final class HabitatEdge {
        public final String id;
        public final String scientificName;
        public final String habitatId;
        public final String evidenceType;
        public final String evidenceText;
        public final String sourceTitle;
        public final String sourceUrl;
        private HabitatEdge(JSONObject record) {
            id = record.optString("id");
            scientificName = record.optString("scientificName");
            habitatId = record.optString("habitatId");
            evidenceType = record.optString("evidenceType");
            evidenceText = record.optString("evidenceText");
            sourceTitle = record.optString("sourceTitle");
            sourceUrl = record.optString("sourceUrl");
        }
    }

    public static final class Snapshot {
        public final String edition = INDEX_EDITION;
        public final String regionId = REGION_ID;
        public final String bookEdition = BOOK_EDITION;
        public final int bibliographyCount;
        public final int pendingHabitatEdgeCount;
        private final List<Taxon> taxa;
        private final List<Habitat> habitats;
        private final Map<String,Taxon> byName;
        private final Map<String,List<HabitatEdge>> approvedEdges;

        private Snapshot(List<Taxon> taxa, List<Habitat> habitats,
                Map<String,Taxon> byName,
                Map<String,List<HabitatEdge>> edges,
                int pendingHabitatEdgeCount, int bibliographyCount) {
            this.taxa = Collections.unmodifiableList(new ArrayList<>(taxa));
            this.habitats = Collections.unmodifiableList(new ArrayList<>(habitats));
            this.byName = Collections.unmodifiableMap(new HashMap<>(byName));
            Map<String,List<HabitatEdge>> frozen = new HashMap<>();
            for (Map.Entry<String,List<HabitatEdge>> item : edges.entrySet())
                frozen.put(item.getKey(),
                    Collections.unmodifiableList(new ArrayList<>(item.getValue())));
            approvedEdges = Collections.unmodifiableMap(frozen);
            this.pendingHabitatEdgeCount = pendingHabitatEdgeCount;
            this.bibliographyCount = bibliographyCount;
        }

        public List<Taxon> taxa() { return taxa; }
        public List<Habitat> habitats() { return habitats; }
        public Taxon findScientificName(String scientificName) {
            return byName.get(normalized(scientificName));
        }
        public List<HabitatEdge> approvedHabitatEdges(String scientificName) {
            List<HabitatEdge> result = approvedEdges.get(normalized(scientificName));
            return result == null ? Collections.emptyList() : result;
        }
        public List<Taxon> search(String query, int limit) {
            String q = normalized(query);
            if (q.isEmpty() || limit <= 0) return Collections.emptyList();
            ArrayList<Taxon> exact = new ArrayList<>();
            ArrayList<Taxon> starts = new ArrayList<>();
            ArrayList<Taxon> other = new ArrayList<>();
            for (Taxon taxon : taxa) {
                String latin = normalized(taxon.scientificName);
                String common = normalized(taxon.commonName);
                if (latin.equals(q) || common.equals(q)) exact.add(taxon);
                else if (latin.startsWith(q) || common.startsWith(q)) starts.add(taxon);
                else if (taxon.searchText.contains(q)) other.add(taxon);
            }
            ArrayList<Taxon> result = new ArrayList<>();
            for (List<Taxon> group : new List[]{exact, starts, other}) {
                for (Taxon taxon : group) {
                    result.add(taxon);
                    if (result.size() >= limit)
                        return Collections.unmodifiableList(result);
                }
            }
            return Collections.unmodifiableList(result);
        }
    }

    private static JSONObject readAsset(Context context, String filename, int limit)
            throws Exception {
        try (InputStream in = context.getAssets().open(filename);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[8192];
            int n;
            while ((n = in.read(bytes)) != -1) {
                if (output.size() + n > limit)
                    throw new IllegalStateException("Atlas reference asset exceeds limit");
                output.write(bytes, 0, n);
            }
            return new JSONObject(new String(output.toByteArray(),
                StandardCharsets.UTF_8));
        }
    }

    public static synchronized Snapshot open(Context context) {
        if (cached != null) return cached;
        if (!PhLibraryBook01.ready(context))
            throw new IllegalStateException("Verified offline Book 01 is unavailable");

        try {
            JSONObject registry = readAsset(context, REGISTRY, 12000);
            if (registry.getInt("schemaVersion") != 1 ||
                    !INDEX_EDITION.equals(registry.getString("indexEdition")) ||
                    !REGION_ID.equals(registry.getString("regionId")))
                throw new IllegalStateException("Living Atlas source registry edition mismatch");
            JSONObject compiled = registry.getJSONObject("compiledReference");
            if (!BOOK_EDITION.equals(compiled.getString("bookEdition")) ||
                    compiled.getInt("speciesCount") != PhLibraryBook01.EXPECTED_COUNT ||
                    compiled.getInt("bibliographyCount") != 1159)
                throw new IllegalStateException("Book 01 registry does not match the pinned reference");

            List<JSONObject> accounts = PhLibraryBook01.entries(context);
            List<Taxon> taxa = new ArrayList<>();
            Map<String,Taxon> byName = new LinkedHashMap<>();
            for (JSONObject account : accounts) {
                Taxon taxon = new Taxon(account);
                if (taxon.scientificName.isEmpty() ||
                        byName.put(normalized(taxon.scientificName), taxon) != null)
                    throw new IllegalStateException("Duplicate or empty source taxon");
                taxa.add(taxon);
            }
            if (taxa.size() != 715)
                throw new IllegalStateException("Book 01 has an unexpected size");

            JSONArray bib = PhLibraryBook01.bibliography(context);
            if (bib.length() != 1159)
                throw new IllegalStateException("Pinned Book 01 bibliography is unavailable");

            JSONObject habitatsRoot = readAsset(context, HABITATS, 100000);
            JSONArray profiles = habitatsRoot.getJSONArray("profiles");
            List<Habitat> habitats = new ArrayList<>();
            Set<String> habitatIds = new HashSet<>();
            for (int i=0; i<profiles.length(); i++) {
                JSONObject profile = profiles.getJSONObject(i);
                String id = profile.getString("id"), name = profile.getString("name");
                if (id.isEmpty() || name.isEmpty() || !habitatIds.add(id))
                    throw new IllegalStateException("Invalid habitat identifier");
                habitats.add(new Habitat(id,name));
            }

            JSONObject linkPack = readAsset(context, EDGES, 150000);
            String packReview = linkPack.optString("reviewStatus");
            JSONArray edges = linkPack.getJSONArray("edges");
            Set<String> edgeIds = new HashSet<>();
            Map<String,List<HabitatEdge>> approved = new HashMap<>();
            int pending = 0;
            for (int i=0; i<edges.length(); i++) {
                JSONObject item = edges.getJSONObject(i);
                String id = item.optString("id");
                if (id.isEmpty() || !edgeIds.add(id))
                    throw new IllegalStateException("Duplicate habitat evidence ID");
                String scientific = normalized(item.optString("scientificName"));
                String habitatId = item.optString("habitatId");
                if (!habitatIds.contains(habitatId))
                    throw new IllegalStateException("Unrecognized source habitat");
                boolean approvedLink = approvedHabitatLink(
                    packReview, item.optString("scientificReview"),
                    item.optString("habitatMappingReview"),
                    item.optBoolean("siteOccurrence",false));
                if (approvedLink && byName.containsKey(scientific)) {
                    approved.computeIfAbsent(scientific,k->new ArrayList<>())
                        .add(new HabitatEdge(item));
                } else {
                    pending++;
                }
            }
            Snapshot result = new Snapshot(taxa, habitats, byName,
                approved, pending, bib.length());
            cached = result;
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(
                "Living Atlas source data could not be validated",e);
        }
    }
}
