"""Check the intentional missing-GPS survey start path and export integrity."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
CODE = ROOT / "app/src/main/java/org/phwildlife/survey"


class SurveyStartGpsSmokeTest(unittest.TestCase):
    def test_gps_skip_available_before_sky_chart(self):
        text=(CODE / "SurveyGpsActivity.java").read_text(encoding="utf-8")
        self.assertIn('"Start without GPS · mark start missing"',text)
        self.assertIn('launch.putExtra("start_without_gps",true)',text)
        self.assertIn('skipConfirmationOpen=true',text)
        self.assertIn("setPositiveButton(\"Start with missing GPS\"",text)
        self.assertLess(text.index('Button skip='),text.index('satelliteStatus=new TextView'))

    def test_start_requires_verification_or_explicit_skip(self):
        text=(CODE / "MainActivity.java").read_text(encoding="utf-8")
        self.assertIn('getBooleanExtra("start_without_gps",false)',text)
        self.assertIn('SurveyStartGpsPolicy.classify(withoutGps,',text)
        self.assertIn('withoutGps?null:startLatitude',text)
        self.assertIn('withoutGps?null:startLongitude',text)
        self.assertIn('withoutGps?null:iso(startGpsTime)',text)
        self.assertIn('startGpsStatus});',text)

    def test_explicit_status_survives_resume_and_export(self):
        text=(CODE / "MainActivity.java").read_text(encoding="utf-8")
        self.assertIn("startGpsStatus=c.getString(14)",text)
        self.assertIn("refreshStartGpsBanner()",text)
        self.assertIn('start_gps_recorded_at,start_gps_status FROM surveys',text)
        self.assertIn('start_gps_recorded_at_utc,start_gps_status',text)
        self.assertIn('startGpsBanner.setVisibility(missing?View.VISIBLE:View.GONE)',text)

    def test_migration_keeps_legacy_location_state_without_backfill(self):
        text=(CODE / "FieldDb.java").read_text(encoding="utf-8")
        self.assertIn('super(context,NAME,null,24)',text)
        self.assertIn('if(oldVersion<22)upgradeTwentyTwo(d)',text)
        self.assertIn('ALTER TABLE surveys ADD COLUMN start_gps_status TEXT',text)
        self.assertIn("'legacy_start_location_unrecorded'",text)

    def test_survey_status_does_not_override_station_rule(self):
        text=(CODE / "MainActivity.java").read_text(encoding="utf-8")
        self.assertIn('"missing_or_weak_gps_at_start"',text)
        self.assertIn("verifiedStationFix(",text)


if __name__ == "__main__":
    unittest.main()
