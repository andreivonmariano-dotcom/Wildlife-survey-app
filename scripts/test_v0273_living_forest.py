"""Contract checks for living LIKAS forest and wordmark-only startup."""
from pathlib import Path
import unittest
import re

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT / "app/src/main/java/org/phwildlife/survey"
RENDER=(JAVA / "LikasForestView.java").read_text(encoding="utf-8")
START=(JAVA / "LikasLaunchActivity.java").read_text(encoding="utf-8")
APP=(ROOT / "app/build.gradle").read_text(encoding="utf-8")

class LikasLivingForestTest(unittest.TestCase):
    def test_dynamic_scene_layers_are_really_rendered_each_frame(self):
        for step in (
            "drawSkyLights(c,t,w,h)",
            "drawClouds(c,t,w,h)",
            "drawBirds(c,t,w,h)",
            "drawWaterfalls(c,t,w,h)",
            "drawLakeRipples(c,t,w,h)",
            "drawSwayingLeaves(c,t,w,h)",
            "drawFireflies(c,t,w,h)",
        ):
            self.assertIn(step,RENDER)

    def test_artwork_coordinates_and_sky_identity(self):
        self.assertIn("FALL_X={.536f,.627f,.744f}",RENDER)
        self.assertIn("screenX(FALL_X[fall])",RENDER)
        self.assertIn("screenX(.649f)",RENDER)
        self.assertIn("sunWorld=.39f+dayProgress*.18f",RENDER)
        self.assertIn("deviceClock.get(Calendar.HOUR_OF_DAY)",RENDER)
        self.assertIn("nightMix",RENDER)
        self.assertIn("RadialGradient",RENDER)
        self.assertIn("Math.sin(t",RENDER)

    def test_motion_pacing_uses_accessibility_and_battery_settings(self):
        self.assertIn("ValueAnimator.areAnimatorsEnabled()",RENDER)
        self.assertIn("powerManager.isPowerSaveMode()",RENDER)
        self.assertIn("postInvalidateDelayed(",RENDER)
        self.assertIn("onWindowVisibilityChanged",RENDER)
        self.assertIn("setMotionEnabled(boolean enabled)",RENDER)
        self.assertIn("loadPreferred(context,DAY_HD,DAY_FALLBACK)",RENDER)
        self.assertIn("loadPreferred(context,NIGHT_HD,NIGHT_FALLBACK)",RENDER)

    def test_minimal_startup_auto_enters_home_and_respects_motion_accessibility(self):
        self.assertIn('brand.setText("LIKAS")',START)
        self.assertIn('ValueAnimator.areAnimatorsEnabled()',START)
        self.assertIn('setDuration(460)',START)
        self.assertIn('setDuration(210)',START)
        self.assertIn('overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)',START)
        self.assertIn('root.setOnClickListener(v->leaveSplash())',START)
        self.assertIn('new Intent(this,WelcomeActivity.class)',START)
        self.assertNotIn('new LikasForestView(this)',START)
        self.assertNotIn('action("Resume survey"',START)
        self.assertNotIn('action("Open LIKAS"',START)
        self.assertIn('finish();',START)

    def test_application_identity_and_version(self):
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",APP)
        self.assertGreaterEqual(int(re.search(r"versionCode\s+(\d+)",APP).group(1)),49)
        self.assertIn("versionName '0.27.3-rc7b-literature-inat-preview'",APP)
        self.assertTrue((ROOT / "app/src/main/res/drawable-nodpi/likas_frog_icon.webp").exists())

if __name__=="__main__":
    unittest.main()
