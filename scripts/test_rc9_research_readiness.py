import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSET=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
load=lambda s:json.loads((ASSET/s).read_text())

class ResearchReviewContracts(unittest.TestCase):
    def test_profile_coverage(self):
        d=load("rc9_profile_completion_v1.json")
        self.assertEqual(len(d["profiles"]),96)
        self.assertEqual(d["certifiedComplete"],0)
        self.assertEqual(len(d["requirementDefinitions"]),12)
        self.assertEqual(d["sourceObservedCoverage"]["media"],33)
        self.assertEqual(d["sourceObservedCoverage"]["differential"],4)
        self.assertEqual(d["sourceObservedCoverage"]["review"],0)
        keys={p["sourceTaxonKey"] for p in d["profiles"]}
        self.assertEqual(len(keys),96)
        self.assertFalse(any(p["readyForCertifiedIdentification"] for p in d["profiles"]))
    def test_cited_differential(self):
        data=load("rc9_differential_traits_v1.json")
        self.assertEqual(len(data["pairs"]),2)
        self.assertEqual(sum(len(p["evidence"]) for p in data["pairs"]),8)
        for p in data["pairs"]:
            for trait in p["evidence"]:
                self.assertTrue(trait["sourceA"].startswith("https://"))
                self.assertTrue(trait["sourceB"].startswith("https://"))
                self.assertIn("pending",trait["review"])
                self.assertTrue(trait["part"] and trait["stage"])
    def test_no_fabricated_approved_anatomy(self):
        approved=load("rc9_approved_photo_annotations_v1.json")
        self.assertEqual(approved["items"],[])
        s=(JAVA/"Rc9PhotoAnnotationReviewActivity.java").read_text()
        self.assertIn('"pending_expert_review"',s)
        self.assertIn('"owner_direct_private_research_permission"',s)
        self.assertIn("utc.setTimeZone",s)
        self.assertNotIn("getWritableDatabase",s)
        registry=(JAVA/"Rc9ApprovedPhotoMarkers.java").read_text()
        self.assertIn('"expert_approved"',registry)
        self.assertIn("photoSha256",registry)
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        self.assertIn('android:name=".Rc9PhotoAnnotationReviewActivity"',manifest)
        self.assertIn('android:name=".Rc9DifferentialActivity"',manifest)
        self.assertIn("Rc9ProfileCompleteness.add",(JAVA/"TaxonDetailActivity.java").read_text())

if __name__=="__main__": unittest.main()
