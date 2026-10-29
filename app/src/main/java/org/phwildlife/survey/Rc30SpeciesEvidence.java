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
 * RC30 reviewed additions apply to read-only copies of RC18 taxon accounts.
 * Never edits original scientific assets or saved field survey records.
 * Distribution records represent published locations, not live GPS occurrences.
 */
public final class Rc30SpeciesEvidence {
    private Rc30SpeciesEvidence(){}
    private static final String FILE="rc30_reviewed_priority_evidence_v1.json";
    private static final String[] TOPICS={"Identification","Habitat","Distribution",
        "Natural history","Conservation"};

    public static JSONObject merge(Activity activity,JSONObject corpus) {
        if(corpus==null||corpus.optInt("prioritySpeciesCount")!=96||
                corpus.optBoolean("rc30Merged"))return corpus;
        try(InputStream in=activity.getAssets().open(FILE);
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] block=new byte[4096];int n;
            while((n=in.read(block))!=-1){
                if(out.size()+n>48000)throw new IllegalStateException("Review patch oversized");
                out.write(block,0,n);
            }
            JSONObject addition=new JSONObject(out.toString("UTF-8"));
            JSONArray records=addition.getJSONArray("records");
            if(records.length()!=addition.getInt("recordCount")||
                    records.length()!=8)
                throw new IllegalStateException("Reviewed reference pack incomplete");
            JSONArray accounts=corpus.getJSONArray("accounts");
            if(accounts.length()!=96)
                throw new IllegalStateException("Unexpected original species index");
            Map<String,JSONObject> byName=new HashMap<>();
            for(int i=0;i<accounts.length();i++){
                JSONObject account=accounts.getJSONObject(i);
                byName.put(account.getString("scientificName"),account);
            }
            HashSet<String> known=new HashSet<>();
            int applied=0;
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
                    record.getString("sourceLocation").trim().isEmpty()||
                    record.getString("studyScope").trim().isEmpty())
                    throw new IllegalStateException("Incomplete science provenance");
                JSONObject sections=account.getJSONObject("sections");
                JSONArray target=sections.getJSONArray(topic);
                String key=name+"|"+topic+"|"+record.getString("text");
                if(!known.add(key))throw new IllegalStateException("Duplicate science patch");
                boolean duplicate=false;
                for(int k=0;k<target.length();k++){
                    JSONObject original=target.getJSONObject(k);
                    if(record.getString("text").equals(original.optString("text"))&&
                        record.getString("url").equals(original.optString("url"))){
                        duplicate=true;break;
                    }
                }
                if(!duplicate){
                    target.put(new JSONObject(record.toString()));
                    applied++;
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
            corpus.put("sectionMissingCounts",gaps);
            corpus.put("sourceFactCount",corpus.optInt("sourceFactCount")+applied);
            corpus.put("rc30Merged",true);
            corpus.put("rc30ReviewedAdditions",applied);
        }catch(Exception problem){
            // Always keep the core offline library usable if this optional pack fails.
            // An incomplete pack cannot introduce partially cited records.
            return corpus;
        }
        return corpus;
    }
}
