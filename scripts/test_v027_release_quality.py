"""Automated source audits for integrated v0.27 screen, battery and release safeguards."""
import re
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"
def read(name): return (CODE/name).read_text(encoding="utf-8")

class ReleaseScreenAndPerfQA(unittest.TestCase):
    def test_all_registered_activities_have_concrete_source(self):
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
        classes=re.findall(r'android:name="\.([A-Za-z0-9]+Activity)"',manifest)
        self.assertGreaterEqual(len(classes),17)
        for name in classes:
            self.assertTrue((CODE/(name+".java")).is_file(),name)
        self.assertIn('android:windowSoftInputMode="adjustResize"',manifest)
    def test_tiny_screen_and_large_type_are_supported(self):
        home=read("WelcomeActivity.java")
        recorder=read("MainActivity.java")
        kit=read("UiKit.java")
        policy=read("FieldLayoutPolicy.java")
        self.assertIn("FieldLayoutPolicy.stackSurveyTiles",home)
        self.assertIn("second.setOrientation(first.getOrientation())",home)
        self.assertIn("FieldLayoutPolicy.stackSpeciesEntry",recorder)
        self.assertIn("FieldLayoutPolicy.saveHeightDp",recorder)
        self.assertIn("FieldLayoutPolicy.actionHeightDp",recorder)
        self.assertIn("b.setMaxLines(3)",kit)
        self.assertIn("availableWidthDp<390",policy)
        for label in ('"Save Observation"','"End Survey"'):
            self.assertIn(label,recorder)
    def test_hud_db_polling_is_off_main_thread_and_bounded(self):
        reader=read("SurveyHudReader.java")
        recorder=read("MainActivity.java")
        self.assertIn("Executors.newSingleThreadExecutor",reader)
        self.assertIn("AtomicBoolean pending",reader)
        self.assertIn("Result ready=result",reader)
        self.assertIn("ui.post(",reader)
        self.assertIn("helper::close",reader)
        self.assertIn("hudReader.request(",recorder)
        self.assertIn("now-lastProgressFetchMs>=6000L",recorder)
        status=recorder.split("private void status(){",1)[1].split("private void exportZip(){",1)[0]
        self.assertNotIn("visitProgress();",status)
        self.assertNotIn("getReadableDatabase()",status)
        self.assertNotIn("refreshTrack();",status)
    def test_export_uses_its_own_database_connection(self):
        recorder=read("MainActivity.java")
        export=recorder.split("private void csv(FieldDb exportDb,",1)[1]
        self.assertIn("exportDb.getReadableDatabase().rawQuery(sql,null)",export)
        self.assertNotIn("try(Cursor c=db.getReadableDatabase().rawQuery(sql,null))",export)
    def test_new_study_templates_are_scoped_and_older_templates_unchanged(self):
        setup=read("WelcomeActivity.java")
        editor=read("StudyProtocolsActivity.java")
        rule=read("StudyProtocols.java")
        schema=read("FieldDb.java")
        self.assertIn("study_template_taxon_scope",schema)
        self.assertIn("if(oldVersion<27)upgradeTwentySeven(d)",schema)
        self.assertIn('"survey_profile",wildlifeProfiles.get(',editor)
        self.assertIn('"Shared legacy"',editor)
        self.assertIn("survey_profile IS NULL",setup)
        self.assertIn("survey_profile IS NULL",rule)
        self.assertIn("StudyProtocols.apply(database,eventId,projectId,method,profile,",read("MainActivity.java"))
        self.assertIn("survey_profile,name,min_visits",read("StudyProtocolExports.java"))
    def test_offline_and_end_survey_guards_are_preserved(self):
        main=read("MainActivity.java")
        for key in ('start_without_gps','SurveyClosePolicy.resolve(',
                    'survey_study_protocols','StudyProtocols.passes',
                    'TrapProtocolPolicy.canComplete','FieldProtocolPolicy.canComplete',
                    'PhotoEvidence.writeZip','AcousticEvidence.writeZip'):
            self.assertIn(key,main)
        self.assertIn('android:allowBackup="false"',
            (ROOT/"app/src/main/AndroidManifest.xml").read_text(encoding="utf-8"))
if __name__=="__main__": unittest.main()
