# APO Living Atlas | Data layer v1 and editable-source workflow
**Working implementation note, 11 November 2026.** This is a technical source record. It does not amend the approved APO governing charter v1.2.

## Sources and ownership

The editable scientific working sources live in the existing owner's Google Drive folder "APO app Data repository" as two Office files:

- **BOOK 1: MINDANAO PAIC.docx**: reviewed Greater Mindanao encyclopedia edition 1.82; 715 styled species headings, supporting accounts and literature citations.
- **MINDANAO PAIC CHECKLIST.xlsx**: "Master Checklist" contains 714 distinct scientific names and structured name, order, family, distribution, IUCN, DENR, habitat and elevational-range fields. Additional "Keep 29", "Omit 11" and "Scope and sources" tabs document editorial inclusion decisions.

These files may be edited using Google Drive's Office document editors. They remain the editable scientific working sources; the Android APK must never depend on Google sign-in or connection to read them. This public GitHub record deliberately does not expose the files' sharing links or clone their full unpublished manuscripts.

The already bundled, SHA-256-pinned **PHLibrary Book 01 v1.82** remains the installed app's reference edition until an explicitly reviewed new edition passes integrity, taxonomy, evidence, permissions and APK upgrade gates. Existing field data remains in SQLite v29. Personal observations do not modify curated reference accounts.

## Versioned runtime interface

- **LivingAtlasDataLayer.java** reads the verified Book 01 entries, verified bibliography, native habitat profiles, and the original habitat evidence list. It requires the bundled \`living_atlas_sources_v1.json\` registry and checks counts/versions. It is a read-only foundation for future universal search and Ecological Threads views.
- Edition-scoped lookup IDs have the form \`book01@1.82:pithecophaga_jefferyi\`. They identify an entry in a **specific reference edition**. They are never treated as an authoritative permanent scientific taxon ID or automatically substituted into \`field_records.taxon_key\`.
- Scientific and geographic claims retain their actual source scope. Habitat relationships can reach public Ecological Threads views only when the **evidence pack, individual scientific review and habitat crosswalk** all say \`approved\`, with \`siteOccurrence=false\`. Pending records remain offline in their archival pack, available for review but excluded from verified relationship results.
- Book account text matches are browsing/search aids, never automatically recorded habitat use or occurrence. Individual field observations remain in SQLite and retain their original identification, location accuracy, effort and evidence.

## Specific source reconciliation needed

Direct comparison of the editable documents found 715 Book 01 species headings and 714 checklist scientific names. Three headings appear in Book 01 only: \`Ceyx mindanensis\`, \`Mops plicatus\` and \`Oreophryne cf. nana\`. Two scientific names appear in the checklist only: \`Chaerephon plicata\` and \`Aphantophryne nana\`. Some are plausible nomenclatural or identification differences, but equivalence is **not an approved assumption**. An editorial crosswalk must record the original name, source date, accepted determination, review decision and effect on source counts. Maintain \`Oreophryne cf. nana\` as a provisional identification with unranked educational rarity and no inherited species-level IUCN status unless a reviewed source change explicitly establishes otherwise.

An older \`phlibrary_book01_manifest.json\` still records edition 1.75 and 714 entries. The current loader uses the SHA-256-verified v1.82 compressed book, so do not infer runtime edition from the archival manifest. The validator uses the pinned v1.82 pack and its bibliography.

## Updating the app when the Google files change

1. Edit the existing Drive Word/Excel sources. Record the revision date and retain editorial rationale, citations and unmodified archival originals.
2. Export the **Master Checklist** tab to CSV. Run \`python3 scripts/check_living_atlas_data.py --checklist-csv /path/to/master_checklist.csv\` to report book-only and checklist-only names. This **compares** names and never writes or remaps them.
3. Resolve taxonomic splits, synonym proposals, provisional identifications, PAIC records, status authorities, proposed habitat edges and publishing/photograph rights with explicit review decisions. Missing records remain missing; no generated replacements.
4. Convert the approved source revision to a new versioned, bundled offline reference edition using a separately tested, edition-aware compiler. Do not directly replace the pinned v1.82 JSON/ZIP or current 715-count validation with an unreviewed Drive export. Keep older source editions available for migrations and auditing.
5. Run the source validator and full Android scientific/UI CI. Check offline reading, link review status, source citations, protected local survey data, installed-app signature and no-clear-data upgrade. The new APK must remain usable in airplane mode.

Run \`python3 scripts/check_living_atlas_data.py\` for the current immutable app-payload audit. The current implementation is an index and validator, **not yet** a DOCX/XLSX auto-publisher, fully approved ecological graph, or new UI. All subsequent user-facing scientific copy must meet APO-09 through APO-12. The charter changes only following separately approved owner wording.

## Release restrictions

The rights audit of photo source and the public private-labelled archive, scientific text editing, and physical-device QA remain open. Do not treat this source-only layer as permission for public release.
