package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

/** Complete offline original bibliography with virtualized, accessible rows. */
public final class PhLibraryBibliographyActivity extends Activity {
    private final ArrayList<JSONObject> refs=new ArrayList<>();
    private final ArrayList<JSONObject> matches=new ArrayList<>();
    private EditText search;
    private TextView count,savedToggle;
    private boolean savedOnly=false;
    private ListView list;
    private BaseAdapter adapter;
    private String originalCitation="";
    private boolean candidateMode=false;
    private int dp(float v){return PhLibrary2Ui.dp(this,v);}
    private static String lower(String s){
        return s==null?"":s.toLowerCase(Locale.ROOT);
    }
    private static boolean candidate(String citation,String reference){
        // This is discovery assistance only, never an asserted author-year identity match.
        String surname=citation.trim().split("[,;&\\s]+")[0]
            .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9.]", "");
        if(surname.length()<3)return false;
        Matcher m=Pattern.compile("(?:18|19|20)\\d{2}").matcher(citation);
        String full=lower(reference);
        if(m.find())return full.contains(surname)&&full.contains(m.group());
        String withoutDate=citation.replaceAll("(?i),?\\s*n\\.d\\.?\\s*$","").trim();
        return withoutDate.length()>=8&&full.contains(lower(withoutDate));
    }
    private void refresh(){
        if(adapter==null)return;
        matches.clear();
        candidateMode=false;
        String q=search.getText().toString().trim();
        String lowerQuery=lower(q);
        for(JSONObject item:refs){
            String citation=item.optString("text");
            if((q.isEmpty()||lower(citation).contains(lowerQuery))&&
                    (!savedOnly||ApoReferenceBookmarks.isSaved(this,citation)))
                matches.add(item);
        }
        boolean fromCitation=!originalCitation.isEmpty()&&q.equals(originalCitation);
        if(fromCitation&&matches.isEmpty()){
            for(JSONObject item:refs)
                if(candidate(q,item.optString("text"))&&
                    (!savedOnly||ApoReferenceBookmarks.isSaved(this,item.optString("text"))))
                    matches.add(item);
            candidateMode=!matches.isEmpty();
        }
        if(fromCitation){
            count.setText(candidateMode?
                matches.size()+(matches.size()==1?" possible reference":" possible references")+
                    " · Not verified":
                matches.isEmpty()?"No automatic reference match":
                    matches.size()+(matches.size()==1?" text match":" text matches")+
                        " · Not verified");
        }else{
            count.setText(q.isEmpty()?refs.size()+" references":
                matches.isEmpty()?"No matching references":
                    matches.size()+(matches.size()==1?" reference":" references"));
        }
        adapter.notifyDataSetChanged();
        if(savedToggle!=null){
            savedToggle.setText(savedOnly?"All references":"Saved only ("+
                ApoReferenceBookmarks.count(this)+")");
            savedToggle.setContentDescription(savedOnly?
                "Show all reference citations":"Show saved reference citations");
        }
        list.setSelection(0);
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setStatusBarColor(PhLibrary2Ui.PAPER);
        getWindow().setNavigationBarColor(PhLibrary2Ui.PAPER);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        savedOnly=saved!=null&&saved.getBoolean("saved_only",false);
        JSONArray items=PhLibraryBook01.bibliography(this);
        for(int i=0;i<items.length();i++){
            JSONObject ref=items.optJSONObject(i);if(ref!=null)refs.add(ref);
        }
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(7),dp(16),dp(4));root.setBackgroundColor(PhLibrary2Ui.PAPER);
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER_VERTICAL);
        boolean large=getResources().getConfiguration().fontScale>=1.6f;
        TextView back=PhLibrary2Ui.action(this,"Back",this::finish,false);
        if(large){back.setTextSize(12);back.setPadding(dp(6),dp(5),dp(6),dp(5));}
        nav.addView(back,new LinearLayout.LayoutParams(dp(large?98:75),dp(48)));
        TextView navTitle=PhLibrary2Ui.text(this,"References",
            large?12:14,PhLibrary2Ui.FOREST,false,true);
        navTitle.setGravity(Gravity.CENTER);
        nav.addView(navTitle,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(nav);
        search=new EditText(this);search.setSingleLine(true);
        search.setTextSize(16);search.setHint("Search references");
        search.setContentDescription("Search original bibliography by author, title, year or DOI");
        UiKit.fieldInput(this,search);root.addView(search,new LinearLayout.LayoutParams(-1,dp(56)));
        savedToggle=PhLibrary2Ui.action(this,"Saved only",()->{
            savedOnly=!savedOnly;
            refresh();
        },false);
        savedToggle.setTextSize(14);
        LinearLayout.LayoutParams savedLp=new LinearLayout.LayoutParams(-1,dp(50));
        savedLp.topMargin=dp(7);
        root.addView(savedToggle,savedLp);
        count=PhLibrary2Ui.text(this,"",14,PhLibrary2Ui.FOREST,false,true);
        count.setPadding(0,dp(8),0,dp(8));root.addView(count);

        list=new ListView(this);list.setDividerHeight(dp(7));
        list.setDivider(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        list.setBackgroundColor(PhLibrary2Ui.PAPER);
        adapter=new BaseAdapter(){
            public int getCount(){return matches.size();}
            public Object getItem(int index){return matches.get(index);}
            public long getItemId(int index){return index;}
            public View getView(int position,View existing,ViewGroup parent){
                TextView row=existing instanceof TextView?(TextView)existing:
                    PhLibrary2Ui.text(PhLibraryBibliographyActivity.this,"",15,
                        PhLibrary2Ui.INK,false,false);
                String citation=matches.get(position).optString("text");
                boolean isSaved=ApoReferenceBookmarks.isSaved(
                    PhLibraryBibliographyActivity.this,citation);
                row.setText((isSaved?"★  ":"")+citation);
                row.setMaxLines(6);
                row.setEllipsize(TextUtils.TruncateAt.END);
                row.setContentDescription(
                    (isSaved?"Saved reference. ":"Reference citation. ")+citation);
                row.setLineSpacing(dp(4),1.11f);
                row.setPadding(dp(14),dp(13),dp(14),dp(13));
                row.setBackground(PhLibrary2Ui.touch(PhLibraryBibliographyActivity.this,
                    PhLibrary2Ui.IVORY,PhLibrary2Ui.LINE,13));
                row.setTextIsSelectable(false);
                row.setMinHeight(dp(70));
                return row;
            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent,view,position,id)->{
            String full=matches.get(position).optString("text");
            String sourceUrl=matches.get(position).optString("url");
            boolean isSaved=ApoReferenceBookmarks.isSaved(this,full);
            TextView detail=PhLibrary2Ui.text(this,full+
                "\n\nFull text: unavailable in this offline edition."+
                (sourceUrl.startsWith("https://")?
                    "\nOnline source: connection required.":""),16,
                PhLibrary2Ui.INK,false,false);
            detail.setPadding(dp(20),dp(15),dp(20),dp(15));
            detail.setTextIsSelectable(true);
            detail.setAutoLinkMask(Linkify.WEB_URLS);
            detail.setMovementMethod(LinkMovementMethod.getInstance());
            AlertDialog.Builder dialog=new AlertDialog.Builder(this).setTitle("Reference details")
                .setView(detail).setPositiveButton("Close",null)
                .setNegativeButton(isSaved?"Remove saved":"Save reference",(d,w)->{
                    ApoReferenceBookmarks.setSaved(this,full,!isSaved);
                    refresh();
                });
            if(sourceUrl.startsWith("https://"))
                dialog.setNeutralButton("Open source",(d,w)->{
                    try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(sourceUrl)));}
                    catch(Exception ignored){
                        new AlertDialog.Builder(this)
                            .setMessage("No app is available to open this source.")
                            .setPositiveButton("Close",null).show();
                    }
                });
            dialog.show();
        });
        root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        originalCitation=saved==null?
            getIntent().getStringExtra("phlibrary_bib_citation"):
            saved.getString("citation","");
        if(originalCitation==null)originalCitation="";
        String q=saved==null?(originalCitation.isEmpty()?
            getIntent().getStringExtra("phlibrary_bib_search"):originalCitation):
            saved.getString("q","");
        search.setText(q==null?"":q);
        search.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){refresh();}
            public void afterTextChanged(Editable e){}
        });
        refresh();
        if(saved!=null){
            int first=saved.getInt("first");
            int offset=saved.getInt("offset");
            list.post(()->list.setSelectionFromTop(first,offset));
        }
    }
    @Override protected void onSaveInstanceState(Bundle out){
        out.putString("q",search.getText().toString());
        out.putBoolean("saved_only",savedOnly);
        out.putString("citation",originalCitation);
        out.putInt("first",list.getFirstVisiblePosition());
        View child=list.getChildAt(0);out.putInt("offset",child==null?0:child.getTop());
        super.onSaveInstanceState(out);
    }
}
