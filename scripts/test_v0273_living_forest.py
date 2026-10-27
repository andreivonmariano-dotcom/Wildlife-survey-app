"""Contract checks for living LIKAS forest and wordmark-only startup."""
from pathlib import Path
import unittest

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

    def test_startup_is_wordmark_only_and_version_is_not_hardcoded(self):
        self.assertNotIn("ImageView",START)
        self.assertNotIn("icon.setImageResource",START)
        self.assertIn('label("LIKAS",brandSize,CREAM,true)',START)
        self.assertIn('label("Explore. Record. Conserve.",16,SAGE,false)',START)
        self.assertIn('getPackageInfo(getPackageName(),0).versionName',START)
        self.assertIn("Gravity.TOP|Gravity.END",START)
        self.assertIn("action(\"Resume survey\",true)",START)
        self.assertIn("action(\"Open LIKAS\",false)",START)

    def test_application_identity_and_version(self):
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",APP)
        self.assertIn("versionCode 49",APP)
        self.assertIn("versionName '0.27.3-rc4'",APP)
        self.assertTrue((ROOT / "app/src/main/res/drawable-nodpi/likas_frog_icon.webp").exists())

if __name__=="__main__":
    unittest.main()
