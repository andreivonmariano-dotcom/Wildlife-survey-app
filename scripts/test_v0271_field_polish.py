"""Source checks for v0.27.1 UI polish and scientific display accuracy."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/org/phwildlife/survey"

def source(name):
    return (JAVA / name).read_text(encoding="utf-8")

class FieldPolishContractTest(unittest.TestCase):
    def test_missing_vertical_accuracy_is_not_reported_as_zero(self):
        s = source("StationProgressActivity.java")
        self.assertIn('c.isNull(6)?" · accuracy unavailable"', s)
        self.assertIn('String.format(Locale.US," · ±%.0f m",c.getDouble(6))', s)

    def test_station_lists_distinguish_registry_from_survey_visits(self):
        s = source("StationProgressActivity.java")
        self.assertIn("Other registered project stations", s)
        self.assertIn("Stations visited in this survey", s)
        self.assertIn("SurveyMethods.displayLabel(profile,method)", s)
        self.assertIn("WHEN complete=1 THEN 'complete' ELSE 'incomplete'", s)

    def test_no_extra_ui_recorder_actions(self):
        s = source("MainActivity.java")
        self.assertIn('UiKit.button(this,"End Survey"', s)
        self.assertIn('UiKit.button(this,"Save Observation"', s)
        self.assertIn('UiKit.button(this,"Review Records"', s)
        self.assertIn('private void finishPrompt()', s)

    def test_shared_touch_feedback_and_palette(self):
        s = source("UiKit.java")
        self.assertIn("RippleDrawable", s)
        self.assertIn("sectionTitle(Activity a,String title)", s)
        self.assertIn("FieldLayoutPolicy.actionHeightDp(", s)

    def test_brand_art_stays_on_branch(self):
        art = ROOT / "app/src/main/assets/likas_forest"
        self.assertTrue((art / "forest_day_hd.webp").is_file())
        self.assertTrue((art / "forest_night_hd.webp").is_file())
        self.assertTrue((ROOT / "app/src/main/res/drawable-nodpi/likas_frog_icon.webp").is_file())

if __name__ == "__main__":
    unittest.main()
