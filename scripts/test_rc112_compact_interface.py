#!/usr/bin/env python3
"""RC112 source contract: compact badge UI and non-enlarging APO icon."""
import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class Rc112CompactInterface(unittest.TestCase):
    def test_version_is_monotonic_and_data_model_unchanged(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 176",gradle)
        self.assertIn("0.27.4-rc116-localities-research-preview",gradle)
        self.assertIn("org.phwildlife.survey.beta08",gradle)
    def test_atlas_bare_status_values_with_accessibility(self):
        directory=(JAVA/"PhLibraryDirectoryActivity.java").read_text()
        badge=(JAVA/"PhLibraryStatusBadge.java").read_text()
        reader=(JAVA/"PhLibraryBook01.java").read_text()
        self.assertIn("makeCompact(",directory)
        self.assertIn("makeCompact(a,tier,true)",reader)
        self.assertNotIn('"Book rarity",10',directory)
        self.assertNotIn('"IUCN global",10',directory)
        self.assertIn('IUCN global status:',badge)
        self.assertIn("Educational rarity:",badge)
        self.assertIn("confirmClearSaved()",directory)
        self.assertIn('"Manage":"Clear"',directory)
    def test_original_icon_protected_and_foreground_shrunk(self):
        icon=(ROOT/"scripts/prepare_apo_compact_launcher.py").read_text()
        self.assertIn('SHA = "828b156748e5614c7a9b74efe90ee0272b1336b7f92334ebe5702a7959332c7b"',icon)
        self.assertIn("resize((310, 310)",icon)
        self.assertIn('Image.new("RGBA", (432, 432)',icon)
        self.assertNotIn("resize((432, 432)",icon)
    def test_survey_invariants_remain_exposed(self):
        recorder=(JAVA/"MainActivity.java").read_text()
        for field in ["Save Observation","End Survey","Finish Complete",
                      "End Incomplete","Not Counted","Review"]:
            self.assertIn(field,recorder)
    def test_offline_photo_list_stays_text_only(self):
        directory=(JAVA/"PhLibraryDirectoryActivity.java").read_text()
        self.assertNotIn("photoOrPlaceholder(",directory)
        self.assertIn("species",directory.lower())
if __name__=="__main__":
    unittest.main()
