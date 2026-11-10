package org.phwildlife.survey;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Performs bounded catalog search on Android's filter worker rather than
 * allocating every national taxon as a UI dropdown entry.
 *
 * Never treats national occurrence as verified island/province occurrence.
 */
public final class TaxonSuggestionAdapter extends ArrayAdapter<String> implements Filterable {
    private final FieldDb helper;
    private final Filter searchFilter=new SearchFilter();
    private String group,geographicRegion,provincePsgc;
    private boolean documentedOnly;
    private java.util.Map<String,String> detailByName=new LinkedHashMap<>();
    private static final class SearchItems {
        final List<String> names;
        final java.util.Map<String,String> metadata;
        SearchItems(List<String> names,java.util.Map<String,String> metadata){
            this.names=names;this.metadata=metadata;
        }
    }
    private int dp(float value){
        return Math.round(getContext().getResources().getDisplayMetrics().density*value);
    }
    @Override public View getView(int position,View convertView,ViewGroup parent){
        LinearLayout row;
        if(convertView instanceof LinearLayout)row=(LinearLayout)convertView;
        else{
            row=new LinearLayout(getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(12),dp(7),dp(12),dp(7));
            TextView name=new TextView(getContext());
            name.setTextSize(15);name.setTextColor(UiKit.INK);
            name.setMaxLines(2);
            row.addView(name);
            TextView meta=new TextView(getContext());
            meta.setTextSize(12);meta.setTextColor(UiKit.MUTED);
            meta.setMaxLines(2);
            row.addView(meta);
        }
        String item=getItem(position);
        TextView heading=(TextView)row.getChildAt(0);
        TextView detail=(TextView)row.getChildAt(1);
        heading.setText(item);
        String description=detailByName.get(item);
        detail.setText(description==null?"":description);
        detail.setVisibility(description==null||description.isEmpty()?View.GONE:View.VISIBLE);
        return row;
    }
    @Override public View getDropDownView(int position,View convertView,ViewGroup parent){
        return getView(position,convertView,parent);
    }
    private static String detail(String endemism,String iucn,String philippines,
            String synonym,String region){
        StringBuilder s=new StringBuilder();
        if(endemism!=null&&!endemism.isEmpty())s.append(endemism);
        if(iucn!=null&&!iucn.isEmpty()){
            if(s.length()>0)s.append(" · ");
            s.append("IUCN ").append(iucn);
        }
        if(philippines!=null&&!philippines.isEmpty()){
            if(s.length()>0)s.append(" · ");
            s.append("PH ").append(philippines);
        }
        if(synonym!=null&&!synonym.isEmpty()){
            if(s.length()>0)s.append(" · ");
            s.append("Prior name: ").append(synonym.length()>48?
                synonym.substring(0,45)+"…":synonym);
        }
        if(region!=null&&region.contains("MINDANAO")){
            if(s.length()>0)s.append(" · ");
            s.append("Mindanao listed");
        }
        return s.toString();
    }
    public TaxonSuggestionAdapter(Context context,FieldDb helper,
            String group,String geographicRegion,String provincePsgc,boolean documentedOnly){
        super(context,android.R.layout.simple_dropdown_item_1line,new ArrayList<String>());
        this.helper=helper;
        setScope(group,geographicRegion,provincePsgc,documentedOnly);
    }
    public void setScope(String group,String region,String psgc,boolean documentedOnly){
        this.group=group;
        this.geographicRegion=region==null?"PH":region;
        this.provincePsgc=psgc==null?"":psgc;
        this.documentedOnly=documentedOnly;
    }
    @Override public Filter getFilter(){return searchFilter;}
    private final class SearchFilter extends Filter{
        @Override protected FilterResults performFiltering(CharSequence constraint){
            String term=TaxonSearchPolicy.trim(constraint==null?"":constraint.toString());
            final String selectedGroup=group;
            final String selectedRegion=geographicRegion;
            final String selectedProvince=provincePsgc;
            final boolean regional=TaxonSearchPolicy.hasDocumentedRegion(
                documentedOnly,selectedRegion);
            LinkedHashSet<String> choices=new LinkedHashSet<>();
            LinkedHashMap<String,String> metadata=new LinkedHashMap<>();
            for(String unknown:Taxa.unknownLabels(selectedGroup)){
                if(term.isEmpty()||unknown.toLowerCase(java.util.Locale.ROOT).contains(term))
                    choices.add(unknown);
            }
            try{
                SQLiteDatabase database=helper.getReadableDatabase();
                // Favorites and recent observations supplement the authoritative
                // catalog and preserve verbatim names absent from published taxa.
                String favoriteSql="SELECT raw_identification FROM taxa_favorites WHERE taxon_group=?"+
                    (term.isEmpty()?"":" AND LOWER(raw_identification) LIKE ? ESCAPE '!'")+
                    " ORDER BY raw_identification LIMIT 12";
                String[] favorites=term.isEmpty()?new String[]{selectedGroup}:
                    new String[]{selectedGroup,TaxonSearchPolicy.contains(term)};
                try(Cursor c=database.rawQuery(favoriteSql,favorites)){
                    while(c.moveToNext())choices.add(c.getString(0));
                }
                StringBuilder sql=new StringBuilder(
                    "SELECT common_name,scientific_name,distribution_status,iucn_red_list,"+
                    "ph_red_list,synonym_text,region_codes FROM taxon_catalog WHERE taxon_group=?");
                ArrayList<String> args=new ArrayList<>();
                args.add(selectedGroup);
                if(regional){
                    sql.append(" AND review_status='reviewed' AND (INSTR('|'||"+
                        "COALESCE(region_codes,'')||'|','|'||?||'|')>0");
                    args.add(selectedRegion);
                    if(!selectedProvince.isEmpty()){
                        sql.append(" OR INSTR('|'||COALESCE(region_codes,'')||'|','|'||?||'|')>0");
                        args.add("P:"+selectedProvince);
                    }
                    sql.append(")");
                }
                if(!term.isEmpty()){
                    sql.append(" AND (LOWER(common_name) LIKE ? ESCAPE '!'"+
                        " OR LOWER(scientific_name) LIKE ? ESCAPE '!')");
                    String pattern=term.length()<TaxonSearchPolicy.MIN_CONTAINS_LENGTH?
                        TaxonSearchPolicy.prefix(term):TaxonSearchPolicy.contains(term);
                    args.add(pattern);args.add(pattern);
                }
                sql.append(" ORDER BY ");
                if(!term.isEmpty()){
                    sql.append("CASE WHEN LOWER(scientific_name)=? THEN 0 "+
                        "WHEN LOWER(common_name)=? THEN 1 "+
                        "WHEN LOWER(common_name) LIKE ? ESCAPE '!' THEN 2 "+
                        "WHEN LOWER(scientific_name) LIKE ? ESCAPE '!' THEN 3 "+
                        "ELSE 4 END,");
                    args.add(term);args.add(term);
                    args.add(TaxonSearchPolicy.prefix(term));
                    args.add(TaxonSearchPolicy.prefix(term));
                }
                // Rank independently documented island matches first, but never
                // remove national candidates with unassessed local occurrence.
                if(!regional&&selectedRegion!=null&&
                    !selectedRegion.equals("PH")&&!selectedRegion.equals("OTHER")){
                    sql.append("CASE WHEN review_status='reviewed' AND ("+
                        "INSTR('|'||COALESCE(region_codes,'')||'|','|'||?||'|')>0");
                    args.add(selectedRegion);
                    if(!selectedProvince.isEmpty()){
                        sql.append(" OR INSTR('|'||COALESCE(region_codes,'')||'|','|'||?||'|')>0");
                        args.add("P:"+selectedProvince);
                    }
                    sql.append(") THEN 0 ELSE 1 END,");
                }
                sql.append(" common_name COLLATE NOCASE,scientific_name COLLATE NOCASE LIMIT 55");
                try(Cursor c=database.rawQuery(sql.toString(),args.toArray(new String[0]))){
                    while(c.moveToNext()){
                        String common=c.isNull(0)?"":c.getString(0);
                        String scientific=c.isNull(1)?"":c.getString(1);
                        if(!scientific.isEmpty()){
                            String label=common.isEmpty()?scientific:common+" · "+scientific;
                            choices.add(label);
                            metadata.put(label,detail(c.getString(2),c.getString(3),
                                c.getString(4),c.getString(5),c.getString(6)));
                        }
                    }
                }
                // Older names can resolve to an accepted taxon through a reviewed
                // crosswalk. The suggestion displays its accepted current label.
                // Historic records and raw verbatim observations are never rewritten.
                if(!term.isEmpty()){
                    String aliasSql="SELECT c.common_name,c.scientific_name,"+
                        "c.distribution_status,c.iucn_red_list,c.ph_red_list,"+
                        "s.alias_name,c.region_codes FROM "+
                        "taxon_synonyms s JOIN taxon_catalog c ON c.taxon_key=s.taxon_key "+
                        "WHERE c.taxon_group=? AND LOWER(s.alias_name) LIKE ? ESCAPE '!'"+
                        (regional?" AND c.review_status='reviewed' AND (INSTR('|'||"+
                            "COALESCE(c.region_codes,'')||'|','|'||?||'|')>0"+
                            (selectedProvince.isEmpty()?"":" OR INSTR('|'||COALESCE(c.region_codes,'')||"+
                                "'|','|'||?||'|')>0")+")":"")+
                        " ORDER BY s.alias_name COLLATE NOCASE LIMIT 12";
                    ArrayList<String> aliasArgs=new ArrayList<>();
                    aliasArgs.add(selectedGroup);
                    aliasArgs.add(TaxonSearchPolicy.contains(term));
                    if(regional){
                        aliasArgs.add(selectedRegion);
                        if(!selectedProvince.isEmpty())aliasArgs.add("P:"+selectedProvince);
                    }
                    try(Cursor aliases=database.rawQuery(aliasSql,
                            aliasArgs.toArray(new String[0]))){
                        while(aliases.moveToNext()){
                            String common=aliases.isNull(0)?"":aliases.getString(0);
                            String scientific=aliases.isNull(1)?"":aliases.getString(1);
                            if(!scientific.isEmpty()){
                                String label=common.isEmpty()?scientific:common+" · "+scientific;
                                choices.add(label);
                                metadata.put(label,detail(aliases.getString(2),
                                    aliases.getString(3),aliases.getString(4),
                                    aliases.getString(5),aliases.getString(6)));
                            }
                        }
                    }
                }
                // Only show historic data if the user is not asking for strictly
                // documented regional evidence.
                if(!regional){
                    String recent="SELECT common_name,scientific_name,raw_identification FROM "+
                        "field_records WHERE taxon_group=? AND deleted_at IS NULL "+
                        (term.isEmpty()?"":"AND (LOWER(common_name) LIKE ? ESCAPE '!' OR "+
                            "LOWER(scientific_name) LIKE ? ESCAPE '!' OR "+
                            "LOWER(raw_identification) LIKE ? ESCAPE '!') ")+
                        "ORDER BY id DESC LIMIT 10";
                    String[] recentArgs=term.isEmpty()?new String[]{selectedGroup}:
                        new String[]{selectedGroup,TaxonSearchPolicy.contains(term),
                            TaxonSearchPolicy.contains(term),TaxonSearchPolicy.contains(term)};
                    try(Cursor c=database.rawQuery(recent,recentArgs)){
                        while(c.moveToNext()){
                            String common=c.isNull(0)?"":c.getString(0);
                            String scientific=c.isNull(1)?"":c.getString(1);
                            String verbatim=c.isNull(2)?"":c.getString(2);
                            String label=!common.isEmpty()&&!scientific.isEmpty()?
                                common+" · "+scientific:!verbatim.isEmpty()?verbatim:scientific;
                            if(!label.isEmpty())choices.add(label);
                        }
                    }
                }
            }catch(Exception ignored){
                // Field-recording remains possible during catalog upgrades;
                // the observer can still enter a verbatim identification.
            }
            List<String> values=new ArrayList<>(TaxonSearchPolicy.MAX_SUGGESTIONS);
            for(String value:choices){
                if(values.size()>=TaxonSearchPolicy.MAX_SUGGESTIONS)break;
                values.add(value);
            }
            FilterResults result=new FilterResults();
            result.values=new SearchItems(values,metadata);
            result.count=values.size();
            return result;
        }
        @Override protected void publishResults(CharSequence constraint,FilterResults results){
            clear();
            if(results!=null&&results.values instanceof SearchItems){
                SearchItems items=(SearchItems)results.values;
                detailByName=items.metadata;
                addAll(items.names);
            }
            notifyDataSetChanged();
        }
        @Override public CharSequence convertResultToString(Object result){
            return result==null?"":result.toString();
        }
    }
}
