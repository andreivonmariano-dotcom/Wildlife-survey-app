package org.phwildlife.survey;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import java.text.BreakIterator;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;

/** A date-stable, source-backed daily field-guide feature. All media is bundled offline. */
public final class ApoSpeciesOfDay {
    private ApoSpeciesOfDay(){}
    private static int dp(Activity a,float n){return PhLibrary2Ui.dp(a,n);}
    private static final ZoneId PH_TIME=ZoneId.of("Asia/Manila");
    private static final int PALE=0xFFE8EEDC;

    /** Endemicity comes only from an explicit source badge, not an inferred range. */
    public static String endemicity(JSONObject account){
        String badge=account==null?"":account.optString("b","");
        if(badge.contains("Philippine endemic"))return "Philippine endemic";
        if(badge.contains("Mindanao endemic"))return "Mindanao endemic";
        return "Not specified";
    }
    /** Preserve a complete sentence from the canonical account, citations included. */
    public static String firstSourceSentence(String paragraph){
        if(paragraph==null||paragraph.trim().isEmpty())return "";
        String clean=paragraph.trim();
        BreakIterator iterator=BreakIterator.getSentenceInstance(Locale.ENGLISH);
        iterator.setText(clean);
        int end=iterator.first();
        end=iterator.next();
        if(end==BreakIterator.DONE||end<12)return clean;
        return clean.substring(0,end).trim();
    }
    /** One reproducible selection per Philippine day; never uses field observations. */
    public static JSONObject select(Activity a,LocalDate date){
        if(!PhLibraryBook01.ready(a))return null;
        ArrayList<JSONObject> candidate=new ArrayList<>();
        for(JSONObject row:PhLibraryBook01.entries(a)){
            String name=row.optString("s");
            if(name.trim().isEmpty()||!PhLibrarySpeciesMedia.hasApprovedPhoto(a,name))continue;
            if(firstSourceSentence(row.optString("p")).isEmpty())continue;
            candidate.add(row);
        }
        if(candidate.isEmpty())return null;
        candidate.sort((one,two)->one.optString("s").compareToIgnoreCase(two.optString("s")));
        int index=(int)Math.floorMod(date.toEpochDay(),candidate.size());
        return candidate.get(index);
    }
    private static TextView smallLabel(Activity a,String value,int color){
        TextView v=PhLibrary2Ui.text(a,value,12,color,false,true);
        v.setLetterSpacing(.06f);
        return v;
    }
    /** Compact Home teaser; detailed facts and citations stay in the source account. */
    public static View createCompact(Activity a,Consumer<String> open){
        JSONObject account=select(a,LocalDate.now(PH_TIME));
        if(account==null)return null;
        String latin=account.optString("s");
        String common=account.optString("c",latin);
        String iucn=account.isNull("iucn")?"Unassessed":
            PhLibraryStatusBadge.status(account.optString("iucn","Unassessed"));
        LinearLayout row=new LinearLayout(a);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(a,10),dp(a,10),dp(a,12),dp(a,10));
        row.setBackground(PhLibrary2Ui.touch(a,UiKit.BG,UiKit.BORDER,16));
        View cover=PhLibrarySpeciesMedia.photoOrPlaceholder(a,latin);
        if(cover instanceof ImageView)
            ((ImageView)cover).setScaleType(ImageView.ScaleType.CENTER_CROP);
        FrameLayout image=new FrameLayout(a);
        image.setBackgroundColor(PhLibrary2Ui.DEEP);
        image.setClipToOutline(true);
        image.addView(cover,new FrameLayout.LayoutParams(-1,-1));
        row.addView(image,new LinearLayout.LayoutParams(dp(a,88),dp(a,88)));
        LinearLayout labels=new LinearLayout(a);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(a,12),0,0,0);
        labels.addView(PhLibrary2Ui.text(a,"SPECIES OF THE DAY",11,
            UiKit.MUTED,false,true));
        TextView name=PhLibrary2Ui.text(a,common,17,UiKit.INK,true,false);
        name.setMaxLines(2);
        LinearLayout.LayoutParams n=new LinearLayout.LayoutParams(-1,-2);
        n.topMargin=dp(a,5);labels.addView(name,n);
        TextView scientific=PhLibrary2Ui.text(a,latin,13,UiKit.MUTED,false,false);
        scientific.setTypeface(Typeface.create("serif",Typeface.ITALIC));
        labels.addView(scientific);
        TextView context=PhLibrary2Ui.text(a,"IUCN global: "+iucn,12,
            UiKit.FOREST,false,false);
        LinearLayout.LayoutParams c=new LinearLayout.LayoutParams(-1,-2);
        c.topMargin=dp(a,5);labels.addView(context,c);
        row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
        row.setMinimumHeight(dp(a,108));
        row.setFocusable(true);row.setClickable(true);
        row.setContentDescription("Species of the day: "+common+", "+latin+
            ". IUCN global: "+iucn+". Open full account.");
        row.setOnClickListener(v->open.accept(latin));
        return row;
    }
    public static View create(Activity a,Consumer<String> open){
        LocalDate today=LocalDate.now(PH_TIME);
        JSONObject row=select(a,today);
        if(row==null)return null;
        String latin=row.optString("s"),common=row.optString("c",latin);
        String rarity=row.optString("t","Unranked");
        String status=row.isNull("iucn")?"Unassessed":row.optString("iucn","Unassessed");
        String endemic=endemicity(row);
        String fact=firstSourceSentence(row.optString("p"));
        boolean stack=a.getResources().getConfiguration().fontScale>=1.5f ||
            a.getResources().getConfiguration().screenWidthDp<350;
        String day=today.format(DateTimeFormatter.ofPattern("dd MMM yyyy",Locale.ENGLISH))
            .toUpperCase(Locale.ENGLISH);

        LinearLayout card=new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(PhLibrary2Ui.touch(a,PhLibrary2Ui.IVORY,PhLibrary2Ui.LINE,20));
        card.setClipToOutline(true);
        card.setTag("apo_species_of_the_day");

        LinearLayout header=new LinearLayout(a);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackgroundColor(PhLibrary2Ui.DEEP);
        header.setPadding(dp(a,18),dp(a,16),dp(a,18),dp(a,17));
        LinearLayout upper=new LinearLayout(a);
        upper.setOrientation(stack?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        TextView eyebrow=smallLabel(a,"TODAY IN THE FIELD GUIDE",PALE);
        upper.addView(eyebrow,new LinearLayout.LayoutParams(stack?-1:0,-2,1f));
        TextView date=PhLibrary2Ui.text(a,day,12,PALE,false,true);
        LinearLayout.LayoutParams dateLp=new LinearLayout.LayoutParams(-2,-2);
        if(stack)dateLp.topMargin=dp(a,5);
        upper.addView(date,dateLp);
        header.addView(upper);
        TextView title=PhLibrary2Ui.text(a,common,stack?25:29,Color.WHITE,true,false);
        title.setLineSpacing(dp(a,1),1.02f);
        LinearLayout.LayoutParams titleLp=new LinearLayout.LayoutParams(-1,-2);
        titleLp.topMargin=dp(a,11);header.addView(title,titleLp);
        TextView latinLabel=PhLibrary2Ui.text(a,latin,16,PALE,true,false);
        latinLabel.setTypeface(Typeface.create("serif",Typeface.ITALIC));
        LinearLayout.LayoutParams latinLp=new LinearLayout.LayoutParams(-1,-2);
        latinLp.topMargin=dp(a,5);header.addView(latinLabel,latinLp);
        card.addView(header);

        View photo=PhLibrarySpeciesMedia.photoOrPlaceholder(a,latin);
        if(photo instanceof ImageView)
            ((ImageView)photo).setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout imageFrame=new FrameLayout(a);
        imageFrame.setBackgroundColor(PhLibrary2Ui.DEEP);
        imageFrame.addView(photo,new FrameLayout.LayoutParams(-1,-1));
        card.addView(imageFrame,new LinearLayout.LayoutParams(-1,dp(a,stack?222:238)));

        LinearLayout body=new LinearLayout(a);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(a,18),dp(a,15),dp(a,18),dp(a,17));
        String group=row.optString("g","");
        String order=row.optString("o","");
        TextView taxonomy=smallLabel(a,
            (group.isEmpty()?"WILDLIFE":group.toUpperCase(Locale.ENGLISH))+
                (order.isEmpty()?"":"  ·  "+order.toUpperCase(Locale.ENGLISH)),
            PhLibrary2Ui.FOREST);
        body.addView(taxonomy);
        TextView sourceTitle=smallLabel(a,"FROM THE SPECIES ACCOUNT",PhLibrary2Ui.MUTED);
        LinearLayout.LayoutParams sourceLp=new LinearLayout.LayoutParams(-1,-2);
        sourceLp.topMargin=dp(a,12);body.addView(sourceTitle,sourceLp);
        TextView factView=PhLibrary2Ui.text(a,fact,16,PhLibrary2Ui.INK,false,false);
        factView.setLineSpacing(dp(a,3),1.12f);
        LinearLayout.LayoutParams factLp=new LinearLayout.LayoutParams(-1,-2);
        factLp.topMargin=dp(a,6);body.addView(factView,factLp);

        LinearLayout statuses=new LinearLayout(a);
        statuses.setOrientation(stack?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams statusLp=new LinearLayout.LayoutParams(-1,-2);
        statusLp.topMargin=dp(a,15);body.addView(statuses,statusLp);
        LinearLayout rarityColumn=new LinearLayout(a);
        rarityColumn.setOrientation(LinearLayout.VERTICAL);
        rarityColumn.addView(smallLabel(a,"RARITY",PhLibrary2Ui.MUTED));
        TextView rarityBadge=PhLibraryStatusBadge.make(a,rarity,true);
        rarityBadge.setClickable(false);rarityBadge.setFocusable(false);
        rarityBadge.setTextSize(12);
        LinearLayout.LayoutParams rarityBadgeLp=new LinearLayout.LayoutParams(-1,-2);
        rarityBadgeLp.topMargin=dp(a,5);rarityColumn.addView(rarityBadge,rarityBadgeLp);
        statuses.addView(rarityColumn,new LinearLayout.LayoutParams(stack?-1:0,-2,1f));
        LinearLayout globalColumn=new LinearLayout(a);
        globalColumn.setOrientation(LinearLayout.VERTICAL);
        globalColumn.addView(smallLabel(a,"IUCN GLOBAL STATUS",PhLibrary2Ui.MUTED));
        TextView globalBadge=PhLibraryStatusBadge.make(a,status,false);
        globalBadge.setClickable(false);globalBadge.setFocusable(false);
        globalBadge.setTextSize(12);
        LinearLayout.LayoutParams globalBadgeLp=new LinearLayout.LayoutParams(-1,-2);
        globalBadgeLp.topMargin=dp(a,5);globalColumn.addView(globalBadge,globalBadgeLp);
        LinearLayout.LayoutParams globalLp=new LinearLayout.LayoutParams(stack?-1:0,-2,1f);
        if(stack)globalLp.topMargin=dp(a,10);else globalLp.leftMargin=dp(a,9);
        statuses.addView(globalColumn,globalLp);

        TextView endemicityText=PhLibrary2Ui.text(a,
            "Endemicity: "+endemic,14,PhLibrary2Ui.FOREST,false,true);
        LinearLayout.LayoutParams endLp=new LinearLayout.LayoutParams(-1,-2);
        endLp.topMargin=dp(a,15);body.addView(endemicityText,endLp);
        int images=PhLibraryDriveGallery.photoCount(a,latin);
        if(images>1){
            TextView album=PhLibrary2Ui.text(a,
                images+" photographs available in the species account",
                13,PhLibrary2Ui.MUTED,false,false);
            LinearLayout.LayoutParams albumLp=new LinearLayout.LayoutParams(-1,-2);
            albumLp.topMargin=dp(a,7);body.addView(album,albumLp);
        }
        View rule=new View(a);
        rule.setBackgroundColor(PhLibrary2Ui.LINE);
        LinearLayout.LayoutParams ruleLp=new LinearLayout.LayoutParams(-1,dp(a,1));
        ruleLp.topMargin=dp(a,15);body.addView(rule,ruleLp);
        TextView link=PhLibrary2Ui.text(a,"READ THE SPECIES ACCOUNT  ›",
            14,PhLibrary2Ui.FOREST,false,true);
        link.setGravity(Gravity.CENTER_VERTICAL);
        link.setMinHeight(dp(a,48));
        link.setPadding(0,dp(a,11),0,0);
        body.addView(link);
        card.addView(body);

        card.setFocusable(true);card.setClickable(true);
        card.setContentDescription("Species of the day. "+common+", "+latin+
            ". Rarity: "+rarity+". IUCN global status: "+
            PhLibraryStatusBadge.status(status)+". Endemicity: "+endemic+
            ". "+fact+". Open species account.");
        card.setOnClickListener(v->open.accept(latin));
        return card;
    }
}
