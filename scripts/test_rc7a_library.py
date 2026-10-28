"""RC7A contract tests: private checklist provenance, safe media, and read-only UI."""
import json
from pathlib import Path
import sys
import unittest
from unittest.mock import patch

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"
sys.path.insert(0,str(ROOT/"scripts"))
import audit_inaturalist_photos as photo

class Rc7aLibraryTests(unittest.TestCase):
    def test_48_matched_names_exactly_12_per_source(self):
        p=json.loads((ASSETS/"rc7a_field_cards_v1.json").read_text())
        priority=json.loads((ASSETS/"rc7_priority_batches_v1.json").read_text())
        self.assertEqual(p["schemaVersion"],"rc7a-priority-cards-1")
        self.assertEqual(len(p["accounts"]),48)
        self.assertEqual(p["accountCount"],48)
        expected={a["sourceTaxonKey"] for b in priority["batches"]
                  if b["wave"]=="RC7A" for a in b["entries"]}
        actual={a["sourceTaxonKey"] for a in p["accounts"]}
        self.assertEqual(expected,actual)
        self.assertEqual(len(expected),48)
        for group in ("Birds","Amphibians","Reptiles","Mammals"):
            self.assertEqual(12,sum(a["group"]==group for a in p["accounts"]))

    def test_no_fake_ecology_or_status(self):
        cards=json.loads((ASSETS/"rc7a_field_cards_v1.json").read_text())["accounts"]
        for a in cards:
            for key in ("habitat","elevationMinM","elevationMaxM","iucnCategory",
                        "denrStatus","regionalOccurrence"):
                self.assertIsNone(a[key],(a["scientificName"],key))
            pinned={x["taxon"] for x in photo.load_approval_lock()["items"]}
            expected="licensed_photo_hash_pinned" if a["scientificName"] in pinned else "no_photo_verified"
            self.assertEqual(a["photoState"],expected)
            self.assertEqual(a["photoSourcePriority"],
                "inaturalist:licensed_contributor" if a["scientificName"] in pinned
                else "inaturalist:dreidrei")
            self.assertTrue(a["sourceTaxonKey"])
            self.assertTrue(a["sourceCitation"])
            self.assertTrue(a["sourceUrl"].startswith("https://"))

    def test_manifest_never_claims_download_without_source(self):
        m=json.loads((ASSETS/"offline_inaturalist_manifest_v1.json").read_text())
        lock=photo.load_approval_lock()
        self.assertEqual(m["items"],[]) # Generated only from the reviewed lock at build time.
        self.assertEqual(len(lock["items"]),23)
        self.assertEqual(len({x["photoId"] for x in lock["items"]}),23)
        targets=photo.all_philippine_names()
        for item in lock["items"]:
            self.assertTrue(photo.valid_approved(item,targets))
            self.assertNotEqual(item["observer"].casefold(),"dreidrei")
            self.assertIn(item["photoLicense"],{"cc-by","cc-by-nc"})

    def test_api_proposals_reject_all_rights_reserved(self):
        obs={"id":123,"quality_grade":"research",
             "user":{"login":"dreidrei"},
             "taxon":{"rank":"species","name":"Pithecophaga jefferyi"},
             "photos":[
                {"id":45,"license_code":None,
                 "url":"https://inaturalist-open-data.s3.amazonaws.com/photos/45/medium.jpg",
                 "attribution":"© dreidrei"},
                {"id":46,"license_code":"cc-by-nc",
                 "url":"https://inaturalist-open-data.s3.amazonaws.com/photos/46/medium.jpg",
                 "attribution":"© licensed photographer"},
                {"id":47,"license_code":"cc-by-nc-nd",
                 "url":"https://inaturalist-open-data.s3.amazonaws.com/photos/47/medium.jpg",
                 "attribution":"© another photographer"}
             ]}
        data=photo.candidates_from_results([obs],{"Pithecophaga jefferyi"})
        self.assertEqual(len(data),1)
        self.assertEqual(data[0]["photoId"],46)
        self.assertEqual(data[0]["approvalState"],
                         "needs_independent_photo_and_taxon_review")

    def test_non_source_observer_or_different_taxon_rejected(self):
        obs={"id":123,"quality_grade":"research","user":{"login":"other"},
             "taxon":{"rank":"species","name":"Pithecophaga jefferyi"},
             "photos":[{"id":46,"license_code":"cc-by",
               "url":"https://inaturalist-open-data.s3.amazonaws.com/photos/46/medium.jpg",
               "attribution":"© authorized photographer"}]}
        self.assertEqual([],photo.candidates_from_results([obs],{"Pithecophaga jefferyi"}))
        obs["user"]["login"]="dreidrei"
        self.assertEqual([],photo.candidates_from_results([obs],{"Varanus olivaceus"}))

    def test_prevent_unapproved_or_unpinned_media(self):
        self.assertFalse(photo.valid_approved({"photoId":10},
            {"Pithecophaga jefferyi"}))
        with self.assertRaises(ValueError):
            photo.package_approved([{"photoId":10}],{"Pithecophaga jefferyi"},
                fetcher=lambda url:b"fake")
        self.assertEqual(photo.LICENSES,{"cc0","cc-by","cc-by-sa",
              "cc-by-nc","cc-by-nc-sa"})

    def test_ui_filters_preserve_offline_source_links(self):
        screen=(CODE/"Rc7SpeciesLibraryActivity.java").read_text()
        for token in ("taxon_catalog","taxon_synonyms",
                      "Rc7FieldAccounts.priority","Published literature notes",
                      "Offline photograph","RegionalChecklistActivity",
                      "DiagnosticGuideActivity","taxon_key","LIMIT 2200"):
            self.assertIn(token,screen)
        detail=(CODE/"TaxonDetailActivity.java").read_text()
        self.assertIn("Rc7FieldAccounts.add(this,content,scientific)",detail)
        self.assertIn("OfflineFieldLibrary.addSpecies(this,content,scientific)",detail)
        self.assertIn("iucn_red_list",detail)
        self.assertIn("ph_red_list",detail)
        self.assertIn("taxon_occurrence_evidence",detail)

    def test_home_and_catalog_open_new_library(self):
        for n in ("WelcomeActivity","CatalogActivity"):
            self.assertIn("Rc7SpeciesLibraryActivity.class",
                          (CODE/(n+".java")).read_text())
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        self.assertIn('android:name=".Rc7SpeciesLibraryActivity"',manifest)
        self.assertIn("new Intent(a,DiagnosticGuideActivity.class)",
                      (CODE/"Rc7FieldAccounts.java").read_text())

    def test_never_mutate_historic_observations_for_priority_cards(self):
        src=(CODE/"Rc7FieldAccounts.java").read_text()
        self.assertNotIn("execSQL(",src)
        self.assertNotIn("update(",src)
        self.assertNotIn("delete(",src)
        self.assertIn("sourceCitation",src)
        self.assertIn("scientificLimits",src)

if __name__=="__main__":unittest.main()
