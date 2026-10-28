"""Source checks for staged Android 5fps LIKAS sprite compositor.
Assets are distributed in a separate local archive pending repository import.
This test does not claim on-device playback verification.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'app/src/main/java/org/phwildlife/survey'
PLAYER=(BASE/'LikasSpriteForestView.java').read_text(encoding='utf-8')
STARTUP=(BASE/'LikasLaunchActivity.java').read_text(encoding='utf-8')

class SpritePlayerStageTest(unittest.TestCase):
    def test_genuine_independent_sprites(self):
        for part in ('"cloud"','"river"','"waterfall"','"wildlife"','"rocks"','"canopy"','"fern"'):
            self.assertIn(part,PLAYER)
        self.assertIn('BitmapRegionDecoder.newInstance',PLAYER)
        self.assertIn('s.decoder.decodeRegion',PLAYER)
        self.assertIn('FRAME_MS=200L',PLAYER)
        self.assertIn('ZipInputStream',PLAYER)
        self.assertNotIn('drawWaterfalls(',PLAYER)
        self.assertNotIn('ForestMotionPolicy.pan',PLAYER)

    def test_phone_clock_and_motion_limits(self):
        self.assertIn('nightFraction(int localMinute)',PLAYER)
        self.assertIn('Calendar.getInstance()',PLAYER)
        self.assertIn('ValueAnimator.areAnimatorsEnabled()',PLAYER)
        self.assertIn('powerManager.isPowerSaveMode()',PLAYER)
        self.assertIn('onDetachedFromWindow',PLAYER)
        self.assertIn('postDelayed(this::tick,motion?FRAME_MS:15000L)',PLAYER)

    def test_not_silently_enabled_without_matching_media(self):
        self.assertIn('new LikasForestView(this)',STARTUP)
        self.assertNotIn('new LikasSpriteForestView(this)',STARTUP)
        self.assertIn('action("Resume survey",true)',STARTUP)
        self.assertIn('action("Open LIKAS",false)',STARTUP)

if __name__=='__main__':
    unittest.main()
