"""RC109 invariants: 333 owner-authorized Drive filenames, no outside photo assets."""
import base64
import gzip
import json
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class OwnerPhotoContracts(unittest.TestCase):
    def test_owner_inventory_retains_exact_333_distinct_names(self):
        index=json.loads((ASSETS/"apo_owner_drive_photo_inventory_v1.json").read_text())
        rows=index["items"]
        self.assertEqual(index["count"],333)
        self.assertEqual(len(rows),333)
        self.assertEqual(len({x["name"].lower() for x in rows}),333)
        self.assertEqual({x["group"] for x in rows},
                         {"Birds","Reptiles and Amphibians","Mammals"})
        self.assertEqual(
            {g:sum(x["group"]==g for x in rows) for g in
             {"Birds","Reptiles and Amphibians","Mammals"}},
            {"Birds":163,"Reptiles and Amphibians":149,"Mammals":21})
        for x in rows:
            self.assertRegex(x["name"],r"^[A-Za-z][A-Za-z0-9_]+\.(jpg|png)$")
            self.assertGreater(x["bytes"],1000)
        self.assertIn("Acerodon_jubatus__01.jpg",{x["name"] for x in rows})
        self.assertIn("Oreophryne_anulata.jpg",{x["name"] for x in rows})

    def test_only_owner_gallery_on_species_accounts_and_text_only_atlas(self):
        directory=(JAVA/"PhLibraryDirectoryActivity.java").read_text()
        media=(JAVA/"PhLibrarySpeciesMedia.java").read_text()
        home=(JAVA/"ApoSpeciesOfDay.java").read_text()
        book=(JAVA/"PhLibraryBook01.java").read_text()
        importer=(JAVA/"PhLibraryDrivePackImport.java").read_text()
        self.assertNotIn("photoOrPlaceholder(",directory)
        self.assertNotIn("OfflinePhotoEvidence",media)
        self.assertIn("addReaderPlate(a,parent,scientific)",book)
        self.assertIn('PREFS="phlibrary_owner_drive_gallery_v1"',media)
        self.assertIn("Bitmap.CompressFormat.JPEG,91",media)
        self.assertIn("Bitmap.Config.ARGB_8888",media)
        self.assertIn("ScaleType.FIT_CENTER",media)
        self.assertIn("hasApprovedPhoto(a,name)",home)
        self.assertIn("importPrivateBytes",importer)
        self.assertIn("ZipInputStream",importer)

    def test_never_package_other_source_photographs(self):
        self.assertFalse(list((ASSETS/"offline_photos").glob("*.jpg")))
        for name in ["offline_photo_manifest_v1.json",
                     "offline_inaturalist_manifest_v1.json"]:
            self.assertEqual(json.loads((ASSETS/name).read_text())["items"],[])
        # Approved owner landscape imagery/branding is intentionally separate
        # from wildlife taxon photos and must remain unchanged.
        self.assertTrue((ASSETS/"phlibrary_mindanao_forest_strip.webp.b64").exists())

    def test_taxonomic_book_unmodified_and_release_identity_retained(self):
        rows=json.loads(gzip.decompress(base64.b64decode(
            (ASSETS/"phlibrary_book01_v182.json.gz.b64").read_bytes())))["records"]
        self.assertEqual(len(rows),715)
        code=(ROOT/"app/build.gradle").read_text()
        self.assertRegex(code,r"versionCode\s+169\b")
        self.assertIn("0.27.3-rc109-owner-drive-photos",code)
        self.assertIn("org.phwildlife.survey.beta08",code)

if __name__=="__main__":
    unittest.main()
