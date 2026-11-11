"""Offline source integrity and UI wiring regressions for APO RC108."""
import base64
import gzip
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/org/phwildlife/survey"
ASSETS = ROOT / "app/src/main/assets"


class SpeciesMediaDailyTests(unittest.TestCase):
    def test_locked_book_and_source_backed_photo_candidates(self):
        raw = gzip.decompress(base64.b64decode(
            (ASSETS / "phlibrary_book01_v182.json.gz.b64").read_bytes()))
        book = json.loads(raw)
        rows = {entry["s"]: entry for entry in book["records"]}
        self.assertEqual(len(rows), 715)
        manifest = json.loads((ASSETS / "offline_photo_manifest_v1.json").read_text())
        eligible = [p for p in manifest["items"]
                    if p.get("category") == "species" and p.get("taxon") in rows]
        self.assertGreaterEqual(len(eligible), 3)
        for p in eligible:
            self.assertTrue(p.get("license"))
            if p.get("rightsBasis") == "owner_requested_in_app_preview":
                self.assertEqual(
                    "Source filename only; species identification unverified",
                    p.get("identificationReview"))
                self.assertFalse(p.get("sourceUrl"))
            else:
                self.assertTrue(p.get("sourceUrl"))
            record = rows[p["taxon"]]
            self.assertIn(record["t"],
                          ("Common", "Uncommon", "Rare", "Exceptionally rare",
                           "Legendary", "Unranked"))
            self.assertIn("p", record)

    def test_ui_only_reads_locked_labels(self):
        daily = (JAVA / "ApoSpeciesOfDay.java").read_text()
        self.assertIn('row.optString("t"', daily)
        self.assertIn('row.optString("iucn"', daily)
        self.assertIn('account.optString("b"', daily)
        self.assertIn('firstSourceSentence(row.optString("p"))', daily)
        self.assertIn('ZoneId.of("Asia/Manila")', daily)
        self.assertIn("hasApprovedPhoto", daily)

    def test_placeholders_and_device_storage_have_no_network_dependency(self):
        media = (JAVA / "PhLibrarySpeciesMedia.java").read_text()
        self.assertIn('PhLibrary2Ui.text(a,"?",38', media)
        self.assertIn('0xFFE3E6E3', media)
        self.assertIn('getFilesDir()', media)
        self.assertIn('importPrivate', media)
        self.assertIn('Bitmap.CompressFormat.JPEG', media)
        self.assertNotIn('http://', media)
        self.assertNotIn('https://', media)
        self.assertNotIn('getExternalStorage', media)

    def test_home_atlas_and_reader_connected_without_data_schema_changes(self):
        build = (ROOT / "app/build.gradle").read_text()
        self.assertRegex(build, r"versionCode\s+173\b")
        self.assertIn("0.27.4-rc114-coherent-shell-preview", build)
        home = (JAVA / "WelcomeActivity.java").read_text()
        atlas = (JAVA / "PhLibraryDirectoryActivity.java").read_text()
        reader = (JAVA / "PhLibraryBook01.java").read_text()
        self.assertIn('ApoSpeciesOfDay.createCompact(this,scientific->', home)
        # RC110 atlas intentionally uses a virtualized text list. Each row
        # opens the species reader, where its offline multi-photo gallery lives.
        self.assertIn('startActivity(new Intent(this,TaxonDetailActivity.class)', atlas)
        self.assertIn('goToSpecies(matches.get(index).optString("s"))', atlas)
        self.assertIn('PhLibrarySpeciesMedia.addReaderPlate(a,parent,scientific)', reader)
        media = (JAVA / "PhLibrarySpeciesMedia.java").read_text()
        self.assertIn('PhLibraryDriveGallery.addReaderGallery(a,parent,scientific)', media)


if __name__ == "__main__":
    unittest.main()
