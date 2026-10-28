"""RC7C public CC and copyright-owner photo authorization and source library QA."""
import json
from pathlib import Path
import sys
import unittest
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"
sys.path.insert(0,str(ROOT/"scripts"))
import audit_inaturalist_photos as photos
import collect_owner_dreidrei_photos as owner

class Rc7cOwnerLibraryTests(unittest.TestCase):
    def test_all_96_source_priority_ids_present_and_distinct(self):
        plan=json.loads((ASSETS/"rc7_priority_batches_v1.json").read_text())
        a=json.loads((ASSETS/"rc7a_field_cards_v1.json").read_text())["accounts"]
        b=json.loads((ASSETS/"rc7b_field_cards_v1.json").read_text())["accounts"]
        self.assertEqual(len(a),48)
        self.assertEqual(len(b),48)
        self.assertEqual(len({x["sourceTaxonKey"] for x in a+b}),96)
        expected={r["sourceTaxonKey"] for wave in plan["batches"] for r in wave["entries"]}
        self.assertEqual(expected,{r["sourceTaxonKey"] for r in a+b})
        for group in ("Birds","Reptiles","Amphibians","Mammals"):
            self.assertEqual(24,sum(r["group"]==group for r in a+b))
        self.assertIn("rc7b_field_cards_v1.json",(CODE/"Rc7FieldAccounts.java").read_text())
        self.assertIn('Priority species (96)',(CODE/"Rc7SpeciesLibraryActivity.java").read_text())

    def test_new_taxon_sources_are_labeled_not_fake_biology(self):
        pack=json.loads((ASSETS/"rc7_literature_batch_b_v1.json").read_text())
        self.assertEqual(len(pack["records"]),48)
        self.assertTrue(all(r["sourceUrl"].startswith("https://") for r in pack["records"]))
        for r in pack["records"]:
            self.assertEqual("source_taxonomy_reviewed_biology_pending",r["reviewStatus"])
            self.assertTrue(r["caution"])
            self.assertGreaterEqual(len(r["facts"]),3)
            self.assertTrue(all(f["sourceUrl"]==r["sourceUrl"] for f in r["facts"]))
        source=(CODE/"Rc7LiteratureNotes.java").read_text()
        self.assertIn("rc7_literature_batch_b_v1.json",source)
        self.assertIn("species ecology still unreviewed",source)

    def test_owned_photo_grant_is_scoped_and_has_original_rights(self):
        lock=json.loads((ROOT/"docs/RC7_DREIDREI_OWNER_APPROVED_LOCK_V1.json").read_text())
        owned=lock["items"]
        self.assertEqual(len(owned),14)
        self.assertEqual(len({r["photoId"] for r in owned}),14)
        self.assertEqual(lock["permission"],photos.OWNER_GRANT)
        permitted=photos.all_philippine_names()
        for r in owned:
            self.assertIn(r["taxon"],permitted)
            self.assertEqual(r["observer"],"dreidrei")
            self.assertEqual(r["photoLicense"],"all_rights_reserved")
            self.assertEqual(r["rightsBasis"],photos.OWNER_GRANT)
            self.assertTrue(photos.valid_approved(r,permitted))
        permission=(ROOT/"docs/LIKAS_DREIDREI_PRIVATE_PHOTO_PERMISSION_2026_10_28.md").read_text()
        self.assertIn("private, noncommercial",permission)
        self.assertIn("No public redistribution grant",permission)

    def test_original_cc_photos_and_owned_photos_not_confused(self):
        entries=photos.load_approval_lock()["items"]
        self.assertEqual(len(entries),37)
        self.assertEqual(len({r["photoId"] for r in entries}),37)
        owners=[r for r in entries if r.get("rightsBasis")==photos.OWNER_GRANT]
        cc=[r for r in entries if r.get("rightsBasis")!=photos.OWNER_GRANT]
        self.assertEqual(len(owners),14)
        self.assertEqual(len(cc),23)
        self.assertEqual(entries[:14],owners)
        self.assertTrue(all(r["photoLicense"] in photos.LICENSES for r in cc))

    def test_forged_ownership_rejected(self):
        item=dict(json.loads((ROOT/"docs/RC7_DREIDREI_OWNER_APPROVED_LOCK_V1.json").read_text())["items"][0])
        targets=photos.all_philippine_names()
        for k,v in [("observer","different"),
                    ("photoAttribution","(c) someone else"),
                    ("photoLicense","cc-by"),
                    ("permissionSource","docs/not_real.md"),
                    ("mediumUrl","https://evil.example/anything.jpg"),
                    ("rightsBasis","unknown_grant")]:
            altered=dict(item);altered[k]=v
            self.assertFalse(photos.valid_approved(altered,targets),(k,v))
        self.assertFalse(owner.candidate_photo(
            {"id":1,"user":{"login":"other"},"taxon":{"rank":"species","name":"Cynocephalus volans"}},
            {"id":1,"attribution":"(c) Andrei Von Tirona",
             "url":"https://static.inaturalist.org/photos/1/medium.jpg"},
            {"Cynocephalus volans"}))

    def test_visual_alignment_and_no_storage_migration(self):
        browser=(CODE/"Rc7SpeciesLibraryActivity.java").read_text()
        self.assertIn("screenDp<385f",browser)
        self.assertIn("new LinearLayout.LayoutParams(-1,dp(46))",browser)
        self.assertIn("PAGE=24",browser)
        self.assertIn("OfflinePhotoEvidence.hasOfflineTaxon",browser)
        self.assertIn("taxon_synonyms",browser)
        db=(CODE/"FieldDb.java").read_text()
        self.assertIn("super(context,NAME,null,27)",db)
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 54",gradle)
        self.assertIn("0.27.3-rc7c-owner-photos-preview",gradle)

if __name__=="__main__":unittest.main()
