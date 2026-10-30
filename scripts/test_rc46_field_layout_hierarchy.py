"""RC46 visual hierarchy contracts. No changes to field measurement or data semantics."""
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def source(name):
    return (JAVA/name).read_text(encoding="utf-8")

class Rc46FieldLayoutHierarchy(unittest.TestCase):
    def test_active_home_uses_compact_content_sized_hero(self):
        portal=source("LikasPortalUi.java")
        welcome=source("WelcomeActivity.java")
        self.assertIn("public static View hero(Activity a,boolean compact)",portal)
        self.assertIn("cover.setMinimumHeight(dp(a,compact?88:151))",portal)
        self.assertIn("boolean showFrog=a.getResources().getConfiguration().fontScale<1.30f",portal)
        self.assertIn("dp(a,showFrog?76:20)",portal)
        self.assertIn("new FrameLayout.LayoutParams(-1,-2,Gravity.TOP)",portal)
        home=welcome.split("private void home(){",1)[1].split("private void fieldworkHub(){",1)[0]
        self.assertIn("hero(this,lastHomeActiveSurvey!=null)",home)
        self.assertIn("new LinearLayout.LayoutParams(-1,-2)",home)
        self.assertIn("if(lastHomeActiveSurvey==null)",home)
        self.assertIn('LikasPortalUi.session(this,()->',home)
        self.assertLess(home.index("LikasPortalUi.session(this,()->"),
                        home.index('LikasPortalUi.tile(this,"Fieldwork"'))
        for route in ('"Fieldwork"','"Library"','"Settings"'):
            self.assertIn(route,home)

    def test_ime_layout_keeps_footer_and_essential_warnings(self):
        recorder=source("MainActivity.java")
        self.assertIn("private boolean inputKeyboardVisible=false",recorder)
        self.assertIn("root.getWindowVisibleDisplayFrame(visibleFrame)",recorder)
        self.assertIn("boolean keyboardShown=obscured>dp(160)",recorder)
        self.assertIn("top.setVisibility(keyboardShown?View.GONE:View.VISIBLE)",recorder)
        self.assertIn("progressView.setVisibility(inputKeyboardVisible||result.progress.isEmpty()",recorder)
        self.assertIn("hud.addView(sessionClockView)",recorder)
        self.assertIn("hud.addView(startGpsBanner)",recorder)
        self.assertIn('saveButton=UiKit.button(this,"Save Observation","primary")',recorder)
        self.assertIn('endSurveyButton=UiKit.button(this,"End Survey","outline-danger")',recorder)
        self.assertIn("root.addView(footer)",recorder)
        self.assertIn("endSurveyButton.setOnClickListener(v->finishPrompt())",recorder)
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
        self.assertIn('android:name=".MainActivity" android:screenOrientation="portrait" android:windowSoftInputMode="adjustResize"',manifest)

    def test_gps_primary_actions_are_visible_before_optional_diagnostics(self):
        gps=source("SurveyGpsActivity.java")
        self.assertIn("LinearLayout diagnostics=new LinearLayout(this)",gps)
        self.assertIn("diagnostics.setVisibility(View.GONE)",gps)
        self.assertIn("diagnostics.addView(satelliteStatus)",gps)
        self.assertIn("diagnostics.addView(signalStatus)",gps)
        self.assertIn("diagnostics.addView(skyView",gps)
        self.assertIn("content.addView(diagnostics)",gps)
        self.assertIn('Button detailToggle=UiKit.button(this,"Satellite details ▾","quiet")',gps)
        self.assertIn("detailToggle.setOnClickListener",gps)
        self.assertLess(gps.index("content.addView(retry)"),gps.index("content.addView(diagnostics)"))
        self.assertLess(gps.index("content.addView(settings)"),gps.index("content.addView(diagnostics)"))
        self.assertLess(gps.index("content.addView(cancel)"),gps.index("content.addView(diagnostics)"))
        self.assertIn('"Start without GPS · mark start missing"',gps)
        self.assertIn('"Start without GPS · flag missing"',gps)
        self.assertIn("startButton.setVisibility(View.GONE)",gps)

    def test_version_and_reference_assets_are_unchanged(self):
        gradle=(ROOT/"app/build.gradle").read_text(encoding="utf-8")
        self.assertGreaterEqual(int(__import__("re").search(r"versionCode\s+(\d+)",gradle).group(1)),109)
        self.assertIn("0.27.3-rc49-history-review-preview",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("super(context,NAME,null,27)",source("FieldDb.java"))
        self.assertIn("frame.setClipToOutline(true)",source("LikasPortalUi.java"))

if __name__=="__main__":
    unittest.main()
