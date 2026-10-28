"""RC7B quality requirements for 34 literature notes and research photo assets."""
from pathlib import Path
import json
import sys
import unittest

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"
sys.path.insert(0,str(ROOT/"scripts"))
import audit_inaturalist_photos as photos

class Rc7bFieldGuideTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        first=json.loads((ASSETS/"rc7_literature_birds_amphibians_v1.json").read_text())
        second=json.loads((ASSETS/"rc7_literature_reptiles_mammals_v1.json").read_text())
        cls.notes=first["records"]+second["records"]
        cls.cards=json.loads((ASSETS/"rc7a_field_cards_v1.json").read_text())["accounts"]
        cls.lock=photos.load_approval_lock()["items"]

    def test_every_source_priority_draft_has_literature(self):
        draft={x["scientificName"] for x in self.cards
               if x["accountTier"]=="source_catalog_priority_draft"}
        completed={x["scientificName"] for x in self.notes}
        self.assertEqual(len(draft),34)
        self.assertEqual(len(self.notes),34)
        self.assertEqual(draft,completed)

    def test_every_assertion_has_an_original_reference(self):
        for item in self.notes:
            with self.subTest(name=item["scientificName"]):
                self.assertEqual(item["reviewStatus"],
                                 "literature_synthesis_pending_specialist_review")
                self.assertTrue(item["sourceUrl"].startswith("https://"))
                self.assertTrue(item["sourceTitle"])
                self.assertGreaterEqual(len(item["facts"]),2)
                self.assertTrue(item["caution"])
                for fact in item["facts"]:
                    self.assertTrue(fact["field"])
                    self.assertGreaterEqual(len(fact["value"]),35)
                    self.assertEqual(fact["sourceUrl"],item["sourceUrl"])

    def test_reviewed_media_is_only_noncommercial_compatible_cc(self):
        self.assertEqual(len(self.lock),23)
        self.assertEqual(len({x["photoId"] for x in self.lock}),23)
        targets=photos.all_philippine_names()
        for item in self.lock:
            self.assertTrue(photos.valid_approved(item,targets))
            self.assertIn(item["photoLicense"],("cc-by","cc-by-nc"))
            self.assertNotEqual(item["observer"].casefold(),"dreidrei")
            self.assertEqual(item["approvalState"],"approved_private_research")
            self.assertIn("pending",item["reviewedBy"])

    def test_ui_safeguards_and_photo_cards(self):
        view=(CODE/"Rc7SpeciesLibraryActivity.java").read_text()
        self.assertIn("screenDp<385f",view)
        self.assertIn("OfflinePhotoEvidence.thumbnailForTaxon(",view)
        self.assertIn("Rc7LiteratureNotes.available(",view)
        self.assertIn('if(filter==3&&!OfflinePhotoEvidence.hasOfflineTaxon',view)
        detail=(CODE/"TaxonDetailActivity.java").read_text()
        self.assertIn("Rc7LiteratureNotes.add(this,content,scientific)",detail)
        ref=(CODE/"Rc7LiteratureNotes.java").read_text()
        self.assertIn("literature_synthesis_pending_specialist_review",ref)
        self.assertNotIn("getWritableDatabase",ref)

    def test_device_upgrade_data_schema_unchanged(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        database=(CODE/"FieldDb.java").read_text()
        self.assertIn("versionCode 53",gradle)
        self.assertIn("0.27.3-rc7b-literature-inat-preview",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("super(context,NAME,null,27)",database)

if __name__=="__main__":unittest.main()
