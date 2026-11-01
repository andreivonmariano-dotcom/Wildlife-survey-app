"""RC8B library content-depth audit, habitat association and photo comparison QA."""
import collections
import json
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/"app/src/main/assets"
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"
load=lambda name:json.loads((A/name).read_text(encoding="utf-8"))
M=load("rc8_evidence_comparisons_v1.json")
H=load("rc8_species_habitat_links_v1.json")
import sys
sys.path.insert(0,str(ROOT/"scripts"))
import audit_inaturalist_photos as photos
P={"items":photos.load_approval_lock()["items"]}
D=load("diagnostic_profiles_v1.json")
PRIORITY=load("rc7a_field_cards_v1.json")["accounts"]+load("rc7b_field_cards_v1.json")["accounts"]
CLAIMS={r["scientificName"]:r for r in M["species"]}

class Rc8bScientificAudit(unittest.TestCase):
    def test_not_all_1503_have_detailed_species_profiles(self):
        self.assertEqual(len(PRIORITY),96)
        self.assertEqual(len(D["profiles"]),17)
        self.assertEqual(sum(r["accountTier"]=="existing_source_linked_account"
            for r in PRIORITY),14)
        assert len(CLAIMS)==96
        self.assertEqual(sum(len(r["traits"]) for r in M["species"]),467)
        self.assertGreaterEqual(sum(t["review"]=="checklist_identity_only"
            for r in M["species"] for t in r["traits"]),96)

    def test_published_habitat_edges_are_literal_source_excerpts(self):
        self.assertEqual(H["schemaVersion"],"rc8-species-habitat-links-v1")
        self.assertEqual(len(H["edges"]),21)
        self.assertEqual(len({r["scientificName"] for r in H["edges"]}),15)
        self.assertEqual(len({r["habitatId"] for r in H["edges"]}),6)
        habitats={r["id"] for r in M["habitats"]}
        for row in H["edges"]:
            self.assertIn(row["habitatId"],habitats)
            self.assertFalse(row["siteOccurrence"])
            self.assertEqual(row["habitatMappingReview"],
                "provisional_physiognomic_crosswalk")
            self.assertEqual(row["sourceTaxonKey"],CLAIMS[row["scientificName"]]["sourceTaxonKey"])
            matches=[c for c in CLAIMS[row["scientificName"]]["traits"]
                if c["value"]==row["evidenceText"] and c["url"]==row["sourceUrl"]]
            self.assertGreaterEqual(len(matches),1,row["id"])
        self.assertIn("not yet reviewed",H["disclaimer"].lower())

    def test_49_photo_files_cover_only_33_taxa(self):
        self.assertEqual(len(P["items"]),49)
        self.assertEqual(len({p["taxon"] for p in P["items"]}),33)
        self.assertEqual(len({p["photoId"] for p in P["items"]}),49)
        self.assertEqual(sum(p.get("rightsBasis")=="owner_direct_private_research_permission"
            for p in P["items"]),26)
        self.assertTrue(all(p["photoPageUrl"].startswith("https://www.inaturalist.org/photos/")
            for p in P["items"]))

    def test_photo_comparison_preserves_rights_and_avoids_fake_regions(self):
        text=(CODE/"Rc8PhotoComparisonActivity.java").read_text()
        self.assertIn("OfflinePhotoEvidence.add(this,column",text)
        self.assertIn("No anatomical regions have been drawn",text)
        self.assertIn("source",text)
        self.assertIn("Not assessed",text)
        self.assertIn("Compare another photograph of species A",text)
        self.assertNotIn("getWritableDatabase",text)
        self.assertNotIn("INSERT INTO",text)
        self.assertIn("Rc8PhotoComparisonActivity.class",
            (CODE/"Rc7SpeciesLibraryActivity.java").read_text())

    def test_habitat_link_view_does_not_infer_absence(self):
        text=(CODE/"HabitatSpeciesEvidenceActivity.java").read_text()
        self.assertIn("Literature associations · Local occurrence unassessed",text)
        self.assertIn("No reviewed species links for this habitat.",text)
        self.assertIn('edge.optString("sourceTitle")',text)
        self.assertNotIn("getWritableDatabase",text)
        self.assertIn("HabitatSpeciesEvidenceActivity.class",
            (CODE/"HabitatRecordsActivity.java").read_text())
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        for cls in ("Rc8PhotoComparisonActivity","HabitatSpeciesEvidenceActivity"):
            self.assertIn('android:name=".'+cls+'"',manifest)

if __name__=="__main__": unittest.main()
