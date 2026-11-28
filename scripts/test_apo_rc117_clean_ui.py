"""RC117 source checks for coherent screen copy, transitions and data preservation."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def source(name): return (JAVA/name).read_text(encoding="utf-8")

class ApoCleanUi(unittest.TestCase):
    def test_one_entry_per_destination_and_clean_navigation(self):
        cards=source("ApoHomeUi.java")
        welcome=source("WelcomeActivity.java")
        self.assertNotIn("row.addView(index",cards)
        self.assertIn('row.setContentDescription("Open "+title+',cards)
        self.assertIn('tile.setContentDescription("Open "+title)',cards)
        self.assertIn('cfg.screenWidthDp<360||cfg.fontScale>=1.3f',cards)
        home=welcome.split("private void home(){",1)[1].split("private void bioToolsHub(){",1)[0]
        self.assertEqual(home.count("ApoHomeUi.module(this,"),1)
        self.assertEqual(home.count("ApoHomeUi.shortcutPair(this,"),1)
        for title in ("Living Atlas","Offline Map","Research Library"):
            self.assertEqual(home.count('"'+title+'"'),1)
        self.assertEqual(home.count('ApoHomeUi.section(this,"Explore"'),1)
        self.assertNotIn('End Survey is available while recording or paused.',welcome)
        self.assertIn('new String[]{"Field","Atlas","Map","Research","My Data"}',source("ApoShellBar.java"))
        atlas=source("LikasLibraryActivity.java")
        self.assertEqual(atlas.count('tile(body,"Species Atlas"'),0)
        self.assertIn('"Browse species"',atlas)
        self.assertIn('"TODAY IN ATLAS"',source("ApoSpeciesOfDay.java"))
        bibliography=source("PhLibraryBibliographyActivity.java")
        self.assertIn('PhLibrary2Ui.text(this,"Research Library"',bibliography)
        self.assertIn('matches.size()==1?" saved reference":" saved references"',bibliography)
        self.assertIn('tile(body,"Research Library"',atlas)

    def test_recorder_keeps_lifecycle_without_duplicate_captions(self):
        recorder=source("MainActivity.java")
        for contract in ('UiKit.button(this,"End Survey","outline-danger")',
                         'SurveyClosePolicy.resolve(', '"Cancel Survey · Not Counted"',
                         '"End Incomplete"', '"Finish Complete"'):
            self.assertIn(contract,recorder)
        self.assertIn("timelineDraft.setVisibility(unfinished?View.VISIBLE:View.GONE)",recorder)
        self.assertIn('"Observations · "+timelineSavedCount',recorder)
        self.assertNotIn("content.addView(methodFocus)",recorder)
        self.assertIn('eventView.setVisibility("opportunistic".equals(method)&&target<=0?',recorder)

    def test_transition_theme_and_reduced_motion(self):
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
        self.assertIn('android:name=".ApoApplication"',manifest)
        self.assertIn('android:theme="@style/ApoTheme"',manifest)
        themed=(ROOT/"app/src/main/res/values/apo_theme.xml").read_text()
        for key in ("activityOpenEnterAnimation","activityOpenExitAnimation",
                    "activityCloseEnterAnimation","activityCloseExitAnimation"):
            self.assertEqual(themed.count(key),1)
        root=ROOT/"app/src/main/res/anim"
        for name in ("apo_screen_enter","apo_screen_exit",
                     "apo_screen_back_enter","apo_screen_back_exit"):
            doc=ET.parse(root/(name+".xml")).getroot()
            self.assertEqual(doc.attrib["{http://schemas.android.com/apk/res/android}duration"],"155")
        app=source("ApoApplication.java")
        self.assertIn('getBoolean("reduced_motion",false)',app)
        self.assertIn("ValueAnimator.areAnimatorsEnabled()",app)
        self.assertIn("setWindowAnimations(reduced?0:R.style.ApoWindowAnimations)",app)

    def test_app_identity_database_and_map_source_remain_stable(self):
        self.assertIn('super(context,NAME,null,29)',source("FieldDb.java"))
        self.assertIn('getDatabasePath(FieldDb.NAME)',source("ApoOfflineMapActivity.java"))
        self.assertIn('SQLiteDatabase.OPEN_READONLY',source("ApoOfflineMapActivity.java"))
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 177",gradle)
        self.assertIn("org.phwildlife.survey.beta08",gradle)

if __name__=="__main__":
    unittest.main()
