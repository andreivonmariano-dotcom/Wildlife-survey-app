package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
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
    private TextView count;
    private ListView list;
    private BaseAdapter adapter;
    private int dp(float v){return PhLibrary2Ui.dp(this,v);}
    private void refresh(){
        matches.clear();
        String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);
        for(JSONObject item:refs){
            if(q.isEmpty()||item.optString("text").toLowerCase(Locale.ROOT).contains(q))
                matches.add(item);
        }
        count.setText(matches.size()+" of "+refs.size()+" original references"+
            (matches.isEmpty()&&q.length()>0?" · No exact text match":""));
        adapter.notifyDataSetChanged();
        list.setSelection(0);
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setStatusBarColor(PhLibrary2Ui.PAPER);
        getWindow().setNavigationBarColor(PhLibrary2Ui.PAPER);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        JSONArray items=PhLibraryBook01.bibliography(this);
        for(int i=0;i<items.length();i++){
            JSONObject ref=items.optJSONObject(i);if(ref!=null)refs.add(ref);
        }
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(7),dp(16),dp(4));root.setBackgroundColor(PhLibrary2Ui.PAPER);
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.addView(PhLibrary2Ui.action(this,"Back",this::finish,false),
            new LinearLayout.LayoutParams(dp(75),dp(48)));
        TextView navTitle=PhLibrary2Ui.text(this,"REFERENCE LIBRARY",14,PhLibrary2Ui.FOREST,false,true);
        navTitle.setGravity(Gravity.CENTER);
        nav.addView(navTitle,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(nav);
        root.addView(PhLibrary2Ui.eyebrow(this,"BOOK 01  /  ORIGINAL BIBLIOGRAPHY"));
        TextView heading=PhLibrary2Ui.text(this,"Research sources",31,PhLibrary2Ui.INK,true,false);
        root.addView(heading);
        TextView intro=PhLibrary2Ui.text(this,
            "1,159 full original bibliography records, searchable offline. "+
            "Individual author–year account citations are not yet completely reconciled.",
            14,PhLibrary2Ui.MUTED,false,false);
        intro.setLineSpacing(dp(3),1.05f);intro.setPadding(0,dp(7),0,dp(14));
        root.addView(intro);
        search=new EditText(this);search.setSingleLine(true);
        search.setTextSize(16);search.setHint("Author, title, year or DOI");
        UiKit.fieldInput(this,search);root.addView(search,new LinearLayout.LayoutParams(-1,dp(56)));
        count=PhLibrary2Ui.text(this,"",14,PhLibrary2Ui.FOREST,false,true);
        count.setPadding(0,dp(14),0,dp(8));root.addView(count);

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
                row.setText(matches.get(position).optString("text"));
                row.setLineSpacing(dp(3),1.06f);
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
            TextView detail=PhLibrary2Ui.text(this,full,16,PhLibrary2Ui.INK,false,false);
            detail.setPadding(dp(20),dp(15),dp(20),dp(15));
            detail.setTextIsSelectable(true);
            detail.setAutoLinkMask(Linkify.WEB_URLS);
            detail.setMovementMethod(LinkMovementMethod.getInstance());
            new AlertDialog.Builder(this).setTitle("Source reference")
                .setView(detail).setPositiveButton("Close",null).show();
        });
        root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        String q=saved==null?getIntent().getStringExtra("phlibrary_bib_search"):
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
        out.putInt("first",list.getFirstVisiblePosition());
        View child=list.getChildAt(0);out.putInt("offset",child==null?0:child.getTop());
        super.onSaveInstanceState(out);
    }
}
