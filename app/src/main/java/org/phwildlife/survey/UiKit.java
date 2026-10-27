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
  // Field palette pairs with the LIKAS pond frog icon and stays legible in sunlight.
  public static final int BG=Color.rgb(245,247,240);
  public static final int INK=Color.rgb(21,54,41);
  public static final int FOREST=Color.rgb(35,100,71);
  public static final int POND=Color.rgb(51,126,115);
  public static final int MUTED=Color.rgb(88,111,101);
  public static final int BLUE=Color.rgb(40,88,137);
  public static final int AMBER=Color.rgb(164,102,44);
  public static final int BORDER=Color.rgb(215,227,215);
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
      case "outline-danger":fill=Color.WHITE;stroke=Color.rgb(181,77,61);
          foreground=Color.rgb(154,51,42);break;
      case "pond":fill=POND;stroke=POND;break;
    }
    Button b=new Button(a);b.setAllCaps(false);b.setText(title);b.setTextSize(15);b.setTypeface(null,Typeface.BOLD);
    b.setTextColor(foreground);b.setMinHeight(dp(a,48));b.setMinimumWidth(dp(a,48));
    b.setGravity(Gravity.CENTER);b.setPadding(dp(a,9),dp(a,7),dp(a,9),dp(a,7));
    GradientDrawable shape=new GradientDrawable();shape.setColor(fill);shape.setStroke(dp(a,1),stroke);shape.setCornerRadius(dp(a,14));
    b.setBackground(shape);b.setBackgroundTintList(null);
    return b;
  }
  public static GradientDrawable surface(Activity a) {
    GradientDrawable d=new GradientDrawable();d.setColor(Color.WHITE);
    d.setStroke(dp(a,1),BORDER);d.setCornerRadius(dp(a,16));return d;
  }
}
