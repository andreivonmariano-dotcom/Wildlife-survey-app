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
import android.widget.ScrollView;
import android.widget.TextView;

/** Small shared visual system for the offline survey application. */
public final class UiKit {
  private UiKit(){}
  // Shared forest palette. Warm off-white is reserved for readable field surfaces.
  public static final int BG=Color.rgb(247,248,242);
  public static final int INK=Color.rgb(19,51,38);
  public static final int DEEP=Color.rgb(10,42,29);
  public static final int FOREST=Color.rgb(33,83,53);
  public static final int POND=Color.rgb(46,105,65);
  public static final int MUTED=Color.rgb(81,106,87);
  public static final int LEAF_LIGHT=Color.rgb(237,246,231);
  public static final int LEAF_MID=Color.rgb(228,239,220);
  public static final int HIGHLIGHT=Color.rgb(205,230,160);
  // Distinct diagnostic/warning colors remain available where the meaning requires them.
  public static final int BLUE=Color.rgb(40,88,137);
  public static final int AMBER=Color.rgb(145,85,20); // legible diagnostic text on pale cards
  public static final int BORDER=Color.rgb(217,228,210); // decorative surface outline
  public static final int CONTROL_BORDER=Color.rgb(117,143,123); // 3:1+ for functional controls
  private static int dp(Activity a,int n){return (int)(a.getResources().getDisplayMetrics().density*n+0.5);}
  public static TextView title(Activity a,String title) {
    TextView t=new TextView(a);t.setText(title);t.setTextSize(LikasDesignSystem.TEXT_PAGE);
    t.setTypeface(null,Typeface.BOLD);
    t.setTextColor(INK);t.setPadding(dp(a,3),dp(a,9),dp(a,3),dp(a,7));return t;
  }
  /** Shared compact Back + page-title header for non-startup destinations. */
  public static LinearLayout navigationHeader(Activity a,String title,String backLabel,
          Runnable onBack) {
    LinearLayout row=new LinearLayout(a);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    boolean largeText=a.getResources().getConfiguration().fontScale>=1.30f;
    // One small icon-only Back action, with the full destination spoken to assistive tools.
    Button back=button(a,"‹","quiet");
    back.setContentDescription("Back to "+backLabel);
    back.setTextSize(18);
    back.setPadding(0,0,0,0);
    back.setMinWidth(dp(a,LikasDesignSystem.MIN_TOUCH_DP));
    back.setMinHeight(dp(a,LikasDesignSystem.MIN_TOUCH_DP));
    row.addView(back,new LinearLayout.LayoutParams(dp(a,48),dp(a,48)));
    back.setOnClickListener(v->onBack.run());
    TextView heading=new TextView(a);
    heading.setText(title);
    heading.setTextSize(largeText?17:20);
    heading.setTextColor(INK);
    heading.setSingleLine(!largeText);
    if(largeText)heading.setMaxLines(2);
    else heading.setEllipsize(android.text.TextUtils.TruncateAt.END);
    heading.setContentDescription(title);
    heading.setTypeface(null,Typeface.BOLD);
    heading.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
    heading.setPadding(dp(a,13),0,dp(a,3),0);
    row.addView(heading,new LinearLayout.LayoutParams(0,-2,1f));
    row.setMinimumHeight(dp(a,largeText?64:52));
    return row;
  }
  public static Button button(Activity a,String title,String type) {
    int fill=FOREST,stroke=FOREST,foreground=Color.WHITE;
    switch(type){
      case "secondary":fill=Color.WHITE;stroke=CONTROL_BORDER;foreground=INK;break;
      case "camera":fill=BLUE;stroke=BLUE;break;
      case "warning":fill=AMBER;stroke=AMBER;break;
      case "quiet":fill=BG;stroke=CONTROL_BORDER;foreground=FOREST;break;
      case "danger":fill=Color.rgb(166,53,48);stroke=fill;break;
      case "outline-danger":fill=Color.WHITE;stroke=Color.rgb(181,77,61);
          foreground=Color.rgb(154,51,42);break;
      case "pond":fill=POND;stroke=POND;break;
    }
    Button b=new Button(a);b.setAllCaps(false);b.setText(title);
    b.setTextSize(LikasDesignSystem.TEXT_CONTROL);b.setTypeface(null,Typeface.BOLD);
    b.setTextColor(foreground);
    b.setGravity(Gravity.CENTER);b.setMaxLines(3);
    b.setMinWidth(dp(a,LikasDesignSystem.MIN_TOUCH_DP));
    b.setMinHeight(dp(a,FieldLayoutPolicy.actionHeightDp(
        a.getResources().getConfiguration().fontScale)));
    b.setPadding(dp(a,10),dp(a,LikasDesignSystem.SPACE_SM),
        dp(a,10),dp(a,LikasDesignSystem.SPACE_SM));
    GradientDrawable shape=new GradientDrawable();shape.setColor(fill);shape.setStroke(dp(a,1),stroke);shape.setCornerRadius(dp(a,LikasDesignSystem.RADIUS_BUTTON));
    GradientDrawable mask=new GradientDrawable();
    mask.setColor(Color.WHITE);mask.setCornerRadius(dp(a,LikasDesignSystem.RADIUS_BUTTON));
    int feedback=("secondary".equals(type)||"quiet".equals(type)||"outline-danger".equals(type))
        ? Color.argb(25,35,100,71) : Color.argb(52,255,255,255);
    b.setBackground(new RippleDrawable(ColorStateList.valueOf(feedback),shape,mask));
    b.setBackgroundTintList(null);
    return b;
  }
  /** Identical native EditText / autocomplete surfaces across setup and live recording.
   * Only presentation is changed: no input type, text value or listener is overwritten. */
  public static void fieldInput(Activity a,TextView input){
    if(input==null)return;
    input.setTextColor(INK);
    input.setHintTextColor(MUTED);
    input.setMinHeight(dp(a,Math.max(56,FieldLayoutPolicy.actionHeightDp(
        a.getResources().getConfiguration().fontScale))));
    input.setPadding(dp(a,LikasDesignSystem.SPACE_LG),
        dp(a,LikasDesignSystem.SPACE_MD),
        dp(a,LikasDesignSystem.SPACE_LG),
        dp(a,LikasDesignSystem.SPACE_MD));
    GradientDrawable bg=new GradientDrawable();
    bg.setColor(Color.WHITE);
    bg.setStroke(dp(a,1),CONTROL_BORDER);
    bg.setCornerRadius(dp(a,LikasDesignSystem.RADIUS_INPUT));
    input.setBackground(bg);
  }
  /** Short, readable section titles for research and field-management screens. */
  public static TextView sectionTitle(Activity a,String title) {
    TextView t=new TextView(a);
    t.setText(title);t.setTextColor(FOREST);
    t.setTextSize(LikasDesignSystem.TEXT_SECTION);t.setTypeface(null,Typeface.BOLD);
    t.setPadding(dp(a,LikasDesignSystem.SPACE_XS),dp(a,14),
        dp(a,LikasDesignSystem.SPACE_XS),dp(a,LikasDesignSystem.SPACE_SM));
    return t;
  }
  /** Common secondary body text, with sp scaling and adequate pale-background contrast. */
  public static TextView caption(Activity a,String value){
    TextView t=new TextView(a);t.setText(value);t.setTextSize(LikasDesignSystem.TEXT_CAPTION);
    t.setTextColor(MUTED);return t;
  }
  /** One page shell, one Back pattern and one content scroll for all portal destinations. */
  public static LinearLayout page(Activity a,String name,String parent,Runnable onBack,
      LinearLayout body){
    LinearLayout root=new LinearLayout(a);root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundColor(BG);
    root.setPadding(dp(a,LikasDesignSystem.SCREEN_GUTTER),
        dp(a,LikasDesignSystem.SPACE_MD),
        dp(a,LikasDesignSystem.SCREEN_GUTTER),dp(a,LikasDesignSystem.SPACE_SM));
    root.addView(navigationHeader(a,name,parent,onBack));
    ScrollView scroll=new ScrollView(a);scroll.setFillViewport(true);
    body.setOrientation(LinearLayout.VERTICAL);
    body.setPadding(0,dp(a,LikasDesignSystem.SPACE_SM),0,
        dp(a,LikasDesignSystem.SPACE_XL));
    scroll.addView(body);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    return root;
  }
  public static GradientDrawable surface(Activity a) {
    GradientDrawable d=new GradientDrawable();d.setColor(Color.WHITE);
    d.setStroke(dp(a,1),BORDER);d.setCornerRadius(dp(a,LikasDesignSystem.RADIUS_CARD));return d;
  }
}
