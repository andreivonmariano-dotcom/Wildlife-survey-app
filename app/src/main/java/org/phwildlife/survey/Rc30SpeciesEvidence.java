package org.phwildlife.survey;

import android.app.Activity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Read-only, transactional union of RC30 and RC31 source-verified accounts.
 * Original immutable RC18 evidence, SQLite field records and survey states never change.
 * Dated IUCN, DENR and historic specimen observations retain independent source scopes.
 */
public final class Rc30SpeciesEvidence {
    private Rc30SpeciesEvidence(){}
    private static final String FILE="rc30_reviewed_priority_evidence_v1.json";
    private static final String RC31="rc31_reviewed_sparse_species_assessments_v1.json";
    private static final String RC32="rc32_sparse_priority_ecology_diagnostics_v1.json";
    private static final String[] TOPICS={"Identification","Habitat","Distribution",
        "Natural history","Conservation"};

    private static JSONArray readPack(Activity a,String file,int expected)throws Exception{
        try(InputStream in=a.getAssets().open(file);
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] block=new byte[4096];int n;
            while((n=in.read(block))!=-1){
                if(out.size()+n>86000)throw new IllegalStateException("Review patch oversized");
                out.write(block,0,n);
            }
            JSONObject addition=new JSONObject(out.toString("UTF-8"));
            if(addition.optInt("prioritySpeciesCount")!=96)
                throw new IllegalStateException("Reviewed registry mismatch");
            JSONArray records=addition.getJSONArray("records");
            if(records.length()!=expected||addition.getInt("recordCount")!=expected)
                throw new IllegalStateException("Reviewed pack count mismatch");
            return records;
        }
    }

    public static JSONObject merge(Activity activity,JSONObject corpus) {
        if(corpus==null||corpus.optInt("prioritySpeciesCount")!=96||
                corpus.optBoolean("rc30Merged"))return corpus;
        try{
            // Read and validate both source packs before making any visible change.
            JSONArray original=readPack(activity,FILE,8);
            JSONArray latest=readPack(activity,RC31,26);
            JSONArray diagnostic=readPack(activity,RC32,15);
            JSONObject updated=new JSONObject(corpus.toString());
            JSONArray accounts=updated.getJSONArray("accounts");
            if(accounts.length()!=96||corpus.optInt("sourceFactCount")!=456)
                throw new IllegalStateException("Unexpected original species corpus");
            Map<String,JSONObject> byName=new HashMap<>();
            for(int i=0;i<accounts.length();i++){
                JSONObject account=accounts.getJSONObject(i);
                byName.put(account.getString("scientificName"),account);
            }
            HashSet<String> known=new HashSet<>();
            int applied=0,rc30Count=0,rc31Count=0,rc32Count=0;
            for(int pack=0;pack<3;pack++){
                JSONArray records=pack==0?original:pack==1?latest:diagnostic;
                for(int i=0;i<records.length();i++){
                    JSONObject record=records.getJSONObject(i);
                    String name=record.getString("scientificName");
                    String topic=record.getString("section");
                    JSONObject account=byName.get(name);
                    if(account==null)throw new IllegalStateException("Unknown reviewed species");
                    boolean allowed=false;
                    for(String s:TOPICS)if(s.equals(topic))allowed=true;
                    if(!allowed||!record.getString("url").startsWith("https://")||
                        record.getString("text").trim().isEmpty()||
                        record.getString("source").trim().isEmpty()||
                        record.getString("sourceType").trim().isEmpty()||
                        record.getString("sourceLocation").trim().isEmpty()||
                        record.getString("studyScope").trim().isEmpty())
                        throw new IllegalStateException("Incomplete science provenance");
                    if("government_national_conservation_record".equals(
                            record.getString("sourceType"))&&
                        !"DENR".equals(record.optString("assessmentAuthority")))
                        throw new IllegalStateException("Ambiguous national assessment");
                    if(record.has("assessmentAuthority")&&
                            !"Conservation".equals(topic))
                        throw new IllegalStateException("Assessment applied to wrong topic");
                    String key=name+"|"+topic+"|"+record.getString("text");
                    if(!known.add(key))
                        throw new IllegalStateException("Duplicate reviewed claim");
                    JSONArray target=account.getJSONObject("sections").getJSONArray(topic);
                    boolean duplicate=false;
                    for(int k=0;k<target.length();k++){
                        JSONObject existing=target.getJSONObject(k);
                        if(record.getString("text").equals(existing.optString("text"))&&
                            record.getString("url").equals(existing.optString("url"))){
                            duplicate=true;break;
                        }
                    }
                    if(!duplicate){
                        target.put(new JSONObject(record.toString()));
                        applied++;
                        if(pack==0)rc30Count++;
                        else if(pack==1)rc31Count++;
                        else rc32Count++;
                    }
                }
            }
            JSONObject gaps=new JSONObject();
            for(String section:TOPICS){
                int missing=0;
                for(int i=0;i<accounts.length();i++)
                    if(accounts.getJSONObject(i).getJSONObject("sections")
                            .getJSONArray(section).length()==0)missing++;
                gaps.put(section,missing);
            }
            updated.put("sectionMissingCounts",gaps);
            updated.put("sourceFactCount",456+applied);
            updated.put("rc30Merged",true);
            updated.put("rc30ReviewedAdditions",rc30Count);
            updated.put("rc31ReviewedAdditions",rc31Count);
            updated.put("rc32ReviewedAdditions",rc32Count);
            return updated;
        }catch(Exception problem){
            // An invalid record never exposes a partially modified research account.
            return corpus;
        }
    }
}
