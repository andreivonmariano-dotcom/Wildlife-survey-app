"""Source contracts for APO RC73's typography-only Home navigation."""
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parent.parent
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class ApoHomeContracts(unittest.TestCase):
    def test_home_has_four_modules_and_settings(self):
        s=(JAVA/"WelcomeActivity.java").read_text()
        home=s.split("private void home(){",1)[1].split("private void phMapsHub(){",1)[0]
        for name in ("BioTools","Living Atlas"):
            self.assertIn('"'+name+'"',home)
        self.assertIn("Coming later: PHMaps · BioDiscover",home)
        self.assertIn("ApoHomeUi.header",home)
        self.assertIn("LikasSettingsActivity.class",home)
        self.assertNotIn("LikasPortalUi.hero",home)
        self.assertNotIn("LikasPortalUi.tile",home)
        self.assertNotIn("likas_natator_frog",home)

    def test_text_first_home_and_accessibility(self):
        s=(JAVA/"ApoHomeUi.java").read_text()
        self.assertIn("setContentDescription",s)
        self.assertIn("setMinimumHeight(dp(a,88))",s)
        self.assertIn("setFocusable(true)",s)
        self.assertNotIn("R.drawable.",s)
        self.assertNotIn("ImageView",s)

    def test_undeveloped_features_are_disclosed(self):
        s=(JAVA/"WelcomeActivity.java").read_text()
        self.assertIn("Standalone map layers",s)
        self.assertIn("not yet available in this build",s)
        self.assertIn('"phmaps".equals(currentScreen)',s)
        self.assertIn('"biodiscover".equals(currentScreen)',s)

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
