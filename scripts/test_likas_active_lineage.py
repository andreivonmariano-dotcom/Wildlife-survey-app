"""Regression checks for the LIKAS active-branch and release guard."""
import unittest
from check_likas_active_lineage import source_audit

POLICY = {
  "minimum_version_code": 49,
  "next_release_version_code_minimum": 50,
  "application_id": "org.phwildlife.survey.beta08",
  "sqlite_schema": 27,
}
DB = "super(context,NAME,null,27); if(oldVersion<27)upgradeTwentySeven(d)"
LAUNCH = '"Explore. Record. Conserve." "Resume survey" "Open LIKAS"'
SPRITE = "FRAME_MS=200L"

def gradle(code, app="org.phwildlife.survey.beta08", name="0.27.3-rc4"):
    return f"applicationId '{app}'\nversionCode {code}\nversionName '{name}'\n"

class LikasActiveLineageTest(unittest.TestCase):
    def check(self, text, launch=LAUNCH, asset=False, db=DB):
        return source_audit(POLICY, text, db, launch, SPRITE, asset)[0]

    def test_correct_rc4_staged_source_passes(self):
        self.assertEqual([], self.check(gradle(49)))

    def test_wrong_v016_baseline_rejected(self):
        self.assertTrue(any("older" in e for e in self.check(gradle(18))))

    def test_old_schema_rejected(self):
        self.assertTrue(any("schema" in e.lower() for e in
                            self.check(gradle(50), db="super(context,NAME,null,11)")))

    def test_missing_sprite_archive_rejected_when_player_activated(self):
        errors = self.check(gradle(50), launch=LAUNCH+" new LikasSpriteForestView(this)")
        self.assertTrue(any("lacks reviewed media" in e for e in errors))

    def test_version_bump_required_before_activation(self):
        errors = self.check(gradle(49), launch=LAUNCH+" new LikasSpriteForestView(this)", asset=True)
        self.assertTrue(any("versionCode >= 50" in e for e in errors))

    def test_app_identity_rejected_if_changed(self):
        self.assertTrue(any("applicationId" in e for e in
                            self.check(gradle(50, app="other.app.id")))

if __name__ == "__main__":
    unittest.main()
