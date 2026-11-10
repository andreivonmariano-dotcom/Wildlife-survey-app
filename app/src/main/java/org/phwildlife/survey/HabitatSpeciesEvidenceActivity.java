package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

/** Offline habitat reader. Original evidence pack and links remain unchanged. */
public final class HabitatSpeciesEvidenceActivity extends Activity {
    private final ArrayList<JSONObject> habitats=new ArrayList<>();
    private JSONObject articles;
    private String currentHabitat=null;
    private ScrollView pageScroll;
    private int indexScrollY=0,detailScrollY=0;

    private int dp(float n){return PhLibrary2Ui.dp(this,n);}
    private static JSONObject read(Activity a,String file)throws Exception{
        try(InputStream in=a.getAssets().open(file);
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];int n;
            while((n=in.read(buffer))!=-1){
                if(out.size()+n>250000)
                    throw new IllegalStateException("Habitat data exceeds edition limit");
                out.write(buffer,0,n);
            }
            return new JSONObject(out.toString("UTF-8"));
        }
    }
    private LinearLayout shell(String title,Runnable onBack){
        getWindow().setStatusBarColor(PhLibrary2Ui.PAPER);
        getWindow().setNavigationBarColor(PhLibrary2Ui.PAPER);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PhLibrary2Ui.PAPER);
        root.setPadding(dp(15),dp(7),dp(15),0);
        LinearLayout nav=new LinearLayout(this);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        boolean large=getResources().getConfiguration().fontScale>=1.6f;
        TextView back=PhLibrary2Ui.action(this,"Back",onBack,false);
        if(large){
            back.setTextSize(12);
            back.setPadding(dp(6),dp(5),dp(6),dp(5));
        }
        nav.addView(back,new LinearLayout.LayoutParams(dp(large?99:80),dp(48)));
        TextView label=PhLibrary2Ui.text(this,title,large?11:14,
            PhLibrary2Ui.FOREST,false,true);
        label.setGravity(Gravity.CENTER);
        nav.addView(label,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(nav);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        pageScroll=scroll;
        LinearLayout body=new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(1),dp(8),dp(1),dp(28));
        scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        return body;
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        try{
            JSONArray profiles=read(this,
                "offline_habitat_profiles_v1.json").getJSONArray("profiles");
            if(profiles.length()!=15)
                throw new IllegalStateException("Unexpected habitat source edition");
            HashSet<String> seen=new HashSet<>();
            for(int i=0;i<profiles.length();i++){
                JSONObject profile=profiles.getJSONObject(i);
                String id=profile.getString("id");
                if(!seen.add(id)||PhLibraryHabitatFilter.indexOfId(id)==0)
                    throw new IllegalStateException("Unknown or duplicated habitat ID");
                habitats.add(profile);
            }
            JSONObject edition=read(this,"phlibrary_habitat_reader_v1.json");
            if(!"rc107-habitat-reader-v1".equals(edition.optString("edition")))
                throw new IllegalStateException("Unrecognized habitat reader edition");
            articles=edition.getJSONObject("profiles");
            if(articles.length()!=15)
                throw new IllegalStateException("Incomplete habitat reader edition");
            for(JSONObject profile:habitats)
                if(articles.optString(profile.optString("id")).trim().isEmpty())
                    throw new IllegalStateException("Missing habitat account");
        }catch(Exception error){
            LinearLayout body=shell("Habitat Atlas",this::finish);
            body.addView(PhLibrary2Ui.text(this,
                "Habitat reference data are unavailable offline.",17,
                PhLibrary2Ui.INK,false,false));
            return;
        }
        if(state!=null){
            indexScrollY=state.getInt("index_scroll",0);
            detailScrollY=state.getInt("detail_scroll",0);
        }
        String requested=state==null?getIntent().getStringExtra("habitat_id"):
            state.getString("habitat");
        if(requested!=null&&!requested.isEmpty())showHabitat(requested);
        else showIndex();
    }
    private void showIndex(){
        currentHabitat=null;
        LinearLayout body=shell("Habitat Atlas",this::finish);
        for(JSONObject h:habitats){
            String id=h.optString("id");
            LinearLayout card=PhLibrary2Ui.panel(this,
                h.optString("name"),"",()->showHabitat(id));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.bottomMargin=dp(8);
            body.addView(card,lp);
        }
        if(indexScrollY>0&&pageScroll!=null)
            pageScroll.post(()->pageScroll.scrollTo(0,indexScrollY));
    }
    private void showHabitat(String id){
        JSONObject profile=null;
        for(JSONObject habitat:habitats)
            if(id.equals(habitat.optString("id"))){profile=habitat;break;}
        if(profile==null){showIndex();return;}
        if(currentHabitat==null&&pageScroll!=null)
            indexScrollY=pageScroll.getScrollY();
        currentHabitat=id;
        LinearLayout body=shell("Habitat Atlas",this::showIndex);
        TextView title=PhLibrary2Ui.text(this,profile.optString("name"),27,
            PhLibrary2Ui.INK,true,false);
        title.setPadding(0,dp(12),0,dp(14));
        body.addView(title);

        // The canonical habitat wording stays intact; only its reading typography
        // changes. Source references are never split or silently rewritten.
        TextView article=PhLibrary2Ui.text(this,articles.optString(id),17,
            PhLibrary2Ui.INK,false,false);
        article.setTextIsSelectable(true);
        article.setLineSpacing(dp(7),1.21f);
        article.setPadding(dp(2),0,dp(2),dp(19));
        article.setContentDescription("Habitat account for "+profile.optString("name")+
            ". "+articles.optString(id));
        body.addView(article);

        // The destination is a *text retrieval* filter, not a claimed species list.
        TextView browse=PhLibrary2Ui.action(this,"Account text matches",()->{
            startActivity(new Intent(this,PhLibraryDirectoryActivity.class)
                .putExtra("phlibrary_habitat_id",id));
        },false);
        browse.setContentDescription("Find source accounts mentioning "+profile.optString("name")+
            ". Search matches do not establish species habitat occupancy.");
        if(getResources().getConfiguration().fontScale>=1.6f){
            browse.setTextSize(12);
            browse.setPadding(dp(8),dp(5),dp(8),dp(5));
        }
        body.addView(browse,new LinearLayout.LayoutParams(-1,dp(52)));
        if(detailScrollY>0 && pageScroll!=null){
            final int offset=detailScrollY;
            pageScroll.post(()->pageScroll.scrollTo(0,offset));
            detailScrollY=0;
        }
    }
    @Override protected void onSaveInstanceState(Bundle out){
        out.putString("habitat",currentHabitat);
        if(currentHabitat==null&&pageScroll!=null)
            indexScrollY=pageScroll.getScrollY();
        else if(pageScroll!=null)detailScrollY=pageScroll.getScrollY();
        out.putInt("index_scroll",indexScrollY);
        out.putInt("detail_scroll",detailScrollY);
        super.onSaveInstanceState(out);
    }
    @Override public void onBackPressed(){
        if(currentHabitat!=null)showIndex();
        else super.onBackPressed();
    }
}
