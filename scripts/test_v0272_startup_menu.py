"""Source contracts for the owner-approved minimalist LIKAS startup."""
import pathlib
import unittest

ROOT=pathlib.Path(__file__).resolve().parents[1]
CODE=(ROOT/"app/src/main/java/org/phwildlife/survey/LikasLaunchActivity.java").read_text(encoding="utf-8")
HOME=(ROOT/"app/src/main/java/org/phwildlife/survey/WelcomeActivity.java").read_text(encoding="utf-8")
MANIFEST=(ROOT/"app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")

class LikasStartupMenuTests(unittest.TestCase):
    def test_launch_activity_is_only_launcher(self):
        self.assertIn('android:name=".LikasLaunchActivity"',MANIFEST)
        self.assertEqual(MANIFEST.count('android.intent.category.LAUNCHER'),1)

    def test_launch_never_starts_survey_implicitly(self):
        self.assertNotIn('new Intent(this,MainActivity.class)',CODE)
        self.assertIn('new Intent(this,WelcomeActivity.class)',CODE)
        self.assertIn('home.putExtra("show_menu",true)',CODE)
        self.assertIn('startActivity(home)',CODE)
        self.assertIn('finish();',CODE)

    def test_active_resume_remains_available_on_home_not_splash(self):
        self.assertIn('private String activeSurvey()',HOME)
        self.assertIn('new Intent(this,MainActivity.class).putExtra("resume",true)',HOME)
        self.assertNotIn('resumeButton',CODE)

    def test_minimal_title_motion_supports_reduced_motion(self):
        self.assertIn('brand.setText("LIKAS")',CODE)
        self.assertIn('ValueAnimator.areAnimatorsEnabled()',CODE)
        self.assertIn('root.setOnClickListener(v->leaveSplash())',CODE)
        self.assertIn('handler.removeCallbacksAndMessages(null)',CODE)
        self.assertNotIn('new LikasForestView(this)',CODE)
        self.assertNotIn('new RippleDrawable(',CODE)
        self.assertNotIn('getWritableDatabase()',CODE)
        self.assertTrue((ROOT/"app/src/main/res/drawable-nodpi/likas_frog_icon.webp").exists())

if __name__=="__main__":unittest.main()
