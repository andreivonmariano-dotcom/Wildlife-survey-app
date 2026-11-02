"""APO RC74 BioTools UI and independent research-export source contracts."""
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def source(name):return (JAVA/name).read_text(encoding="utf-8")

def method(name, next_name):
    s=source("WelcomeActivity.java")
    return s.split("private void "+name+"(){",1)[1].split("private void "+next_name+"(){",1)[0]

class ApoBioToolsV134(unittest.TestCase):
    def test_home_biotools_enters_new_scientific_workspace(self):
        home=method("home","phMapsHub")
        self.assertIn('this::bioToolsHub',home)
        self.assertNotIn('this::fieldworkHub',home)
        bio=method("bioToolsHub","fieldworkHub")
        self.assertEqual(bio.count("ApoHomeUi.module(this,"),5)
        for label in ('"Surveys"','"Projects & Stations"','"Field Records"',
                      '"Analysis & Exports"','"Field Utilities"'):
            self.assertIn(label,bio)
        self.assertNotIn("LikasPortalUi.action(",bio)
        self.assertNotIn("LikasPortalUi.tile(",bio)
        self.assertNotIn("R.drawable.",bio)

    def test_surveys_still_handle_active_and_new_sessions(self):
        s=method("fieldworkHub","fieldRecordsHub")
        for required in ('String unfinished=activeSurvey()','if(unfinished!=null)',
                '"Current survey"','"New survey"','"Birds"','"Herpetofauna"',
                '"Mammals"','"Resume Survey"','putExtra("resume",true)'):
            self.assertIn(required,s)
        self.assertIn("SurveyMethods.lookup",s)
        self.assertNotIn("HistoryActivity.class",s)
        self.assertNotIn("DashboardActivity.class",s)

    def test_all_tools_remain_reachable(self):
        s=source("WelcomeActivity.java")
        for target in ("SitesActivity.class","HistoryActivity.class",
                       "HabitatRecordsActivity.class","DashboardActivity.class",
                       "AuditActivity.class","ResearchExportActivity.class",
                       "CallLibraryActivity.class","TrapProtocolActivity.class",
                       "FieldProtocolsActivity.class","StudyProtocolsActivity.class"):
            self.assertIn(target,s)
        for screen in ('"fieldwork"','"biotools"','"records"','"analysis"','"utilities"'):
            self.assertIn(screen,s)
        self.assertIn('UiKit.navigationHeader(this,profile+" survey","Surveys"',s)
        self.assertNotIn("field_tools_open",s)

    def test_export_is_independent_and_uses_canonical_writer(self):
        export=source("ResearchExportActivity.java")
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        self.assertIn('android:name=".ResearchExportActivity" android:exported="false"',manifest)
        for token in ("Intent.ACTION_CREATE_DOCUMENT","CATEGORY_OPENABLE",
                      'setType("application/zip")',
                      "MainActivity.writeResearchArchive(getApplicationContext(),db,zip)",
                      "new Thread(","ZipOutputStream","FieldDb", "runOnUiThread"):
            self.assertIn(token,export)
        self.assertNotIn("startActivity(new Intent(this,MainActivity.class)",export)
        self.assertNotIn("getWritableDatabase()",export)
        recorder=source("MainActivity.java")
        self.assertIn("static void writeResearchArchive(",recorder)
        self.assertIn('new Intent(Intent.ACTION_CREATE_DOCUMENT)',recorder)

    def test_version_and_database_identity_preserved(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 134",gradle)
        self.assertIn("0.27.3-rc74-apo-biotools",gradle)
        self.assertIn("org.phwildlife.survey.beta08",gradle)
        self.assertIn("super(context,NAME,null,28)",source("FieldDb.java"))

if __name__=="__main__":unittest.main()
