package org.phwildlife.survey;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.Arrays;
import java.util.List;

/** Full, readable selected method and checklist region at Android 200% text. */
public final class ApoSpinnerAdapter extends ArrayAdapter<String> {
    private final Activity activity;
    public ApoSpinnerAdapter(Activity activity,String[] values){
        this(activity,Arrays.asList(values));
    }
    public ApoSpinnerAdapter(Activity activity,List<String> values){
        super(activity,android.R.layout.simple_spinner_item,values);
        this.activity=activity;
    }
    private int dp(int n){return Math.round(n*activity.getResources().getDisplayMetrics().density);}
    private View line(int position,View reused){
        TextView label=reused instanceof TextView?(TextView)reused:new TextView(activity);
        label.setText(getItem(position));
        label.setTextSize(15);
        label.setTextColor(UiKit.INK);
        label.setGravity(Gravity.CENTER_VERTICAL);
        label.setMaxLines(4);
        label.setSingleLine(false);
        label.setEllipsize(null);
        label.setMinHeight(dp(56));
        label.setPadding(dp(12),dp(8),dp(12),dp(8));
        return label;
    }
    @Override public View getView(int position,View convertView,ViewGroup parent){
        return line(position,convertView);
    }
    @Override public View getDropDownView(int position,View convertView,ViewGroup parent){
        return line(position,convertView);
    }
}
