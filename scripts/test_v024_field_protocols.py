"""Check structured coverboard, timed area-search and mammal sign-survey workflows."""
import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def read(name):
    return (JAVA/name).read_text(encoding="utf-8")
class V024FieldProtocolTest(unittest.TestCase):
    def test_non_destructive_schema_v25(self):
        schema=read("FieldDb.java")
        self.assertIn("super(context,NAME,null,27)",schema)
        self.assertIn("if(oldVersion<25)upgradeTwentyFive(d)",schema)
        self.assertIn("upgradeTwentyFive(d);",schema)
        for table in ("coverboard_units","coverboard_checks","area_search_passes",
                      "track_sign_efforts","track_sign_encounters"):
            self.assertIn("CREATE TABLE IF NOT EXISTS "+table+"(",schema)
            self.assertNotIn("DROP TABLE "+table,schema)
    def test_three_methods_no_longer_basic_log(self):
        catalog=read("SurveyMethods.java")
        for method in ("coverboard_search","area_search","track_sign"):
            line=next(v for v in catalog.splitlines() if 'new Method("'+method+'"' in v)
            self.assertTrue(line.rstrip().endswith("false),"),line)
        for method in ("bat_acoustic",):
            line=next(v for v in catalog.splitlines() if 'new Method("'+method+'"' in v)
            self.assertTrue(line.rstrip().endswith("true),"),line)
    def test_board_inventory_and_inspection(self):
        ui=read("FieldProtocolsActivity.java")
        for item in ("Register coverboard","Register","board_id","installed_at_utc",
                     "Record board inspection","individuals_seen","board_condition",
                     "humidity_pct","temperature_c","No boards registered"):
            self.assertIn(item,ui)
        self.assertIn("if(eventId==null||stationId.isEmpty())return false",ui)
    def test_area_pass_effort_and_repeat_detection_controls(self):
        ui=read("FieldProtocolsActivity.java")
        for item in ("Record timed area-search pass","measured_area_m2","repeat_pass",
                     "duplicate_control_notes","validInterval(started,ended)",
                     "Measured footprint area","Observer name"):
            self.assertIn(item,ui)
    def test_sign_effort_deduplicated_and_zero_detection_allowed(self):
        ui=read("FieldProtocolsActivity.java")
        for item in ("track_sign_efforts","track_sign_encounters","verified_length_m",
                     "independent_event_key","age_class","identification_confidence",
                     "No signs detected · valid zero result"):
            self.assertIn(item,ui)
    def test_method_complete_is_protocol_specific(self):
        main=read("MainActivity.java")
        self.assertIn("FieldProtocolPolicy.canComplete(method",main)
        self.assertIn("private int[] methodCompleteness()",main)
        self.assertIn("outcome=SurveyClosePolicy.INCOMPLETE",main)
        self.assertIn("FieldProtocolEvidence.writeZip(",main)
        self.assertIn("FieldProtocolsActivity.class",main)
    def test_review_after_survey_end(self):
        self.assertIn("FieldProtocolsActivity.class",read("HistoryActivity.java"))
        self.assertIn("FieldProtocolsActivity.class",read("WelcomeActivity.java"))
        self.assertIn(".FieldProtocolsActivity",
            (ROOT/"app/src/main/AndroidManifest.xml").read_text(encoding="utf-8"))
    def test_five_datasets_exported(self):
        s=read("FieldProtocolEvidence.java")
        for name in ("coverboard_units.csv","coverboard_checks.csv",
                     "area_search_passes.csv","track_sign_efforts.csv",
                     "track_sign_encounters.csv"):
            self.assertIn('"'+name+'"',s)
if __name__=="__main__":
    unittest.main()
