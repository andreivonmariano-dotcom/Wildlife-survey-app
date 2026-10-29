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
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.net.Uri;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Read-only catalog record and evidence page. This never changes a historic
 * observation or claims the species has been seen at the survey station.
 */
public final class TaxonDetailActivity extends Activity {
    private FieldDb db;
    private LinearLayout content;
    private AutoCompleteTextView search;
    private LinearLayout searchRow;
    private Button groupFilter;
    private TextView screenCaption;
    private String searchGroup="";
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
        body.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
        body.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        content.addView(body);
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        db=new FieldDb(this);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(18),dp(15),dp(18),dp(15));
        root.addView(UiKit.navigationHeader(this,"Species Library","Back",this::finish));
        screenCaption=text("Identification, habitat and research sources",12,false);
        screenCaption.setTextColor(UiKit.MUTED);
        root.addView(screenCaption);
        searchRow=new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        search=new AutoCompleteTextView(this){
            @Override public boolean enoughToFilter(){return true;}
        };
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setThreshold(1);
        search.setHint("Common, scientific, or former name");
        search.setContentDescription("Search species by scientific, common or former name");
        SpeciesReferenceSuggestionAdapter autocomplete=
            new SpeciesReferenceSuggestionAdapter(this,db,null);
        search.setAdapter(autocomplete);
        search.setDropDownHeight(dp(220));
        search.setOnItemClickListener((parent,view,position,id)->{
            search.dismissDropDown();
            lookup(parent.getItemAtPosition(position).toString());
        });
        searchRow.addView(search,new LinearLayout.LayoutParams(0,dp(52),1f));
        Button dropdown=UiKit.button(this,"▾","quiet");
        dropdown.setContentDescription("Show species suggestions");
        dropdown.setOnClickListener(v->{
            if(search.getText().toString().trim().length()<2){
                search.requestFocus();
                android.widget.Toast.makeText(this,
                    "Type at least two letters to see matching species",
                    android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            search.requestFocus();
            autocomplete.getFilter().filter(search.getText().toString(),
                count->{if(count>0)search.showDropDown();});
        });
        searchRow.addView(dropdown,new LinearLayout.LayoutParams(dp(48),dp(52)));
        Button find=UiKit.button(this,"Find","secondary");
        find.setOnClickListener(v->lookup(search.getText().toString()));
        searchRow.addView(find,new LinearLayout.LayoutParams(dp(80),dp(52)));
        root.addView(searchRow);
        final String[] groupNames={"All groups","Birds","Amphibians","Reptiles","Mammals"};
        groupFilter=UiKit.button(this,"All wildlife groups  ▾","quiet");
        groupFilter.setContentDescription("Filter species by wildlife group");
        LinearLayout.LayoutParams groupLp=new LinearLayout.LayoutParams(-1,dp(48));
        groupLp.topMargin=dp(6);
        root.addView(groupFilter,groupLp);
        groupFilter.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Browse wildlife group")
            .setItems(groupNames,(dialog,which)->{
                String group=which==0?null:groupNames[which];
                searchGroup=group==null?"":group;
                autocomplete.setGroup(group);
                groupFilter.setText(groupNames[which]+"  ▾");
                search.setText("");
                showMessage("Search "+(group==null?
                    "all Philippine wildlife groups":group.toLowerCase(Locale.ROOT))+
                    " by typing at least two letters.");
            }).setNegativeButton("Cancel",null).show());
        search.setOnEditorActionListener((v,action,event)->{
            lookup(search.getText().toString());return true;
        });
        ScrollView scroll=new ScrollView(this);
        content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0,dp(8),0,dp(30));
        scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        setContentView(root);
        String query=getIntent().getStringExtra("taxon_query");
        String key=getIntent().getStringExtra("taxon_key");
        if(key!=null&&!key.isEmpty())loadKey(key);
        else if(query!=null&&!query.trim().isEmpty()){
            search.setText(query);
            lookup(query);
        }else{
            content.addView(FieldVisuals.context(this,
                "Search names with at least two letters, or browse the reference accounts below."));
            OfflineFieldLibrary.addSpeciesDirectory(this,content,scientific->{
                search.setText(scientific);
                search.dismissDropDown();
                lookup(scientific);
            });
        }
    }
    private static String originalReference(String source){
        String label=source==null?"":source.toLowerCase(Locale.ROOT);
        if(label.contains("checklist of amphibians of the philippines"))
            return "https://www.researchgate.net/publication/360034142_Checklist_of_amphibians_of_the_Philippines_version_2025-1";
        if(label.contains("checklist of reptiles of the philippines"))
            return "https://www.researchgate.net/publication/362324825_Checklist_of_reptiles_of_the_Philippines_version_2026-1";
        if(label.contains("mammal diversity database"))
            return "https://doi.org/10.5281/zenodo.21654811";
        if(label.contains("avilist"))
            return "https://doi.org/10.2173/avilist.v2026";
        return "";
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
        java.util.HashSet<String> acceptedNames=new java.util.HashSet<>();
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT taxon_key,common_name,scientific_name FROM taxon_catalog WHERE ("+
                "taxon_key=? OR LOWER(scientific_name)=LOWER(?) OR "+
                "LOWER(common_name)=LOWER(?) OR "+
                "LOWER(scientific_name) LIKE ? ESCAPE '!' OR "+
                "LOWER(common_name) LIKE ? ESCAPE '!' OR EXISTS ("+
                "SELECT 1 FROM taxon_synonyms a WHERE a.taxon_key=taxon_catalog.taxon_key "+
                "AND LOWER(a.alias_name) LIKE ? ESCAPE '!')) "+
                "AND (?='' OR taxon_group=?) "+
                "ORDER BY CASE WHEN LOWER(scientific_name)=LOWER(?) THEN 0 ELSE 1 END,"+
                "CASE WHEN taxon_key LIKE 'STARTER:%' THEN 1 ELSE 0 END,"+
                "scientific_name COLLATE NOCASE LIMIT 64",
            new String[]{query,query,query,pattern,pattern,pattern,searchGroup,searchGroup,query})){
            while(c.moveToNext()){
                String common=c.isNull(1)?"":c.getString(1);
                String scientific=c.getString(2);
                // An old starter and an official checklist may describe one species.
                // Prefer the attributed source above the starter, never duplicate it.
                if(!acceptedNames.add(scientific.toLowerCase(Locale.ROOT)))continue;
                keys.add(c.getString(0));
                names.add((common.isEmpty()?"":common+" · ")+scientific);
            }
        }catch(Exception ex){showMessage("Catalog search unavailable.");return;}
        if(keys.isEmpty()){
            showMessage("No matching species in the offline catalog.");
            return;
        }
        if(keys.size()==1){loadKey(keys.get(0));return;}
        final String[] ids=keys.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle("Select a species")
            .setItems(names.toArray(new String[0]),(d,which)->loadKey(ids[which]))
            .setNegativeButton("Cancel",null).show();
    }
    /**
     * Publisher references open externally. Offline photographs use attributed
     * CC preview files shipped separately by the licensed reference pack.
     */
    private void sourceLink(String title,String candidate,String prefix){
        if(candidate==null||!candidate.startsWith(prefix))return;
        Button button=UiKit.button(this,title,"secondary");
        content.addView(button);
        button.setOnClickListener(v->{
            try{
                Intent open=new Intent(Intent.ACTION_VIEW,Uri.parse(candidate));
                startActivity(open);
            }catch(Exception ex){
                new AlertDialog.Builder(this)
                    .setMessage("No app is available to open this reference link.")
                    .setPositiveButton("Close",null).show();
            }
        });
    }
    private void showSearchControls(){
        if(searchRow!=null)searchRow.setVisibility(View.VISIBLE);
        if(groupFilter!=null)groupFilter.setVisibility(View.VISIBLE);
        if(screenCaption!=null)screenCaption.setVisibility(View.VISIBLE);
    }
    private void showMessage(String message){
        showSearchControls();
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
            searchRow.setVisibility(View.GONE);
            groupFilter.setVisibility(View.GONE);
            screenCaption.setVisibility(View.GONE);
            Button searchAnother=UiKit.button(this,"⌕  Search another species","quiet");
            searchAnother.setContentDescription("Search another wildlife species");
            LinearLayout.LayoutParams searchLp=new LinearLayout.LayoutParams(-1,dp(45));
            searchLp.bottomMargin=dp(9);
            content.addView(searchAnother,searchLp);
            searchAnother.setOnClickListener(v->{
                showSearchControls();
                search.requestFocus();
                search.selectAll();
                InputMethodManager keyboard=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(keyboard!=null)keyboard.showSoftInput(search,InputMethodManager.SHOW_IMPLICIT);
            });
            TextView common=text(col(c,"common_name"),21,true);
            if(!common.getText().toString().isEmpty())content.addView(common);
            TextView latin=text(scientific,19,true);
            latin.setTypeface(null,Typeface.BOLD_ITALIC);
            latin.setPadding(0,dp(3),0,dp(3));
            content.addView(latin);
            // All 96 prioritized taxa share one concise, citation-backed account.
            // Preserve the complete legacy catalog for the remaining source names.
            if(Rc7FieldAccounts.priority(this,scientific)){
                Rc11PrioritySpeciesView.add(this,content,c,scientific);
                return;
            }
            OfflinePhotoEvidence.addForTaxon(this,content,scientific);
            boolean detailed=OfflineFieldLibrary.addSpecies(this,content,scientific);
            boolean literature=Rc7LiteratureNotes.add(this,content,scientific);
            boolean rc7=Rc7FieldAccounts.add(this,content,scientific);
            Rc9ProfileCompleteness.add(this,content,scientific);
            content.addView(FieldVisuals.section(this,"Identification and taxonomy",""));
            paragraph("Classification",col(c,"taxon_group")+" · "+
                col(c,"order_name")+" · "+col(c,"family"));
            paragraph("Taxonomic authority",col(c,"authority"));
            paragraph("Endemism / origin",col(c,"distribution_status"));
            paragraph("Philippine distribution in source",col(c,"distribution_text"));
            content.addView(FieldVisuals.section(this,"Conservation references",""));
            paragraph("IUCN category (source snapshot)",col(c,"iucn_red_list"));
            paragraph("Philippine Red List category (source snapshot)",col(c,"ph_red_list"));
            paragraph("CITES (source snapshot)",col(c,"cites"));
            paragraph("Taxonomic note",col(c,"taxonomic_note"));
            if(Rc7FieldAccounts.priority(this,scientific)){
                Button guide=UiKit.button(this,"Compare field traits and habitat evidence","secondary");
                content.addView(guide);
                guide.setOnClickListener(v->startActivity(
                    new Intent(this,Rc8TraitComparisonActivity.class)
                        .putExtra("scientific_name",scientific)));
            }
            if(Rc7FieldAccounts.priority(this,scientific)){
                Button differences=UiKit.button(this,"Compare documented lookalike characters","quiet");
                differences.setOnClickListener(v->startActivity(
                    new Intent(this,Rc9DifferentialActivity.class).putExtra("scientific_name",scientific)));
                content.addView(differences);
            }
            if(OfflinePhotoEvidence.hasOfflineTaxon(this,scientific)){
                Button pictures=UiKit.button(this,"Compare offline wildlife photographs","secondary");
                content.addView(pictures);
                pictures.setOnClickListener(v->startActivity(
                    new Intent(this,Rc8PhotoComparisonActivity.class)
                        .putExtra("scientific_name",scientific)));
            }
            if(DiagnosticGuideActivity.hasProfile(this,scientific)){
                Button legacy=UiKit.button(this,"Compare source-checked ID characters","quiet");
                content.addView(legacy);
                legacy.setOnClickListener(v->startActivity(
                    new Intent(this,DiagnosticGuideActivity.class)
                        .putExtra("scientific_name",scientific)));
            }
            paragraph("Original source",col(c,"source")+" · "+col(c,"source_version"));
            paragraph("Source publication",originalReference(col(c,"source")));
            sourceLink("WBCP checklist source",col(c,"source_url"),
                "https://birdwatch.ph/");
            sourceLink("Bird photographs and identification on eBird",
                col(c,"ebird_url"),"https://ebird.org/species/");
            sourceLink("Bird vocalizations on xeno-canto",
                col(c,"sound_url"),"https://www.xeno-canto.org/species/");
            paragraph("Source record ID",col(c,"taxon_key"));
            Button iNat=UiKit.button(this,"iNaturalist photos · dreidrei (online)","quiet");
            iNat.setOnClickListener(v->startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.inaturalist.org/observations?user_id=dreidrei"+
                    "&taxon_name="+Uri.encode(scientific)+"&photos=true"))));
            content.addView(iNat);
            content.addView(FieldVisuals.section(this,"Habitat and elevation",""));
            try(Cursor ecology=database.rawQuery(
                "SELECT habitat_codes,elevation_min_m,elevation_max_m,"+
                    "source,source_version,review_status,source_url,evidence_note "+
                    "FROM taxon_habitat_profiles WHERE taxon_key=? LIMIT 1",
                new String[]{key})){
                if(ecology.moveToFirst()){
                    String habitats=ecology.getString(0).replace("|",", ");
                    paragraph("Reviewed habitat codes",habitats);
                    String low=ecology.isNull(1)?"Unassessed":ecology.getString(1)+" m";
                    String high=ecology.isNull(2)?"Unassessed":ecology.getString(2)+" m";
                    paragraph("Published elevation reference","Minimum: "+low+
                        " · Maximum: "+high);
                    paragraph("Habitat reference",ecology.getString(3)+
                        " ("+ecology.getString(4)+")");
                    paragraph("Scope and limitations",
                        ecology.isNull(7)?"":ecology.getString(7));
                    if(!ecology.isNull(6)&&
                        ecology.getString(6).startsWith("https://")){
                        String cited=ecology.getString(6);
                        sourceLink("Open habitat study",cited,"https://");
                    }
                }
            }
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
            content.addView(FieldVisuals.section(this,"Regional source evidence",""));
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
            TextView foot=text("The source and edition for each reference appear above. "+
                "Source links require internet. External photographs and recordings "+
                "remain with their publishers. No reference confirms a sighting "+
                "at this station.",12,false);
            foot.setTextColor(UiKit.MUTED);
            foot.setPadding(0,dp(15),0,0);
            content.addView(foot);
        }catch(Exception ex){showMessage("Unable to read this species record.");}
    }
    @Override protected void onDestroy(){db.close();super.onDestroy();}
}
