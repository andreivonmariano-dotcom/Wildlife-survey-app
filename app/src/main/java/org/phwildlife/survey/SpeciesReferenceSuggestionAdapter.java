package org.phwildlife.survey;

import android.content.Context;
import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

/** Bounded, offline, current/previous-name suggestions for the reference picker. */
public final class SpeciesReferenceSuggestionAdapter
        extends ArrayAdapter<String> implements Filterable {
    private final FieldDb helper;
    private volatile String groupFilter;
    private final Filter filter=new Search();
    public SpeciesReferenceSuggestionAdapter(Context context,FieldDb db,String group){
        super(context,android.R.layout.simple_dropdown_item_1line,new ArrayList<String>());
        helper=db;groupFilter=group;
    }
    public void setGroup(String group){groupFilter=group;}
    @Override public Filter getFilter(){return filter;}
    private final class Search extends Filter {
        @Override protected FilterResults performFiltering(CharSequence input){
            String name=input==null?"":input.toString().trim();
            String group=groupFilter;
            String like=TaxonSearchPolicy.contains(name);
            String where=group==null||group.isEmpty()?"":" AND c.taxon_group=?";
            String sql="SELECT c.taxon_key,COALESCE(c.common_name,''),c.scientific_name,"+
                "COALESCE(c.taxon_group,'') FROM taxon_catalog c WHERE "+
                "(?='' OR LOWER(c.scientific_name) LIKE ? ESCAPE '!' OR "+
                "LOWER(COALESCE(c.common_name,'')) LIKE ? ESCAPE '!' OR EXISTS ("+
                "SELECT 1 FROM taxon_synonyms a WHERE a.taxon_key=c.taxon_key "+
                "AND LOWER(a.alias_name) LIKE ? ESCAPE '!'))"+where+
                " ORDER BY CASE WHEN c.taxon_key LIKE 'STARTER:%' THEN 1 ELSE 0 END,"+
                "c.scientific_name COLLATE NOCASE LIMIT 90";
            ArrayList<String> list=new ArrayList<>();
            HashSet<String> seen=new HashSet<>();
            ArrayList<String> args=new ArrayList<>();
            args.add(name.toLowerCase(Locale.ROOT));
            args.add(like);args.add(like);args.add(like);
            if(group!=null&&!group.isEmpty())args.add(group);
            try(Cursor c=helper.getReadableDatabase().rawQuery(
                    sql,args.toArray(new String[0]))){
                while(c.moveToNext()&&list.size()<24){
                    String scientific=c.getString(2);
                    if(scientific==null||!seen.add(scientific.toLowerCase(Locale.ROOT)))continue;
                    String common=c.getString(1);
                    list.add(common==null||common.isEmpty()?scientific:
                        common+" · "+scientific);
                }
            }catch(Exception ignored){}
            FilterResults values=new FilterResults();
            values.values=list;values.count=list.size();
            return values;
        }
        @Override protected void publishResults(CharSequence constraint,FilterResults results){
            clear();
            if(results!=null&&results.values instanceof ArrayList){
                @SuppressWarnings("unchecked")
                ArrayList<String> items=(ArrayList<String>)results.values;
                addAll(items);
            }
            notifyDataSetChanged();
        }
        @Override public CharSequence convertResultToString(Object item){
            return item==null?"":item.toString();
        }
    }
}