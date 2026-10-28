package org.phwildlife.survey;
import android.app.Activity;
import android.widget.LinearLayout;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

/** Only independently reviewed, photo-hash-matched overlays can render. */
public final class Rc9ApprovedPhotoMarkers {
    private Rc9ApprovedPhotoMarkers(){}
    public static int add(Activity a,LinearLayout parent,JSONObject photo){
        if(photo==null)return 0;
        JSONArray matches=new JSONArray();
        try(InputStream in=a.getAssets().open("rc9_approved_photo_annotations_v1.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[4096];int n;
            while((n=in.read(b))!=-1){
                if(out.size()+n>85000)return 0;
                out.write(b,0,n);
            }
            JSONObject data=new JSONObject(out.toString("UTF-8"));
            if(!"rc9-verified-image-annotation-v1".equals(data.optString("schemaVersion")))return 0;
            JSONArray all=data.getJSONArray("items");
            for(int i=0;i<all.length();i++){
                JSONObject m=all.optJSONObject(i);
                if(m==null)continue;
                if(m.optInt("photoId",-1)!=photo.optInt("photoId",-2))continue;
                if(!"expert_approved".equals(m.optString("reviewStatus")))continue;
                if(!photo.optString("sha256").equals(m.optString("photoSha256")))continue;
                if(m.optString("expertReviewer").isEmpty()||m.optString("approvalReference").isEmpty())continue;
                if(!m.optString("citation").startsWith("https://"))continue;
                double x=m.optDouble("x",-1),y=m.optDouble("y",-1);
                if(x<0||y<0||x>1||y>1)continue;
                matches.put(m);
            }
        }catch(Exception ignored){return 0;}
        if(matches.length()==0)return 0;
        Rc9PhotoMarkerView v=new Rc9PhotoMarkerView(a);
        if(!v.useAsset(photo.optString("offlineAsset")))return 0;
        v.showApproved(matches);
        parent.addView(v,new LinearLayout.LayoutParams(-1,
            Math.round(280*a.getResources().getDisplayMetrics().density)));
        return matches.length();
    }
}
