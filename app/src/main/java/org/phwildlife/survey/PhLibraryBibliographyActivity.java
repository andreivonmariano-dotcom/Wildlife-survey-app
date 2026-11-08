package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

/** Full original 1,159-entry bibliography, independent of unresolved citation matching. */
public final class PhLibraryBibliographyActivity extends Activity {
    private JSONArray refs;
    private EditText search;
    private LinearLayout list;
    private TextView count;
    private int dp(float value){return PhLibraryStyle.dp(this,value);}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);root.setPadding(dp(16),dp(10),dp(16),dp(8));
        root.addView(PhLibraryStyle.navigation(this,"Bibliography","Book 01",this::finish));
        TextView title=PhLibraryStyle.text(this,"Book 01 references",23,UiKit.INK,true);
        title.setPadding(0,dp(15),0,dp(6));root.addView(title);
        TextView subtitle=PhLibraryStyle.text(this,"Original source bibliography · Offline",13,UiKit.MUTED,false);
        root.addView(subtitle);
        search=new EditText(this);search.setSingleLine(true);search.setTextSize(16);
        search.setHint("Search author, year, title or DOI");UiKit.fieldInput(this,search);
        root.addView(search);
        count=PhLibraryStyle.text(this,"",13,UiKit.MUTED,false);count.setPadding(0,dp(12),0,dp(9));root.addView(count);
        ScrollView scroll=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        refs=PhLibraryBook01.bibliography(this);
        search.setText(state==null?getIntent().getStringExtra("phlibrary_bib_search"):state.getString("q",""));
        search.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){render();}
            public void afterTextChanged(Editable e){}
        });
        setContentView(root);render();
    }
    private void render(){
        list.removeAllViews();String term=search.getText().toString().toLowerCase(Locale.ROOT).trim();
        int found=0;
        for(int i=0;i<refs.length();i++){
            JSONObject item=refs.optJSONObject(i);if(item==null)continue;
            String full=item.optString("text");
            if(!term.isEmpty()&&!full.toLowerCase(Locale.ROOT).contains(term))continue;
            found++;
            if(found>45)continue; // avoid materializing hundreds of views
            TextView row=PhLibraryStyle.text(this,full,14,UiKit.INK,false);
            row.setTextIsSelectable(true);row.setLineSpacing(dp(3),1.04f);
            row.setPadding(dp(10),dp(9),dp(10),dp(9));
            row.setBackground(PhLibraryStyle.touch(this,android.graphics.Color.WHITE,UiKit.BORDER,12));
            row.setClickable(true);row.setFocusable(true);
            row.setOnClickListener(v->{
                TextView body=PhLibraryStyle.text(this,full,15,UiKit.INK,false);
                body.setPadding(dp(18),dp(12),dp(18),dp(12));
                body.setAutoLinkMask(Linkify.WEB_URLS);body.setMovementMethod(LinkMovementMethod.getInstance());
                body.setTextIsSelectable(true);
                new AlertDialog.Builder(this).setTitle("Source reference")
                    .setView(body).setPositiveButton("Close",null).show();
            });
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(8);list.addView(row,lp);
        }
        count.setText(refs.length()==0?"Reference package unavailable":found+" references"+(found>45?" · First 45 shown. Refine search.":""));
    }
    @Override protected void onSaveInstanceState(Bundle out){out.putString("q",search.getText().toString());super.onSaveInstanceState(out);}
}
