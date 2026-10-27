"""Source contracts for the LIKAS startup navigation and visual cleanup."""
import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
CODE = (ROOT / "app/src/main/java/org/phwildlife/survey/LikasLaunchActivity.java").read_text(encoding="utf-8")
MANIFEST = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")

class LikasStartupMenuTests(unittest.TestCase):
    def test_launch_activity_is_only_launcher(self):
        self.assertIn('android:name=".LikasLaunchActivity"', MANIFEST)
        self.assertEqual(MANIFEST.count('android.intent.category.LAUNCHER'), 1)

    def test_resume_never_starts_an_unfinished_session_implicitly(self):
        self.assertIn('resumeButton.setVisibility(active?View.VISIBLE:View.GONE)', CODE)
        self.assertIn('resumeButton.setEnabled(LikasUiRules.canContinue(active))', CODE)
        self.assertIn('if(!Boolean.TRUE.equals(activeSurveyStatus()))', CODE)
        self.assertIn('new Intent(this,MainActivity.class).putExtra("resume",true)', CODE)

    def test_unreadable_database_is_not_treated_as_no_surveys(self):
        self.assertIn('private Boolean activeSurveyStatus()', CODE)
        self.assertIn('statusView.setVisibility(status==null?View.VISIBLE:View.GONE)', CODE)
        self.assertIn('return null;', CODE)

    def test_home_action_is_named_truthfully(self):
        self.assertIn('action("Open LIKAS",false)', CODE)
        self.assertIn('home.putExtra("show_menu",true)', CODE)
        self.assertNotIn('"New Survey"', CODE)
        self.assertNotIn('"Home & Tools"', CODE)
        self.assertNotIn('getPackageInfo(', CODE)

    def test_approved_art_and_readable_native_actions(self):
        self.assertIn('new LikasForestView(this)', CODE)
        self.assertIn('R.drawable.likas_frog_icon', CODE)
        self.assertIn('FieldLayoutPolicy.actionHeightDp(fontScale)', CODE)
        self.assertIn('new RippleDrawable(', CODE)
        self.assertIn('View.IMPORTANT_FOR_ACCESSIBILITY_NO', CODE)
        self.assertTrue((ROOT / "app/src/main/res/drawable-nodpi/likas_frog_icon.webp").exists())
        art=ROOT / "app/src/main/assets/likas_forest"
        self.assertTrue((art / "forest_day_hd.webp").exists())
        self.assertTrue((art / "forest_night_hd.webp").exists())

if __name__ == "__main__":
    unittest.main()
