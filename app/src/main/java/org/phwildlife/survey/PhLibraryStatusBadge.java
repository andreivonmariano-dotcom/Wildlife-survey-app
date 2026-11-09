package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.TextView;

/** Compact text-and-symbol labels; rarity is never conflated with conservation risk. */
public final class PhLibraryStatusBadge {
    private PhLibraryStatusBadge(){}
    public static String status(String code){
        if("LC".equals(code))return "Least Concern";
        if("NT".equals(code))return "Near Threatened";
        if("VU".equals(code))return "Vulnerable";
        if("EN".equals(code))return "Endangered";
        if("CR".equals(code))return "Critically Endangered";
        if("DD".equals(code))return "Data Deficient";
        if("NE".equals(code))return "Not Evaluated";
        return "Unassessed";
    }
    public static TextView make(Activity a,String value,boolean rarity){
        String name=rarity?value:status(value);
        int bg=0xFFE2ECE4,fg=0xFF245344,stroke=0xFFBBD0BE;
        String icon="●";
        if(rarity){
            switch(name){
                case "Uncommon":bg=0xFFDFEBDD;fg=0xFF3F5630;stroke=0xFFA9BF9C;icon="◆";break;
                case "Rare":bg=0xFFDAE8F4;fg=0xFF214F7E;stroke=0xFFAAC5DF;icon="✦";break;
                case "Exceptionally rare":bg=0xFFFFEDD3;fg=0xFF81521B;stroke=0xFFE7BC7C;icon="✧";break;
                case "Legendary":bg=0xFF54236F;fg=Color.WHITE;stroke=0xFFE2B95C;icon="★";break;
                case "Unranked":bg=0xFFE8E9E8;fg=0xFF4C5350;stroke=0xFFBEC4C0;icon="○";break;
                default:break;
            }
        }else{
            switch(value){
                case "NT":bg=0xFFF2EFCB;fg=0xFF655D1D;stroke=0xFFD8D094;break;
                case "VU":bg=0xFFFFE5B8;fg=0xFF70451B;stroke=0xFFEBC182;icon="▲";break;
                case "EN":bg=0xFFF8D4B8;fg=0xFF863F23;stroke=0xFFE9AA8A;icon="▲";break;
                case "CR":bg=0xFF7A2431;fg=Color.WHITE;stroke=0xFFAB5E68;icon="!";break;
                case "DD":bg=0xFFDBE4ED;fg=0xFF3B546E;stroke=0xFFB5C8D8;icon="?";break;
                case "NE":case "Unassessed":bg=0xFFE8E9E8;fg=0xFF4C5350;stroke=0xFFBEC4C0;icon="○";break;
                default:break;
            }
        }
        TextView t=PhLibrary2Ui.text(a,icon+"  "+name,13,fg,false,true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(PhLibrary2Ui.dp(a,11),PhLibrary2Ui.dp(a,9),
            PhLibrary2Ui.dp(a,11),PhLibrary2Ui.dp(a,9));
        t.setMinHeight(PhLibrary2Ui.dp(a,43));
        GradientDrawable d=PhLibrary2Ui.bg(a,bg,stroke,20);
        t.setBackground(d);
        final String intro=rarity?
            "Rarity is an educational book category, not an IUCN or DENR assessment.":
            "IUCN global category shown in the original reference account. This is not a Philippine national threat assessment.";
        t.setContentDescription((rarity?"Rarity: ":"IUCN global status: ")+name+". Tap for explanation.");
        t.setClickable(true);t.setFocusable(true);
        t.setOnClickListener(v->new AlertDialog.Builder(a)
            .setTitle((rarity?"Rarity":"IUCN global status")+" · "+name)
            .setMessage(intro).setPositiveButton("Close",null).show());
        return t;
    }
}
