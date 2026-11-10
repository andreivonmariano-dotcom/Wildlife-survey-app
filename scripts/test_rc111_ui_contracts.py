"""APO RC111 approved UX contracts; never substitute these for native emulator QA."""
from pathlib import Path
import hashlib
import re
import unittest

ROOT=Path(__file__).resolve().parents[1]
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"

def src(name):
    return (CODE/name).read_text(encoding="utf-8")

class ApoRc111Contracts(unittest.TestCase):
    def test_version_identity_immutable_source_and_new_icon(self):
        build=(ROOT/"app/build.gradle").read_text()
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        self.assertRegex(build,r"versionCode\s+171\b")
        self.assertIn("0.27.3-rc111-apo-unified-field-first",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn('android:label="Apo"',manifest)
        pairs={
            "app/locked/apo_rc111_approved_launcher_master.png":
                "828b156748e5614c7a9b74efe90ee0272b1336b7f92334ebe5702a7959332c7b",
            "app/src/main/res/drawable-nodpi/apo_rc111_launcher_foreground.png":
                "fef4ef6924d8864b52956ab52e859499122cb9d8b98a9b7ced876557398903fb",
        }
        for name,expected in pairs.items():
            self.assertEqual(hashlib.sha256((ROOT/name).read_bytes()).hexdigest(),expected)
        self.assertIn("6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7",build)
        for xml in ("apo_launcher.xml","apo_launcher_round.xml"):
            self.assertIn("@drawable/apo_rc111_launcher_foreground",
                (ROOT/"app/src/main/res/mipmap-anydpi-v26"/xml).read_text())

    def test_first_screen_prioritizes_field_tasks(self):
        home=src("WelcomeActivity.java")
        section=home[home.index("private void home(){"):home.index("private void phMapsHub(){")]
        self.assertLess(section.index("ApoHomeUi.primary"),section.index("ApoSpeciesOfDay.createCompact"))
        self.assertLess(section.index("Species & habitats"),section.index("ApoSpeciesOfDay.createCompact"))
        self.assertIn("In development: offline map atlas",section)
        self.assertNotIn('ApoHomeUi.module(this,"03","PHMaps"',section)
        self.assertNotIn('ApoHomeUi.module(this,"04","BioDiscover"',section)

    def test_brand_and_tokens(self):
        settings=src("LikasSettingsActivity.java")
        self.assertIn("About APO",settings)
        self.assertIn('.setTitle("APO")',settings)
        self.assertNotIn("About LIKAS",settings)
        for name in ("PAPER","FOREST","DEEP","INK","MUTED","LINE"):
            self.assertIn("UiKit.",src("PhLibrary2Ui.java"))
        self.assertIn("private static final int INK=UiKit.INK",src("ApoHomeUi.java"))

    def test_survey_controls_retain_sticky_end_and_multiline_selection(self):
        setup=src("WelcomeActivity.java")
        recorder=src("MainActivity.java")
        self.assertIn("private ArrayAdapter<String> readableSelection(",setup)
        self.assertIn("view.setMaxLines(3)",setup)
        self.assertIn("geographicChoice.setAdapter(readableSelection(",setup)
        self.assertIn("methodPicker.setAdapter(readableSelection(",setup)
        self.assertIn('UiKit.button(this,"Save Observation","primary")',recorder)
        self.assertIn('UiKit.button(this,"End Survey","outline-danger")',recorder)
        self.assertIn("statusDetailsPanel=hud",recorder)
        self.assertIn("groupBar.setOrientation(stackGroup?",recorder)
        self.assertIn('UiKit.button(this,"Export research ZIP","secondary")',recorder)

    def test_scientific_semantics_and_gallery_performance(self):
        atlas=src("PhLibraryDirectoryActivity.java")
        self.assertIn("source accounts mention ",atlas)
        self.assertIn("Book rarity: ",atlas)
        self.assertIn("IUCN global: ",atlas)
        habitat=src("HabitatSpeciesEvidenceActivity.java")
        self.assertIn("Search matches do not establish species habitat occupancy.",habitat)
        images=src("PhLibraryDriveGallery.java")
        self.assertIn("new java.util.concurrent.ThreadPoolExecutor",images)
        self.assertIn("decode(a.getApplicationContext(),items.get(0),2)",images)
        self.assertIn("preloadNeighbors(a,items,position[0]);",images)

if __name__=="__main__":
    unittest.main()
