package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import java.text.BreakIterator;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;

/** Deterministic offline daily spotlight using source-backed taxa without photographs. */
public final class ApoSpeciesOfDay {
    private ApoSpeciesOfDay(){}
    private static int dp(Activity a,float n){return PhLibrary2Ui.dp(a,n);}
    private static final ZoneId PH_TIME=ZoneId.of("Asia/Manila");

    /** Based only on the account's explicit checklist badge, never inferred from range. */
    public static String endemicity(JSONObject account){
        String badge=account==null?"":account.optString("b","");
        if(badge.contains("Philippine endemic"))return "Philippine endemic";
        if(badge.contains("Mindanao endemic"))return "Mindanao endemic";
        return "Not specified";
    }
    /** Preserve a complete sentence from the canonical paragraph, including its citations. */
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
    /** One constant selection per Philippine calendar day; no internet or local survey data. */
    public static JSONObject select(Activity a,LocalDate date){
        if(!PhLibraryBook01.ready(a))return null;
        ArrayList<JSONObject> candidate=new ArrayList<>();
        for(JSONObject row:PhLibraryBook01.entries(a)){
            String name=row.optString("s");
            if(name.trim().isEmpty())continue;
            if(firstSourceSentence(row.optString("p")).isEmpty())continue;
            candidate.add(row);
        }
        if(candidate.isEmpty())return null;
        candidate.sort((one,two)->one.optString("s").compareToIgnoreCase(two.optString("s")));
        int index=(int)Math.floorMod(date.toEpochDay(),candidate.size());
        return candidate.get(index);
    }
    public static View create(Activity a,Consumer<String> open){
        JSONObject row=select(a,LocalDate.now(PH_TIME));
        if(row==null)return null;
        String latin=row.optString("s"),common=row.optString("c",latin);
        String rarity=row.optString("t","Unranked");
        String status=row.isNull("iucn")?"Unassessed":row.optString("iucn","Unassessed");
        String endemic=endemicity(row);
        String fact=firstSourceSentence(row.optString("p"));
        boolean stacked=a.getResources().getConfiguration().fontScale>=1.6f ||
            a.getResources().getConfiguration().screenWidthDp<350;

        LinearLayout card=new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(a,15),dp(a,16),dp(a,15),dp(a,16));
        card.setBackground(PhLibrary2Ui.touch(a,PhLibrary2Ui.IVORY,PhLibrary2Ui.LINE,17));
        card.setTag("apo_species_of_the_day");
        TextView eyebrow=PhLibrary2Ui.text(a,"SPECIES OF THE DAY",11,
            PhLibrary2Ui.FOREST,false,true);
        eyebrow.setLetterSpacing(.1f);
        card.addView(eyebrow);
        LinearLayout head=new LinearLayout(a);
        head.setOrientation(stacked?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);
        hp.topMargin=dp(a,10);card.addView(head,hp);
        LinearLayout names=new LinearLayout(a);names.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(stacked?-1:0,-2,
            stacked?0:1f);
        if(stacked)np.topMargin=dp(a,11);
        head.addView(names,np);
        TextView title=PhLibrary2Ui.text(a,common,stacked?19:20,PhLibrary2Ui.INK,true,false);
        names.addView(title);
        TextView scientific=PhLibrary2Ui.text(a,latin,13,PhLibrary2Ui.MUTED,true,false);
        scientific.setTypeface(Typeface.create("serif",Typeface.ITALIC));
        LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,-2);
        slp.topMargin=dp(a,4);names.addView(scientific,slp);
        TextView factText=PhLibrary2Ui.text(a,fact,13,PhLibrary2Ui.INK,false,false);
        factText.setLineSpacing(dp(a,2),1.08f);
        LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2);
        fp.topMargin=dp(a,12);card.addView(factText,fp);
        LinearLayout badges=new LinearLayout(a);
        badges.setOrientation(stacked?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);
        bp.topMargin=dp(a,11);card.addView(badges,bp);
        TextView rank=PhLibraryStatusBadge.make(a,rarity,true);
        TextView global=PhLibraryStatusBadge.make(a,status,false);
        rank.setClickable(false);rank.setFocusable(false);
        global.setClickable(false);global.setFocusable(false);
        rank.setTextSize(11);global.setTextSize(11);
        badges.addView(rank,new LinearLayout.LayoutParams(stacked?-1:0,-2,1));
        LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(stacked?-1:0,-2,1);
        if(stacked)gp.topMargin=dp(a,5);else gp.leftMargin=dp(a,7);
        badges.addView(global,gp);
        TextView end=PhLibrary2Ui.text(a,"Endemicity · "+endemic,12,
            PhLibrary2Ui.MUTED,false,true);
        LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,-2);
        ep.topMargin=dp(a,12);card.addView(end,ep);

        card.setFocusable(true);card.setClickable(true);
        card.setContentDescription("Species of the day. "+common+", "+latin+
            ". Rarity: "+rarity+". IUCN global status: "+
            PhLibraryStatusBadge.status(status)+". Endemicity: "+endemic+
            ". "+fact+". Open species account.");
        card.setOnClickListener(v->open.accept(latin));
        return card;
    }
}
