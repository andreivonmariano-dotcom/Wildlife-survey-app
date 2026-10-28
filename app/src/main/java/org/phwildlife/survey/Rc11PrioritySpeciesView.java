package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

/**
 * One readable, cited species-account layout for the full 96-priority registry.
 * Scientific review status stays in the reference drawer; nothing is reclassified.
 */
public final class Rc11PrioritySpeciesView {
    private Rc11PrioritySpeciesView(){}
    private static final String[] PACKS={
        "rc10_research_batch1_v1.json","rc10_research_batch2_v1.json",
        "rc10_research_batch3_v1.json"
    };
    private static final String[] AREAS={"Identification","Habitat","Range",
        "Natural history","Conservation"};
    private static final String[] ICONS={"search","leaf","pin","tracks","shield"};
    private static JSONObject matrix;
    private static final JSONObject[] packs=new JSONObject[PACKS.length];
    private static int dp(Activity a,float n){
        return Math.round(a.getResources().getDisplayMetrics().density*n);
    }
    private static JSONObject asset(Activity a,String name,int maximum){
        try(InputStream in=a.getAssets().open(name);
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buf=new byte[8192];int count;
            while((count=in.read(buf))!=-1){
                if(out.size()+count>maximum)throw new IllegalStateException("Reference oversized");
                out.write(buf,0,count);
            }
            return new JSONObject(out.toString("UTF-8"));
        }catch(Exception ignored){return new JSONObject();}
    }
    private static synchronized void load(Activity a){
        if(matrix==null)matrix=asset(a,"rc8_evidence_comparisons_v1.json",450000);
        for(int i=0;i<PACKS.length;i++)
            if(packs[i]==null)packs[i]=asset(a,PACKS[i],65000);
    }
    private static String column(Cursor row,String key){
        int index=row.getColumnIndex(key);
        return index<0||row.isNull(index)?"":row.getString(index).trim();
    }
    private static TextView text(Activity a,String text,int size,boolean bold,int color){
        TextView v=new TextView(a);
        v.setText(text);v.setTextSize(size);v.setTextColor(color);
        if(bold)v.setTypeface(null,Typeface.BOLD);
        v.setTextIsSelectable(true);
        return v;
    }
    private static String area(String category){
        if("Identification".equals(category)||"Measurements".equals(category)||
            "Differential".equals(category))return "Identification";
        if("Habitat & ecology".equals(category))return "Habitat";
        if("Range".equals(category))return "Range";
        if("Behavior & sound".equals(category)||"Ecology".equals(category)||
            "Behavior".equals(category)||"Life stage".equals(category)||
            "Reproduction".equals(category))return "Natural history";
        if("Conservation".equals(category))return "Conservation";
        return null;
    }
    private static final class Fact {
        final String area,label,value,source,url,limitation,publication,review;
        Fact(String area,String label,String value,String source,String url,
             String limitation,String publication,String review){
            this.area=area;this.label=label;this.value=value;this.source=source;
            this.url=url;this.limitation=limitation;
            this.publication=publication;this.review=review;
        }
    }
    private static ArrayList<Fact> facts(Activity a,String scientific){
        load(a);
        ArrayList<Fact> list=new ArrayList<>();
        HashSet<String> seen=new HashSet<>();
        // The recent species-specific research is presented ahead of older matrix summaries.
        for(JSONObject pack:packs){
            JSONArray rows=pack.optJSONArray("records");
            if(rows==null)continue;
            for(int i=0;i<rows.length();i++){
                JSONObject species=rows.optJSONObject(i);
                if(species==null||!scientific.equalsIgnoreCase(
                    species.optString("scientificName")))continue;
                JSONArray items=species.optJSONArray("items");
                if(items==null)break;
                for(int j=0;j<items.length();j++){
                    JSONObject item=items.optJSONObject(j);
                    if(item==null)continue;
                    String category=area(item.optString("category"));
                    add(list,seen,category,item.optString("label"),
                        item.optString("statement"),item.optString("source"),
                        item.optString("url"),item.optString("limitation"),
                        item.isNull("publicationYear")?"Date not stated":
                            String.valueOf(item.optInt("publicationYear")),
                        item.optString("reviewState"));
                }
            }
        }
        JSONArray entries=matrix.optJSONArray("species");
        if(entries!=null)for(int i=0;i<entries.length();i++){
            JSONObject species=entries.optJSONObject(i);
            if(species==null||!scientific.equalsIgnoreCase(
                species.optString("scientificName")))continue;
            JSONArray traits=species.optJSONArray("traits");
            if(traits!=null)for(int j=0;j<traits.length();j++){
                JSONObject claim=traits.optJSONObject(j);
                if(claim==null)continue;
                add(list,seen,area(claim.optString("category")),
                    claim.optString("label"),claim.optString("value"),
                    claim.optString("source"),claim.optString("url"),"",
                    "Source edition",claim.optString("review"));
            }
            break;
        }
        return list;
    }
    private static void add(ArrayList<Fact> list,HashSet<String> seen,
            String area,String label,String value,String source,String url,
            String limitation,String publication,String review){
        if(area==null||label.trim().isEmpty()||value.trim().isEmpty()||
            source.trim().isEmpty()||!url.startsWith("https://"))return;
        String key=area+"|"+value.trim().toLowerCase(Locale.ROOT);
        if(!seen.add(key))return;
        list.add(new Fact(area,label,value,source,url,limitation,publication,review));
    }
    private static final class Glyph extends View {
        final String type;
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        Glyph(Activity a,String type){super(a);this.type=type;
            setContentDescription(type+" icon");setImportantForAccessibility(
                View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            float unit=Math.min(getWidth(),getHeight())/32f;
            canvas.save();
            canvas.translate((getWidth()-32*unit)/2f,(getHeight()-32*unit)/2f);
            canvas.scale(unit,unit);
            paint.setColor(Color.rgb(235,244,235));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(0,0,32,32,11,11,paint);
            paint.setColor(UiKit.FOREST);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.9f);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            switch(type){
                case "search":
                    canvas.drawCircle(14,14,6,paint);
                    canvas.drawLine(18.8f,18.8f,24,24,paint);break;
                case "leaf":
                    Path leaf=new Path();
                    leaf.moveTo(8,22);leaf.cubicTo(5,12,14,7,25,7);
                    leaf.cubicTo(25,20,18,27,8,22);
                    canvas.drawPath(leaf,paint);
                    canvas.drawLine(9,22,21,12,paint);break;
                case "pin":
                    Path pin=new Path();
                    pin.moveTo(16,27);pin.cubicTo(11,20,7,17,9,11);
                    pin.cubicTo(12,2,23,4,24,12);
                    pin.cubicTo(24,17,20,23,16,27);
                    canvas.drawPath(pin,paint);canvas.drawCircle(16,13,3,paint);break;
                case "tracks":
                    canvas.drawOval(10,16,19,24,paint);
                    canvas.drawCircle(9,13,1.8f,paint);
                    canvas.drawCircle(14,10,1.8f,paint);
                    canvas.drawCircle(19,11,1.8f,paint);
                    canvas.drawCircle(23,15,1.8f,paint);break;
                case "shield":
                    Path shield=new Path();shield.moveTo(16,5);
                    shield.lineTo(25,9);shield.lineTo(23,20);
                    shield.quadTo(20,25,16,27);shield.quadTo(9,24,8,20);
                    shield.lineTo(7,9);shield.close();canvas.drawPath(shield,paint);
                    canvas.drawLine(12,16,15,19,paint);
                    canvas.drawLine(15,19,21,12,paint);break;
                default:
                    canvas.drawRoundRect(7,7,25,25,3,3,paint);
                    canvas.drawLine(11,12,21,12,paint);
                    canvas.drawLine(11,17,21,17,paint);
                    canvas.drawLine(11,22,18,22,paint);
            }
            canvas.restore();
        }
    }
    private static void addFact(Activity a,LinearLayout list,Fact item,
            ArrayList<Fact> referenced){
        LinearLayout block=new LinearLayout(a);block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dp(a,3),dp(a,7),dp(a,3),dp(a,8));
        TextView heading=text(a,item.label,13,true,UiKit.FOREST);
        block.addView(heading);
        TextView description=text(a,item.value,14,false,UiKit.INK);
        description.setLineSpacing(dp(a,2),1.02f);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(a,3);block.addView(description,lp);
        list.addView(block);
        referenced.add(item);
    }
    private static void section(Activity a,LinearLayout parent,String area,
            String icon,ArrayList<Fact> all,ArrayList<Fact> references){
        ArrayList<Fact> available=new ArrayList<>();
        for(Fact item:all)if(area.equals(item.area))available.add(item);
        if(available.isEmpty())return;
        LinearLayout card=new LinearLayout(a);card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(a,13),dp(a,13),dp(a,13),dp(a,12));
        card.setBackground(UiKit.surface(a));
        LinearLayout head=new LinearLayout(a);head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        Glyph glyph=new Glyph(a,icon);
        head.addView(glyph,new LinearLayout.LayoutParams(dp(a,36),dp(a,36)));
        TextView title=text(a,area,17,true,UiKit.INK);
        LinearLayout.LayoutParams titleLp=new LinearLayout.LayoutParams(-1,-2);
        titleLp.leftMargin=dp(a,11);head.addView(title,titleLp);
        card.addView(head);
        LinearLayout details=new LinearLayout(a);
        details.setOrientation(LinearLayout.VERTICAL);
        for(int i=0;i<Math.min(2,available.size());i++)
            addFact(a,details,available.get(i),references);
        card.addView(details);
        if(available.size()>2){
            LinearLayout more=new LinearLayout(a);
            more.setOrientation(LinearLayout.VERTICAL);
            more.setVisibility(View.GONE);
            for(int i=2;i<available.size();i++)addFact(a,more,available.get(i),references);
            Button toggle=UiKit.button(a,"Show "+(available.size()-2)+" more","quiet");
            toggle.setContentDescription("Expand "+area.toLowerCase(Locale.ROOT)+" research notes");
            card.addView(toggle,new LinearLayout.LayoutParams(-1,dp(a,45)));
            card.addView(more);
            toggle.setOnClickListener(v->{
                boolean open=more.getVisibility()!=View.VISIBLE;
                more.setVisibility(open?View.VISIBLE:View.GONE);
                toggle.setText(open?"Show fewer":"Show "+(available.size()-2)+" more");
            });
        }
        LinearLayout.LayoutParams cardLp=new LinearLayout.LayoutParams(-1,-2);
        cardLp.bottomMargin=dp(a,11);parent.addView(card,cardLp);
    }
    private static void sourceDrawer(Activity a,LinearLayout parent,String scientific,
            String citation,String sourceUrl,String sourceKey,ArrayList<Fact> facts){
        LinearLayout references=new LinearLayout(a);
        references.setOrientation(LinearLayout.VERTICAL);
        references.setVisibility(View.GONE);
        references.setPadding(dp(a,9),dp(a,6),dp(a,9),dp(a,12));
        references.addView(text(a,"References · specialist review in progress",
            12,false,UiKit.MUTED));
        HashSet<String> unique=new HashSet<>();
        if(!citation.isEmpty()){
            references.addView(text(a,"Taxonomy · "+citation,12,false,UiKit.MUTED));
            if(sourceUrl.startsWith("https://"))link(a,references,"Open checklist",sourceUrl);
        }
        for(Fact fact:facts){
            if(!unique.add(fact.url))continue;
            TextView source=text(a,fact.source,13,true,UiKit.INK);
            LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);
            sp.topMargin=dp(a,12);references.addView(source,sp);
            link(a,references,"View publication",fact.url);
        }
        if(!sourceKey.isEmpty()){
            TextView provenance=text(a,"Catalog record: "+sourceKey,11,false,UiKit.MUTED);
            LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,-2);
            mp.topMargin=dp(a,15);references.addView(provenance,mp);
        }
        if(!facts.isEmpty()){
            TextView notes=text(a,"Evidence notes",13,true,UiKit.FOREST);
            LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);
            np.topMargin=dp(a,15);references.addView(notes,np);
            for(Fact fact:facts){
                if(fact.limitation.isEmpty()&&fact.review.isEmpty())continue;
                String text=fact.label+" · "+fact.publication;
                if(!fact.limitation.isEmpty())text+="\n"+fact.limitation;

                TextView detail=text(a,text,12,false,UiKit.MUTED);
                LinearLayout.LayoutParams d=new LinearLayout.LayoutParams(-1,-2);
                d.topMargin=dp(a,10);references.addView(detail,d);
            }
        }
        Button toggle=UiKit.button(a,"Sources & details  ▾","secondary");
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,50));
        lp.topMargin=dp(a,4);parent.addView(toggle,lp);
        parent.addView(references);
        toggle.setOnClickListener(v->{
            boolean open=references.getVisibility()!=View.VISIBLE;
            references.setVisibility(open?View.VISIBLE:View.GONE);
            toggle.setText(open?"Hide sources & details  ▴":"Sources & details  ▾");
        });
    }
    private static void link(Activity a,LinearLayout parent,String label,String url){
        Button button=UiKit.button(a,label,"quiet");
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,44));
        lp.topMargin=dp(a,4);parent.addView(button,lp);
        button.setOnClickListener(v->a.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))));
    }
    public static void add(Activity a,LinearLayout parent,Cursor taxon,String scientific){
        String group=column(taxon,"taxon_group");
        String family=column(taxon,"family");
        String subtitle=group+(family.isEmpty()?"":" · "+family);
        TextView meta=text(a,subtitle,13,false,UiKit.MUTED);
        LinearLayout.LayoutParams metaLp=new LinearLayout.LayoutParams(-1,-2);
        metaLp.bottomMargin=dp(a,11);parent.addView(meta,metaLp);
        OfflinePhotoEvidence.addCompactForTaxon(a,parent,scientific);
        ArrayList<Fact> facts=facts(a,scientific);
        if(!facts.isEmpty()){
            TextView heading=text(a,"Field reference",13,true,UiKit.FOREST);
            LinearLayout.LayoutParams t=new LinearLayout.LayoutParams(-1,-2);
            t.topMargin=dp(a,15);t.bottomMargin=dp(a,11);
            parent.addView(heading,t);
        }
        ArrayList<Fact> displayed=new ArrayList<>();
        for(int i=0;i<AREAS.length;i++)
            section(a,parent,AREAS[i],ICONS[i],facts,displayed);
        sourceDrawer(a,parent,scientific,
            column(taxon,"source")+" · "+column(taxon,"source_version"),
            column(taxon,"source_url"),column(taxon,"taxon_key"),facts);
        LinearLayout utilities=new LinearLayout(a);
        utilities.setOrientation(LinearLayout.VERTICAL);
        utilities.setPadding(0,dp(a,8),0,0);
        Button compare=UiKit.button(a,"Compare species traits","quiet");
        utilities.addView(compare,new LinearLayout.LayoutParams(-1,dp(a,48)));
        compare.setOnClickListener(v->a.startActivity(
            new Intent(a,Rc8TraitComparisonActivity.class)
                .putExtra("scientific_name",scientific)));
        if(OfflinePhotoEvidence.hasOfflineTaxon(a,scientific)){
            Button photos=UiKit.button(a,"Photo comparison","quiet");
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(a,48));
            p.topMargin=dp(a,5);utilities.addView(photos,p);
            photos.setOnClickListener(v->a.startActivity(
                new Intent(a,Rc8PhotoComparisonActivity.class)
                    .putExtra("scientific_name",scientific)));
        }
        parent.addView(utilities);
    }
}
