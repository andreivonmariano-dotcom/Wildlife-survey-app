"""RC7D media reproducibility, explicit photo permissions and species evidence gates."""
import json
import hashlib
import tempfile
from pathlib import Path
import sys
import unittest

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
sys.path.insert(0,str(ROOT/"scripts"))
import prepare_offline_photos as commons
import audit_inaturalist_photos as photos

class Rc7dPhotoReliabilityTests(unittest.TestCase):
    def test_exact_nine_pinned_commons_images_required(self):
        lock=json.loads((ROOT/"docs/RC7_PINNED_COMMONS_PHOTO_SHA256.json").read_text())
        source=json.loads((ASSETS/"offline_photo_manifest_v1.json").read_text())
        approved={p["id"]:p for p in lock["items"]}
        self.assertEqual(lock["requiredCount"],9)
        self.assertEqual(len(approved),9)
        for item in source["items"]:
            if item["id"] not in approved:continue
            row=approved[item["id"]]
            self.assertEqual(row["offlineAsset"],item["offlineAsset"])
            self.assertEqual(row["commonsFile"],item["fileName"])
            self.assertRegex(row["sha256"],r"^[a-f0-9]{64}$")
        with tempfile.TemporaryDirectory() as tmp:
            path=Path(tmp)/"test.jpg"
            path.write_bytes(b"\xff\xd8\xff"+b"x"*1000+b"\xff\xd9")
            ref={"sha256":hashlib.sha256(path.read_bytes()).hexdigest()}
            self.assertTrue(commons.valid(path,ref))
            ref["sha256"]="0"*64
            self.assertFalse(commons.valid(path,ref))
        code=(ROOT/"scripts/prepare_offline_photos.py").read_text()
        self.assertIn('raise SystemExit("Pinned Commons media incomplete:',code)

    def test_photo_rights_49_entries_with_independent_licenses(self):
        items=photos.load_approval_lock()["items"]
        self.assertEqual(len(items),49)
        self.assertEqual(len({p["photoId"] for p in items}),49)
        self.assertEqual(sum(p.get("rightsBasis")==photos.OWNER_GRANT for p in items),26)
        self.assertEqual(sum(p.get("rightsBasis")!=photos.OWNER_GRANT for p in items),23)
        self.assertTrue(all(photos.valid_approved(p,photos.all_philippine_names()) for p in items))
        self.assertTrue(all(p["photoLicense"]=="all_rights_reserved"
            for p in items if p.get("rightsBasis")==photos.OWNER_GRANT))

    def test_gallery_read_only_and_attribution_labeled(self):
        src=(ROOT/"app/src/main/java/org/phwildlife/survey/OfflinePhotoEvidence.java").read_text()
        self.assertIn("View ",src)
        self.assertIn("additional research photos",src)
        self.assertIn("owner_direct_private_research_permission",src)
        self.assertIn("Photographer:",src)
        self.assertIn("rights",src.lower())
        self.assertNotIn("getWritableDatabase",src)

    def test_17_referenced_species_remain_pending_key_review(self):
        records=json.loads((ASSETS/"rc7_literature_batch_b_v1.json").read_text())["records"]
        self.assertEqual(len(records),48)
        enriched=[r for r in records
            if r["reviewStatus"]=="literature_synthesis_pending_specialist_review"]
        self.assertGreaterEqual(len(enriched),17)
        for record in enriched:
            self.assertGreaterEqual(len(record["facts"]),4)
            self.assertTrue(record["sourceUrl"].startswith("https://"))
            self.assertTrue(record["caution"])
            self.assertNotIn("specialist_approved",record["reviewStatus"])
        self.assertEqual(48-len(enriched),0)

    def test_device_instrumentation_and_upgrade_contract(self):
        folder=ROOT/"app/src/androidTest/java/org/phwildlife/survey"
        self.assertTrue((folder/"SpeciesLibraryInstrumentedTest.java").is_file())
        self.assertTrue((folder/"BackupRoundTripInstrumentedTest.java").is_file())
        smoke=(ROOT/"scripts/run_emulator_rc7_qa.sh").read_text()
        self.assertIn("adb install -r /tmp/likas-rc7c.apk",smoke)
        self.assertIn("adb install -r app/build/outputs/apk/debug/app-debug.apk",smoke)
        self.assertIn("cmp -s emulator-qa/previous-db.sqlite emulator-qa/after-upgrade-db.sqlite",smoke)
        self.assertIn("rc7_library_screen.png",smoke)
        self.assertIn("instrument -w -r",smoke)

if __name__=="__main__":
    unittest.main()
