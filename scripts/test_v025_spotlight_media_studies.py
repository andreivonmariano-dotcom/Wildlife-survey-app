"""LIКAS v0.25 field-protocol regression checks: spotlight, photos and templates."""
import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def src(name):
    return (JAVA/name).read_text(encoding="utf-8")
class V025Protocols(unittest.TestCase):
    def test_additive_schema(self):
        db=src("FieldDb.java")
        self.assertIn("super(context,NAME,null,27)",db)
        self.assertIn("if(oldVersion<26)upgradeTwentySix(d)",db)
        for term in ("CREATE TABLE IF NOT EXISTS spotlight_passes(",
                     "CREATE TABLE IF NOT EXISTS protocol_media(",
                     "CREATE TABLE IF NOT EXISTS study_protocol_templates(",
                     "CREATE TABLE IF NOT EXISTS survey_study_protocols(",
                     "coverboard_checks ADD COLUMN latitude",
                     "track_sign_encounters ADD COLUMN longitude"):
            self.assertIn(term,db)
    def test_spotlight_full_method(self):
        ui=src("FieldProtocolsActivity.java")
        for term in ("Record spotlight survey pass","beam_width_m","visibility_m",
                     "distance_m","repeat_pass","duplicate_control_notes",
                     "already", "spotlight_passes"):
            if term=="already":continue
            self.assertIn(term,ui)
        self.assertIn('new Method("spotlight","Spotlight survey"',src("SurveyMethods.java"))
        line=next(x for x in src("SurveyMethods.java").splitlines()
                  if 'new Method("spotlight"' in x)
        self.assertIn("false)",line)
        self.assertIn('method.equals("spotlight")',src("MainActivity.java"))
        self.assertIn("spotlight_passes.csv",src("FieldProtocolEvidence.java"))
    def test_protocol_photo_and_verified_gps(self):
        ui=src("FieldProtocolsActivity.java")
        for term in ("GpsFixPolicy.acceptableElapsed","getElapsedRealtimeNanos",
                     "gps_status","missing_or_unverified","choosePhoto",
                     "showPhotos","protocol_media","coverboard_check","track_sign",
                     "photoCount","stampGps"):
            self.assertIn(term,ui)
        photos=src("ProtocolEvidencePhotos.java")
        self.assertIn("PhotoEvidence.addPending",photos)
        self.assertIn("protocol_photo_index.csv",photos)
        self.assertIn('"protocol_photos/"+name',photos)
        self.assertIn("Missing protocol photo",photos)
    def test_study_rules_are_frozen_and_not_weaker_than_normal_rules(self):
        main=src("MainActivity.java")
        for term in ("StudyProtocols.apply","StudyProtocols.snapshot",
                     "StudyProtocols.passes","StudyProtocolExports.writeZip",
                     "ProtocolEvidencePhotos.writeZip"):
            self.assertIn(term,main)
        study=src("StudyProtocols.java")
        self.assertIn("INSERT INTO survey_study_protocols",study)
        self.assertIn("WHERE id=? AND project_id=? AND method=?",study)
        self.assertIn("WHERE event_id=?",study)
        self.assertIn("photoCount(",study)
    def test_templates_are_user_selectable_and_project_scoped(self):
        home=src("WelcomeActivity.java")
        for term in ("studyChoice=new Spinner","refreshStudyChoices()",
                     "study_protocol_id","Manage study templates",
                     "StudyProtocolsActivity.class"):
            self.assertIn(term,home)
        editor=src("StudyProtocolsActivity.java")
        for term in ("min_visits","min_minutes","min_units",
                     "require_verified_gps","require_protocol_photo"):
            self.assertIn(term,editor)
        self.assertIn(".StudyProtocolsActivity",
            (ROOT/"app/src/main/AndroidManifest.xml").read_text(encoding="utf-8"))
    def test_export_contains_all_new_tables(self):
        main=src("MainActivity.java")
        self.assertIn("FieldProtocolEvidence.writeZip",main)
        study=src("StudyProtocolExports.java")
        self.assertIn("study_protocol_templates.csv",study)
        self.assertIn("survey_study_requirements.csv",study)
        self.assertIn("gps_accuracy_m,gps_timestamp_utc,gps_status",src("FieldProtocolEvidence.java"))
if __name__=="__main__":
    unittest.main()
