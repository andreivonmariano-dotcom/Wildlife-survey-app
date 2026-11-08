package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** PHLibrary: one featured encyclopedia and three distinct discovery actions.
 *  Text only. The actual Book 01 profiles remain read-only. */
public final class LikasLibraryActivity extends Activity {
    private static final String[] GROUPS={"Birds","Amphibians","Reptiles","Mammals"};
    private static final String[] TIERS={"Common","Uncommon","Rare","Exceptionally rare","Legendary","Unranked"};
    private int dp(float value){return PhLibraryStyle.dp(this,value);}
    private void add(LinearLayout parent,String title,String subtitle,Runnable action){
        View item=PhLibraryStyle.row(this,title,subtitle,action);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(8);
        parent.addView(item,lp);
    }
    private void openBook(String filter,String value){
        Intent intent=new Intent(this,PhLibraryDirectoryActivity.class);
        if(filter!=null && value!=null)intent.putExtra(filter,value);
        startActivity(intent);
    }
    private void chooseGroup(){
        new AlertDialog.Builder(this).setTitle("Browse wildlife groups")
            .setItems(GROUPS,(dialog,index)->openBook(
                "phlibrary_group",GROUPS[index]))
            .setNegativeButton("Close",null).show();
    }
    private void chooseRarity(){
        new AlertDialog.Builder(this).setTitle("Discover by rarity")
            .setMessage("Educational rarity categories; provisional identifications remain unranked.")
            .setItems(TIERS,(dialog,index)->openBook(
                "phlibrary_rarity",TIERS[index]))
            .setNegativeButton("Close",null).show();
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout body=new LinearLayout(this);
        LinearLayout root=UiKit.page(this,"PHLibrary","Home",this::finish,body);
        LinearLayout nav=(LinearLayout)root.getChildAt(0);
        android.widget.Button back=(android.widget.Button)nav.getChildAt(0);
        back.setText("Back");
        back.setLayoutParams(new LinearLayout.LayoutParams(dp(
            getResources().getConfiguration().fontScale>=1.6f?104:68),dp(48)));
        ScrollView scroll=(ScrollView)root.getChildAt(1);
        scroll.setPadding(0,0,dp(7),0);
        scroll.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);

        TextView preface=PhLibraryStyle.text(this,"PHILIPPINE BIODIVERSITY LIBRARY",
            11,UiKit.MUTED,true);
        preface.setLetterSpacing(.13f);
        preface.setPadding(dp(2),dp(13),0,dp(13));
        if(getResources().getConfiguration().fontScale<1.6f)
            body.addView(preface);

        body.addView(PhLibraryStyle.bookCover(this,()->openBook(null,null)),
            new LinearLayout.LayoutParams(-1,-2));

        body.addView(PhLibraryStyle.section(this,"EXPLORE THE VOLUME"));
        add(body,"Browse wildlife groups",
            "Birds · Amphibians · Reptiles · Mammals",this::chooseGroup);
        add(body,"Discovery Rarity",
            "Explore the five rarity tiers and provisional entries",this::chooseRarity);
        add(body,"Habitat associations",
            "Explore sourced habitat and species links",()->
            startActivity(new Intent(this,HabitatSpeciesEvidenceActivity.class)));

        setContentView(root);
    }
}
