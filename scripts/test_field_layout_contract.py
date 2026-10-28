"""Contract tests for the fixed field dock and compact LIKAS Home layout."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/org/phwildlife/survey"

class FieldLayoutContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.recorder = (JAVA / "MainActivity.java").read_text(encoding="utf-8")
        cls.home = (JAVA / "WelcomeActivity.java").read_text(encoding="utf-8")
        cls.styles = (JAVA / "UiKit.java").read_text(encoding="utf-8")
        cls.gps = (JAVA / "SurveyGpsActivity.java").read_text(encoding="utf-8")

    def test_session_and_gps_readable_outside_scrolling_form(self):
        text = self.recorder
        hud = text.index("LinearLayout hud=vertical()")
        scroll = text.index("fieldScroll=new ScrollView(this)")
        self.assertLess(hud, scroll)
        self.assertIn("hud.addView(sessionClockView)",text)
        self.assertIn("hud.addView(eventView)",text)
        self.assertIn("hud.addView(gpsView)",text)
        self.assertIn("hud.addView(startGpsBanner)",text)
        self.assertIn('showGpsDetail()',text)

    def test_primary_save_and_end_survey_are_fixed_and_separate(self):
        text = self.recorder
        scroll = text.index("root.addView(fieldScroll")
        foot = text.index("LinearLayout footer=vertical()",scroll)
        self.assertLess(scroll,foot)
        self.assertIn('saveButton=UiKit.button(this,"Save Observation","primary")',text)
        self.assertIn('endSurveyButton=UiKit.button(this,"End Survey","outline-danger")',text)
        self.assertIn("footerOptions.addView(endSurveyButton,endParams)",text)
        self.assertIn("root.addView(footer)",text)
        self.assertIn("endSurveyButton.setOnClickListener(v->finishPrompt())",text)
        self.assertIn("saveButton.setOnClickListener(v->save())",text)
        for outcome in ("Finish Complete","End Incomplete","Cancel Survey"):
            self.assertIn(outcome,text)

    def test_home_has_large_two_column_survey_targets(self):
        text=self.home
        self.assertIn('surveyTile(first,"Birds"',text)
        self.assertIn('surveyTile(first,"Herpetofauna"',text)
        self.assertIn('surveyTile(second,"Mammals"',text)
        self.assertIn('surveyTile(second,"Custom"',text)
        self.assertIn("tile.setMinimumHeight(dp(104))",text)
        self.assertIn('Button resume=UiKit.button(this,"Resume Survey","primary")',text)
        self.assertIn('String unfinished=activeSurvey()',text)
        self.assertIn('tools.setVisibility(View.GONE)',text)

    def test_four_part_setup_preserves_real_field_inputs(self):
        text=self.home
        for heading in ('step("01","Survey method")','step("02","Project and location")',
                        'step("03","Sampling effort")','step("04","Begin survey")'):
            self.assertIn(heading,text)
        for entry in ('projectChoice','geographicChoice','spacingChoice','routeLengthEntry',
                      'durationEntry','observerEntry','regionEntry','areaEntry'):
            self.assertIn(entry,text)
        self.assertIn('setupStartButton.setOnClickListener(v->startSelected())',text)
        self.assertIn('setupFooter.setVisibility(View.VISIBLE)',text)
        self.assertIn('extraDetails.addView(observerEntry)',text)

    def test_gps_skip_and_exports_are_unchanged(self):
        self.assertIn('"Start without GPS · mark start missing"',self.gps)
        self.assertIn('start_gps_recorded_at_utc,start_gps_status',self.recorder)
        self.assertIn('"missing_at_survey_start"',self.recorder)
        self.assertIn("recordedCount(", (JAVA / "SurveyHudReader.java").read_text(encoding="utf-8"))

    def test_unified_palette_and_action_colors(self):
        text=self.styles
        for token in ("BG=Color.rgb(245,247,240)","FOREST=Color.rgb(35,100,71)",
                      "POND=Color.rgb(51,126,115)","case \"outline-danger\""):
            self.assertIn(token,text)


if __name__ == "__main__":
    unittest.main()
