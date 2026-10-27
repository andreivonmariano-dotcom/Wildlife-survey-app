package org.phwildlife.survey;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

/** Small shared visual system for the offline survey application. */
public final class UiKit {
  private UiKit(){}
  public static final int BG=Color.rgb(246,249,246);
  public static final int INK=Color.rgb(13,49,39);
  public static final int FOREST=Color.rgb(28,91,62);
  public static final int MUTED=Color.rgb(97,116,104);
  public static final int BLUE=Color.rgb(40,88,137);
  public static final int AMBER=Color.rgb(153,92,28);
  public static final int BORDER=Color.rgb(216,230,218);
  private static int dp(Activity a,int n){return (int)(a.getResources().getDisplayMetrics().density*n+0.5);}
  public static TextView title(Activity a,String title) {
    TextView t=new TextView(a);t.setText(title);t.setTextSize(23);t.setTypeface(null,Typeface.BOLD);
    t.setTextColor(INK);t.setPadding(dp(a,3),dp(a,9),dp(a,3),dp(a,7));return t;
  }
  public static Button button(Activity a,String title,String type) {
    int fill=FOREST,stroke=FOREST,foreground=Color.WHITE;
    switch(type){
      case "secondary":fill=Color.WHITE;stroke=BORDER;foreground=INK;break;
      case "camera":fill=BLUE;stroke=BLUE;break;
      case "warning":fill=AMBER;stroke=AMBER;break;
      case "quiet":fill=BG;stroke=BORDER;foreground=FOREST;break;
      case "danger":fill=Color.rgb(166,53,48);stroke=fill;break;
    }
    Button b=new Button(a);b.setAllCaps(false);b.setText(title);b.setTextSize(15);b.setTypeface(null,Typeface.BOLD);
    b.setTextColor(foreground);b.setMinHeight(dp(a,50));b.setPadding(dp(a,10),dp(a,5),dp(a,10),dp(a,5));
    GradientDrawable shape=new GradientDrawable();shape.setColor(fill);shape.setStroke(dp(a,1),stroke);shape.setCornerRadius(dp(a,13));
    b.setBackground(shape);b.setBackgroundTintList(null);
    return b;
  }
  public static GradientDrawable surface(Activity a) {
    GradientDrawable d=new GradientDrawable();d.setColor(Color.WHITE);
    d.setStroke(dp(a,1),BORDER);d.setCornerRadius(dp(a,15));return d;
  }
}
