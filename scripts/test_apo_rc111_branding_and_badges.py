"""RC111 user-facing APO branding, scientific badge hierarchy and recovery compatibility."""
from pathlib import Path
import unittest

BASE=Path(__file__).resolve().parents[1]
JAVA=BASE/"app/src/main/java/org/phwildlife/survey"
def read(name):return (JAVA/name).read_text(encoding="utf-8")

class ApoRc111BrandingAndReadability(unittest.TestCase):
    def test_user_visible_recovery_is_apo_but_backup_format_is_unchanged(self):
        ui=read("FieldBackupActivity.java")
        fmt=read("FieldBackupArchive.java")
        for expected in ("APO-field-backup.zip","empty APO workspace",
                         "Close and reopen APO","Close APO","Check and restore backup ZIP"):
            self.assertIn(expected,ui)
        for retired in ("empty LIKAS workspace","Close and reopen LIKAS",
                        "Check and restore LIKAS ZIP"):
            self.assertNotIn(retired,ui)
        # Do not rename this legacy format tag: earlier valid backups depend on it.
        self.assertIn('private static final String FORMAT="likas-field-backup-v1"',fmt)
        self.assertIn("FORMAT.equals(manifest.optString",fmt)
        self.assertIn("manifestSchema>=27&&manifestSchema<=29",fmt)
    def test_large_text_status_preserves_label_without_wrapping_symbol(self):
        atlas=read("PhLibraryDirectoryActivity.java")
        self.assertIn('"Book rarity",10,PhLibrary2Ui.MUTED',atlas)
        self.assertIn('"IUCN global",10,PhLibrary2Ui.MUTED',atlas)
        self.assertIn("rarityColumn.addView(rarity,new LinearLayout.LayoutParams(-1,-2))",atlas)
        self.assertIn("globalColumn.addView(conservation,new LinearLayout.LayoutParams(-1,-2))",atlas)
        self.assertIn("h.badges.setOrientation(enlarged?LinearLayout.VERTICAL",atlas)
        self.assertNotIn('rarity.setText("Book rarity · "',atlas)
        self.assertNotIn('conservation.setText("IUCN global · "',atlas)
    def test_saved_clear_still_requires_visible_positive_confirmation(self):
        atlas=read("PhLibraryDirectoryActivity.java")
        test=(BASE/"app/src/androidTest/java/org/phwildlife/survey/Rc103PhLibrary2InstrumentedTest.java").read_text()
        self.assertIn('setTitle("Clear saved species?")',atlas)
        self.assertIn('.setPositiveButton("Clear saved"',atlas)
        self.assertIn('activeClearSavedDialogForQa()',atlas)
        self.assertIn('confirm.performClick()',test)
        self.assertIn('assertTrue(PhLibraryBookmarks.all(a).isEmpty())',test)
    def test_research_source_identifiers_and_survey_engine_preserved(self):
        gradle=(BASE/"app/build.gradle").read_text()
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("versionCode 171",gradle)
        self.assertIn("super(context,NAME,null,29)",read("FieldDb.java"))
        recorder=read("MainActivity.java")
        for action in ('"Save Observation"','"Review Records"','"End Survey"'):
            self.assertIn(action,recorder)

if __name__=="__main__":
    unittest.main()
