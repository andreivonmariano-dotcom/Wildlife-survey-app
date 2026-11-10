"""RC46 visual hierarchy contracts. No changes to field measurement or data semantics."""
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def source(name):
    return (JAVA/name).read_text(encoding="utf-8")

class Rc46FieldLayoutHierarchy(unittest.TestCase):
    def test_active_home_uses_compact_content_sized_hero(self):
        welcome=source("WelcomeActivity.java")
        home=welcome.split("private void home(){",1)[1].split("private void phMapsHub(){",1)[0]
        component=source("ApoHomeUi.java")
        self.assertIn("lastHomeActiveSurvey=activeSurvey()",home)
        self.assertIn("if(lastHomeActiveSurvey!=null)",home)
        self.assertIn("ApoHomeUi.resume(this,",home)
        self.assertLess(home.index("ApoHomeUi.resume(this,"),
                        home.index('ApoHomeUi.module(this,"01","BioTools"'))
        self.assertIn("ApoHomeUi.header(this,",home)
        self.assertIn("UiMotion.enterHome(this,main)",home)
        self.assertNotIn("LikasPortalUi.hero(",home)
        self.assertNotIn("LikasPortalUi.tile(",home)
        self.assertIn("row.setMinimumHeight(dp(a,88))",component)
        self.assertIn('Coming later: PHMaps · BioDiscover',home)
        for route in ('"BioTools"','"PHLibrary"'):
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
        self.assertIn('qualitySummary=UiKit.caption(this',gps)
        self.assertIn('detailPanel.setVisibility(View.GONE)',gps)
        self.assertIn('helpPanel.setVisibility(View.GONE)',gps)
        self.assertIn('detailPanel.addView(satelliteStatus)',gps)
        self.assertIn('detailPanel.addView(signalStatus)',gps)
        self.assertIn('detailPanel.addView(skyView',gps)
        self.assertIn('Button detailToggle=UiKit.button(this,"GPS details","quiet")',gps)
        self.assertIn('helpToggle=UiKit.button(this,"GPS troubleshooting","quiet")',gps)
        self.assertIn('addAction(helpPanel,retry)',gps)
        self.assertIn('addAction(helpPanel,settings)',gps)
        self.assertIn('page.addView(footer)',gps)
        self.assertIn('UiKit.button(this,"Continue without GPS","warning")',gps)
        self.assertIn('UiKit.button(this,"Start station without GPS","warning")',gps)
        self.assertIn('startButton.setVisibility(View.GONE)',gps)
        self.assertIn('stationMode?"Survey":"Survey setup",this::cancel,content',gps)
    def test_version_and_reference_assets_are_unchanged(self):
        gradle=(ROOT/"app/build.gradle").read_text(encoding="utf-8")
        self.assertGreaterEqual(int(__import__("re").search(r"versionCode\s+(\d+)",gradle).group(1)),109)
        self.assertIn("0.27.3-rc",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("super(context,NAME,null,29)",source("FieldDb.java"))
        self.assertIn("frame.setClipToOutline(true)",source("LikasPortalUi.java"))

if __name__=="__main__":
    unittest.main()
