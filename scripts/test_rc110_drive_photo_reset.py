"""RC110: enforce no species photos while Drive labels are being normalized."""
import json
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
class DriveOnlyResetTests(unittest.TestCase):
    def test_only_habitats_in_offline_manifest(self):
        manifest=json.loads((ASSETS/"offline_photo_manifest_v1.json").read_text())
        self.assertTrue(manifest["items"])
        self.assertTrue(all(x["category"]=="habitat" for x in manifest["items"]))
        self.assertFalse((ASSETS/"offline_owner_photo_manifest_v1.json").exists())
    def test_no_bundled_species_jpegs(self):
        self.assertFalse(list((ASSETS/"offline_photos").glob("species_*.jpg")))
    def test_species_ui_hides_bundled_and_app_private_images(self):
        media=(JAVA/"PhLibrarySpeciesMedia.java").read_text()
        evidence=(JAVA/"OfflinePhotoEvidence.java").read_text()
        self.assertIn("return placeholder(a,scientific)",media)
        self.assertIn("hasOfflineTaxon(Activity a,String scientific){return false;}",evidence)
        self.assertIn("findForTaxon(Activity a,String scientific){return null;}",evidence)
        daily=(JAVA/"ApoSpeciesOfDay.java").read_text()
        self.assertNotIn("View photo=PhLibrarySpeciesMedia.photoOrPlaceholder",daily)
    def test_version_and_source_setup(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 170",gradle)
        self.assertNotIn("prepareOwnerNativeSpeciesPhotos",gradle)
if __name__=="__main__":
    unittest.main()
