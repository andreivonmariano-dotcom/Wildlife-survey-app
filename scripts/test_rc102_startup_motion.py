"""RC102 startup layout and motion regression without requiring an Android SDK."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
STARTUP=(ROOT/'app/src/main/java/org/phwildlife/survey/LikasLaunchActivity.java')
BUILD=(ROOT/'app/build.gradle')
MANIFEST=(ROOT/'app/src/main/AndroidManifest.xml')


class Rc102StartupMotion(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source=STARTUP.read_text(encoding='utf-8')

    def test_identity_is_truly_centered(self):
        src=self.source
        self.assertIn('Gravity.CENTER);',src)
        self.assertIn('root.addView(identity,identityParams);',src)
        self.assertNotIn('identityParams.topMargin=',src)
        self.assertIn('identityParams.leftMargin=dp(12);',src)
        self.assertIn('identityParams.rightMargin=dp(12);',src)

    def test_motion_has_distinct_once_only_scale_letter_and_line(self):
        src=self.source
        self.assertIn('setScaleX(0.93f)',src)
        self.assertIn('setScaleY(0.93f)',src)
        self.assertIn('scaleX(1f).scaleY(1f)',src)
        self.assertIn('OvershootInterpolator(0.48f)',src)
        self.assertIn('ValueAnimator.ofFloat(.24f,.12f)',src)
        self.assertIn('underline.setScaleX(0f)',src)
        self.assertIn('underline.animate().scaleX(1f)',src)
        self.assertNotIn('INFINITE',src)
        self.assertIn('underline.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO)',src)

    def test_reduced_motion_and_tap_skip_are_complete(self):
        src=self.source
        self.assertIn('ValueAnimator.areAnimatorsEnabled()',src)
        self.assertIn('getBoolean("reduced_motion",false)',src)
        self.assertIn('if(reducedMotion()) {',src)
        self.assertIn('root.setOnClickListener(v->{',src)
        self.assertIn('underline.animate().cancel()',src)
        self.assertIn('if(letterReveal!=null)letterReveal.cancel()',src)
        self.assertIn('wordmark.setLetterSpacing(.12f)',src)
        self.assertIn('underline.setScaleX(1f)',src)
        self.assertIn('maybeEnterHome()',src)
        self.assertIn('if(entered||exitStarted||qaHoldSplash||isFinishing()',src)

    def test_release_identity_and_data_scope(self):
        build=BUILD.read_text(encoding='utf-8')
        manifest=MANIFEST.read_text(encoding='utf-8')
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn('versionCode 173',build)
        self.assertIn("versionName '0.27.4-rc112-minimal-ui-compact-icon'",build)
        self.assertIn('android:name=".LikasLaunchActivity"',manifest)
        self.assertIn('android:exported="true"',manifest)
        self.assertIn('android:name=".WelcomeActivity"',manifest)
        self.assertNotIn('FieldDb',self.source)


if __name__=='__main__':
    unittest.main()
