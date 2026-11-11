"""RC33 home portal visual/navigation contract without editing survey state."""
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"app/src/main/java/org/phwildlife/survey"

def read(file):
    return (SRC/file).read_text(encoding="utf-8")

class PortalNavigation(unittest.TestCase):
    def test_home_has_one_live_entry_to_each_reference_feature(self):
        welcome=read("WelcomeActivity.java")
        splash=read("LikasLaunchActivity.java")
        self.assertIn('new Intent(this,WelcomeActivity.class)',splash)
        home=welcome.split("private void home(){",1)[1].split("private void bioToolsHub(){",1)[0]
        self.assertIn('ApoHomeUi.header(this,',home)
        for label in ('"Start Survey"','"Living Atlas"','"Research Library"','"Map"'):
            self.assertIn(label,home)
        self.assertIn("LikasSettingsActivity.class",home)
        self.assertIn("this::fieldworkHub",home)
        self.assertIn("ApoHomeUi.resume",home)
        self.assertNotIn('"BioTools"',home)
        self.assertNotIn("FieldBackupActivity.class",home)

    def test_fieldwork_is_distinct_and_preserves_methods(self):
        c=read("WelcomeActivity.java")
        tools=c.split("private void fieldworkHub(){",1)[1].split("/** Short status caption",1)[0]
        for label in ('"Birds"','"Herpetofauna"','"Mammals"',
                      '"Current survey"','"New survey"','"Resume Survey"'):
            self.assertIn(label,tools)
        self.assertNotIn('Rc7SpeciesLibraryActivity.class',tools)
        self.assertIn('private void setup(String selected)',c)
        self.assertNotIn('surveyTile(second,"Custom"',tools)
        self.assertNotIn('private void customPicker()',c)
        self.assertIn('private String activeSurvey()',c)
        self.assertIn('this::home',tools)
        for target in ('SitesActivity.class','HistoryActivity.class',
                       'DashboardActivity.class','CallLibraryActivity.class',
                       'TrapProtocolActivity.class'):
            self.assertIn(target,c)

    def test_library_and_settings_are_separate_destinations(self):
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        for a in ("LikasLibraryActivity","LikasSettingsActivity"):
            self.assertIn(f'android:name=".{a}"',manifest)
            self.assertIn('android:exported="false"',manifest)
        library=read("LikasLibraryActivity.java")
        for c in ("PhLibraryDirectoryActivity.class",
                  "HabitatSpeciesEvidenceActivity.class"):
            self.assertIn(c,library)
        discovery=read("LikasDiscoveryActivity.java")
        self.assertIn("Rc7SpeciesLibraryActivity.class",discovery)
        self.assertIn("HabitatRecordsActivity.class",discovery)
        self.assertNotIn("MainActivity.class",library)
        settings=read("LikasSettingsActivity.java")
        for c in ("FieldBackupActivity.class","DataManagementActivity.class",
                  "ImportActivity.class","AuditActivity.class"):
            self.assertIn(c,settings)
        self.assertIn('getSharedPreferences("likas_ui",MODE_PRIVATE)',settings)

    def test_minimal_custom_artwork_and_motion_controls(self):
        icons=read("LikasPortalUi.java")
        for v in ('case "field":','case "library":case "species":',
                  'case "settings":','case "habitat":','case "photo":'):
            self.assertIn(v,icons)
        self.assertIn("RippleDrawable",icons)
        self.assertIn("Canvas",icons)
        motion=read("UiMotion.java")
        self.assertIn('getBoolean("reduced_motion",false)',motion)
        self.assertRegex((ROOT/"app/build.gradle").read_text(),r"versionCode\s+(?:9[6-9]|[1-9]\d{2,})\b")
        self.assertNotIn("new URL(",icons)

if __name__=="__main__":
    unittest.main()
