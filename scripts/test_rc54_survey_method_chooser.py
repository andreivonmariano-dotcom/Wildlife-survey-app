"""RC54 Screen 3 source contracts: method cards are readable without changed protocols."""
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=(ROOT/"app/src/main/java/org/phwildlife/survey/WelcomeActivity.java").read_text()
METHODS=(ROOT/"app/src/main/java/org/phwildlife/survey/SurveyMethods.java").read_text()
GRADLE=(ROOT/"app/build.gradle").read_text()

class Rc54SurveyMethodChooser(unittest.TestCase):
    def test_each_method_has_persistent_inline_explanation(self):
        self.assertIn('new android.text.SpannableStringBuilder(title+"\\n"+m.description)', JAVA)
        self.assertIn('rb.setContentDescription(title+". "+m.description)', JAVA)
        self.assertIn("rb.setMinHeight(dp(80))", JAVA)
        self.assertIn('step("01","Choose a survey method")', JAVA)
        self.assertNotIn("methodHint", JAVA)
        self.assertNotIn("methodsCard", JAVA)

    def test_native_radio_selection_and_route_continuity(self):
        self.assertIn("methodChoices.addView(rb,methodLp)", JAVA)
        self.assertIn("methodChoices.check(1000)", JAVA)
        self.assertIn("updateMethodSelection(checked)", JAVA)
        self.assertIn("methodChoices.getCheckedRadioButtonId()-1000", JAVA)
        self.assertIn('setupStartButton.setText("Continue to GPS")', JAVA)
        self.assertIn("startActivity(i)", JAVA)
        self.assertIn("persistSetup()", JAVA)
        self.assertIn("getInt(\"methodChoice\",0)", JAVA)
        self.assertIn("forProfile(String profile)", METHODS)

    def test_existing_identity_and_protocol_catalog_are_unchanged(self):
        self.assertIn("versionCode 114", GRADLE)
        self.assertIn("0.27.3-rc54-survey-chooser-preview", GRADLE)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'", GRADLE)
        assert len(re.findall(r'new Method\\("',METHODS))>=17

if __name__=="__main__":
    unittest.main()
