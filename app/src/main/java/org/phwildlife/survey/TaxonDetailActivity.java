package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Read-only catalog record and evidence page. This never changes a historic
 * observation or claims the species has been seen at the survey station.
 */
public final class TaxonDetailActivity extends Activity {
    private FieldDb db;
    private LinearLayout content;
    private EditText search;
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView text(String value,int size,boolean bold){
        TextView v=new TextView(this);
        v.setText(value);v.setTextSize(size);v.setTextColor(UiKit.INK);
        if(bold)v.setTypeface(null,Typeface.BOLD);
        return v;
    }
    private void paragraph(String heading,String value){
        if(value==null||value.trim().isEmpty()||value.equalsIgnoreCase("NL"))return;
        TextView title=text(heading,12,true);
        title.setTextColor(UiKit.MUTED);
        title.setPadding(0,dp(13),0,dp(3));
        content.addView(title);
        TextView body=text(value,15,false);
        body.setTextIsSelectable(true);
        content.addView(body);
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        db=new FieldDb(this);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(18),dp(15),dp(18),dp(15));
        TextView header=UiKit.title(this,"Species reference");
        root.addView(header);
        TextView caption=text("Offline source records, not confirmed field sightings",12,false);
        caption.setTextColor(UiKit.MUTED);
        root.addView(caption);
        LinearLayout input=new LinearLayout(this);
        input.setOrientation(LinearLayout.HORIZONTAL);
        search=new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setHint("Common or scientific name");
        input.addView(search,new LinearLayout.LayoutParams(0,dp(52),1f));
        Button find=UiKit.button(this,"Find","secondary");
        find.setOnClickListener(v->lookup(search.getText().toString()));
        input.addView(find,new LinearLayout.LayoutParams(dp(80),dp(52)));
        root.addView(input);
        search.setOnEditorActionListener((v,action,event)->{
            lookup(search.getText().toString());return true;
        });
        ScrollView scroll=new ScrollView(this);
        content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0,dp(8),0,dp(30));
        scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Button back=UiKit.button(this,"Back","quiet");
        back.setOnClickListener(v->finish());
        root.addView(back);
        setContentView(root);
        String query=getIntent().getStringExtra("taxon_query");
        String key=getIntent().getStringExtra("taxon_key");
        if(key!=null&&!key.isEmpty())loadKey(key);
        else if(query!=null&&!query.trim().isEmpty()){
            search.setText(query);
            lookup(query);
        }else{
            content.addView(text("Search the installed bird, mammal, amphibian and reptile catalogs.",14,false));
        }
    }
    private static String col(Cursor c,String field){
        int i=c.getColumnIndex(field);
        return i<0||c.isNull(i)?"":c.getString(i).trim();
    }
    private void lookup(String input){
        String query=input==null?"":input.trim();
        if(query.isEmpty())return;
        int divider=query.lastIndexOf(" · ");
        if(divider>=0)query=query.substring(divider+3).trim();
        String pattern="%"+TaxonSearchPolicy.escapeLike(query.toLowerCase(Locale.ROOT))+"%";
        ArrayList<String> names=new ArrayList<>(),keys=new ArrayList<>();
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT taxon_key,common_name,scientific_name FROM taxon_catalog WHERE "+
                "taxon_key=? OR LOWER(scientific_name)=LOWER(?) OR "+
                "LOWER(common_name)=LOWER(?) OR "+
                "LOWER(scientific_name) LIKE ? ESCAPE '!' OR "+
                "LOWER(common_name) LIKE ? ESCAPE '!' "+
                "ORDER BY CASE WHEN LOWER(scientific_name)=LOWER(?) THEN 0 ELSE 1 END,"+
                "scientific_name COLLATE NOCASE LIMIT 32",
            new String[]{query,query,query,pattern,pattern,query})){
            while(c.moveToNext()){
                keys.add(c.getString(0));
                String common=c.isNull(1)?"":c.getString(1);
                String scientific=c.getString(2);
                names.add((common.isEmpty()?"":common+" · ")+scientific);
            }
        }catch(Exception ex){showMessage("Catalog search unavailable.");return;}
        if(keys.isEmpty()){
            showMessage("This name is not in the installed catalog. "+
                "Verbatim or unidentified field observations remain available.");
            return;
        }
        if(keys.size()==1){loadKey(keys.get(0));return;}
        final String[] ids=keys.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle("Select a species")
            .setItems(names.toArray(new String[0]),(d,which)->loadKey(ids[which]))
            .setNegativeButton("Cancel",null).show();
    }
    private void showMessage(String message){
        content.removeAllViews();
        content.addView(text(message,15,false));
    }
    private void loadKey(String key){
        SQLiteDatabase database=db.getReadableDatabase();
        try(Cursor c=database.rawQuery(
            "SELECT * FROM taxon_catalog WHERE taxon_key=? LIMIT 1",new String[]{key})){
            if(!c.moveToFirst()){showMessage("Species record unavailable.");return;}
            String scientific=col(c,"scientific_name");
            content.removeAllViews();
            TextView common=text(col(c,"common_name"),21,true);
            if(!common.getText().toString().isEmpty())content.addView(common);
            TextView latin=text(scientific,19,true);
            latin.setTypeface(null,Typeface.BOLD_ITALIC);
            latin.setPadding(0,dp(3),0,dp(3));
            content.addView(latin);
            paragraph("Classification",col(c,"taxon_group")+" · "+
                col(c,"order_name")+" · "+col(c,"family"));
            paragraph("Taxonomic authority",col(c,"authority"));
            paragraph("Endemism / origin",col(c,"distribution_status"));
            paragraph("Philippine distribution in source",col(c,"distribution_text"));
            paragraph("IUCN category (source snapshot)",col(c,"iucn_red_list"));
            paragraph("Philippine Red List category (source snapshot)",col(c,"ph_red_list"));
            paragraph("CITES (source snapshot)",col(c,"cites"));
            paragraph("Taxonomic note",col(c,"taxonomic_note"));
            paragraph("Original source",col(c,"source")+" · "+col(c,"source_version"));
            paragraph("Source record ID",col(c,"taxon_key"));
            if(col(c,"review_status").equals("unreviewed"))
                paragraph("Review status","Not independently reviewed for regional occurrence");
            StringBuilder names=new StringBuilder();
            try(Cursor aliases=database.rawQuery(
                "SELECT alias_name,source,source_version FROM taxon_synonyms "+
                    "WHERE taxon_key=? ORDER BY alias_name COLLATE NOCASE LIMIT 30",
                new String[]{key})){
                while(aliases.moveToNext()){
                    if(names.length()>0)names.append("\n");
                    names.append(aliases.getString(0));
                    if(!aliases.isNull(1)&&!aliases.getString(1).isEmpty())
                        names.append(" (").append(aliases.getString(1))
                            .append(" · ").append(aliases.getString(2)).append(")");
                }
            }
            if(names.length()>0)paragraph("Previous scientific names",names.toString());
            StringBuilder regional=new StringBuilder();
            try(Cursor evidence=database.rawQuery(
                "SELECT area_type,area_label,evidence_basis,evidence_text,"+
                    "source,source_version,source_url,review_status "+
                    "FROM taxon_occurrence_evidence WHERE taxon_key=? "+
                    "ORDER BY area_type,area_label LIMIT 60",new String[]{key})){
                while(evidence.moveToNext()){
                    if(regional.length()>0)regional.append("\n\n");
                    regional.append(evidence.getString(1)).append(" (")
                        .append(evidence.getString(0)).append(") · ")
                        .append(evidence.getString(7)).append("\n")
                        .append(evidence.getString(3)).append("\n")
                        .append(evidence.getString(4)).append(" · ")
                        .append(evidence.getString(5));
                    if(!evidence.isNull(6)&&!evidence.getString(6).isEmpty())
                        regional.append("\n").append(evidence.getString(6));
                }
            }catch(Exception ignored){}
            if(regional.length()>0)
                paragraph("Regional occurrence evidence",regional.toString());
            else paragraph("Regional occurrence evidence","Not assessed in the installed evidence catalog");
            TextView foot=text("A source-listed location is not a verified observation "+
                "at this station. Status categories reflect the source's publication date.",12,false);
            foot.setTextColor(UiKit.MUTED);
            foot.setPadding(0,dp(15),0,0);
            content.addView(foot);
        }catch(Exception ex){showMessage("Unable to read this species record.");}
    }
    @Override protected void onDestroy(){db.close();super.onDestroy();}
}
