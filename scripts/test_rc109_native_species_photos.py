"""RC109: native species-photo packaging, source fidelity, and no network dependence."""
import base64
import gzip
import hashlib
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"

class NativePhotoTests(unittest.TestCase):
    def items(self):
        payload = json.loads((ASSETS / "offline_photo_manifest_v1.json").read_text())
        return [x for x in payload["items"]
                if x.get("rightsBasis") == "owner_requested_in_app_preview"]

    def test_exact_taxa_and_sha256_locked_sources(self):
        photos = self.items()
        self.assertEqual(9, len(photos))
        book = json.loads(gzip.decompress(base64.b64decode(
            (ASSETS / "phlibrary_book01_v182.json.gz.b64").read_bytes())))
        names = {row["s"] for row in book["records"]}
        seen = set()
        for record in photos:
            self.assertIn(record["taxon"], names)
            self.assertNotIn(record["taxon"], seen)
            seen.add(record["taxon"])
            self.assertEqual("Source filename only; species identification unverified",
                             record["identificationReview"])
            self.assertFalse(record["sourceUrl"])
            self.assertIn("species_owner_", record["offlineAsset"])
            source = ROOT / record["nativeSource"]
            self.assertTrue(source.is_file())
            self.assertEqual(hashlib.sha256(source.read_bytes()).hexdigest(),
                             record["sourceSha256"])

    def test_outputs_are_embedded_exif_free_jpegs(self):
        for record in self.items():
            image = (ASSETS / record["offlineAsset"]).read_bytes()
            self.assertGreater(len(image), 1000)
            self.assertLess(len(image), 450000)
            self.assertEqual(image[:3], bytes([255, 216, 255]))
            self.assertEqual(image[-2:], bytes([255, 217]))
            self.assertNotIn(b"Exif" + bytes([0,0]), image)

    def test_no_bulk_import_required_and_daily_home_uses_offline_assets(self):
        library = (ROOT / "app/src/main/java/org/phwildlife/survey/LikasLibraryActivity.java").read_text()
        home = (ROOT / "app/src/main/java/org/phwildlife/survey/ApoSpeciesOfDay.java").read_text()
        self.assertNotIn('tile(body,"Import species photos"', library)
        self.assertIn("hasApprovedPhoto", home)
        self.assertIn("Asia/Manila", home)

if __name__ == "__main__":
    unittest.main()
