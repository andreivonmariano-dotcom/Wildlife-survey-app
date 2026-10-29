package org.phwildlife.survey;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
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
  /** Shared compact Back + page-title header for non-startup destinations. */
  public static LinearLayout navigationHeader(Activity a,String title,String backLabel,
          Runnable onBack) {
    LinearLayout row=new LinearLayout(a);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    boolean largeText=a.getResources().getConfiguration().fontScale>=1.60f;
    // At large font sizes retain a spoken destination, but give the title room.
    Button back=button(a,largeText?"‹":"‹ "+backLabel,"secondary");
    back.setContentDescription("Back to "+backLabel);
    row.addView(back,new LinearLayout.LayoutParams(dp(a,largeText?60:120),dp(a,48)));
    back.setOnClickListener(v->onBack.run());
    TextView heading=new TextView(a);
    heading.setText(title);
    heading.setTextSize(largeText?16:20);
    heading.setTextColor(INK);
    heading.setSingleLine(true);
    heading.setEllipsize(android.text.TextUtils.TruncateAt.END);
    heading.setContentDescription(title);
    heading.setTypeface(null,Typeface.BOLD);
    heading.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
    heading.setPadding(dp(a,8),0,dp(a,3),0);
    row.addView(heading,new LinearLayout.LayoutParams(0,-2,1f));
    row.setMinimumHeight(dp(a,52));
    return row;
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
    b.setTextColor(foreground);
    b.setGravity(Gravity.CENTER);b.setMaxLines(3);
    b.setMinHeight(dp(a,FieldLayoutPolicy.actionHeightDp(
        a.getResources().getConfiguration().fontScale)));
    b.setPadding(dp(a,10),dp(a,6),dp(a,10),dp(a,6));
    GradientDrawable shape=new GradientDrawable();shape.setColor(fill);shape.setStroke(dp(a,1),stroke);shape.setCornerRadius(dp(a,14));
    GradientDrawable mask=new GradientDrawable();
    mask.setColor(Color.WHITE);mask.setCornerRadius(dp(a,14));
    int feedback=("secondary".equals(type)||"quiet".equals(type)||"outline-danger".equals(type))
        ? Color.argb(25,35,100,71) : Color.argb(52,255,255,255);
    b.setBackground(new RippleDrawable(ColorStateList.valueOf(feedback),shape,mask));
    b.setBackgroundTintList(null);
    return b;
  }
  /** Short, readable section titles for research and field-management screens. */
  public static TextView sectionTitle(Activity a,String title) {
    TextView t=new TextView(a);
    t.setText(title);t.setTextColor(FOREST);
    t.setTextSize(17);t.setTypeface(null,Typeface.BOLD);
    t.setPadding(dp(a,4),dp(a,14),dp(a,4),dp(a,8));
    return t;
  }
  public static GradientDrawable surface(Activity a) {
    GradientDrawable d=new GradientDrawable();d.setColor(Color.WHITE);
    d.setStroke(dp(a,1),BORDER);d.setCornerRadius(dp(a,16));return d;
  }
}
