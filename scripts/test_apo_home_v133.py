"""Source contracts for APO RC73's typography-only Home navigation."""
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parent.parent
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class ApoHomeContracts(unittest.TestCase):
    def test_home_has_one_field_action_and_two_live_reference_routes(self):
        s=(JAVA/"WelcomeActivity.java").read_text()
        home=s.split("private void home(){",1)[1].split("private void bioToolsHub(){",1)[0]
        self.assertIn('ApoHomeUi.header(this,',home)
        self.assertIn('"Start Survey"',home)
        self.assertIn('"Living Atlas"',home)
        self.assertIn('"Research Library"',home)
        self.assertIn('"Offline Map"',home)
        self.assertIn('this::openAtlas',home)
        self.assertIn('this::openResearch',home)
        self.assertIn('LikasSettingsActivity.class',home)
        self.assertNotIn('"BioTools"',home)
        self.assertNotIn("LikasPortalUi.hero(",home)

    def test_shared_accessible_components_and_approved_mark(self):
        s=(JAVA/"ApoHomeUi.java").read_text()
        self.assertIn("setContentDescription",s)
        self.assertIn("setMinimumHeight(dp(a,76))",s)
        self.assertIn("setFocusable(true)",s)
        self.assertIn("R.drawable.apo_splash_mark",s)
        self.assertNotIn("likas_natator_frog",s)

    def test_offline_map_has_one_real_destination_and_no_duplicate_hub(self):
        s=(JAVA/"WelcomeActivity.java").read_text()
        self.assertIn('ApoHomeUi.module(this,"03","Offline Map"',s)
        self.assertIn('ApoOfflineMapActivity.class',s)
        self.assertNotIn("private void phMapsHub()",s)
        self.assertNotIn("private void bioDiscoverHub()",s)
        self.assertIn('PhLibraryBibliographyActivity.class',s)

    def test_existing_survey_continuity(self):
        s=(JAVA/"WelcomeActivity.java").read_text()
        self.assertIn("lastHomeActiveSurvey=activeSurvey()",s)
        self.assertIn("ApoHomeUi.resume",s)
        self.assertIn('putExtra("resume",true)',s)
        g=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(re.search(r"versionCode\s+(\d+)",g).group(1)),133)
        self.assertIn("0.27.4-rc",g)
        self.assertIn("org.phwildlife.survey.beta08",g)

    def test_motion_respects_system_and_preference(self):
        s=(JAVA/"UiMotion.java").read_text()
        self.assertIn("public static void enterHome",s)
        self.assertIn("if(!animate(a))",s)
        self.assertIn("ValueAnimator.areAnimatorsEnabled()",s)

if __name__=="__main__":
    unittest.main()
