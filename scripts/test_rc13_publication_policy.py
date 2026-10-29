"""Publication-first LIKAS science and copy rules for the 96 priority accounts."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class PublicationFirstPolicy(unittest.TestCase):
    def test_all_priority_names_and_eleven_publication_categories(self):
        old=json.loads((ASSETS/"rc9_profile_completion_v1.json").read_text())
        now=json.loads((ASSETS/"rc13_publication_coverage_v1.json").read_text())
        self.assertEqual(96,now["profileCount"])
        self.assertEqual("rc13-publication-coverage-v1",now["schemaVersion"])
        self.assertEqual(11,len(now["fieldNames"]))
        self.assertNotIn("review",now["fieldNames"])
        self.assertNotIn("expertApprovedCount",now)
        before={x["scientificName"]:x for x in old["profiles"]}
        after={x["scientificName"]:x for x in now["profiles"]}
        self.assertEqual(set(before),set(after))
        for name,p in after.items():
            self.assertEqual(p["sourceTaxonKey"],before[name]["sourceTaxonKey"])
            self.assertEqual(set(p["fields"]),set(now["fieldNames"]))
            self.assertNotIn("expertSignoff",p)
            self.assertNotIn("diagnosticKeyApproved",p)
            for field,data in p["fields"].items():
                self.assertEqual(data["previousSourceCount"],
                                 before[name]["requirements"][field]["count"])

    def test_peer_reviewed_batch_citations_and_short_facts(self):
        pack=json.loads((ASSETS/"rc13_peer_reviewed_batch1_v1.json").read_text())
        self.assertEqual(pack["schemaVersion"],"rc13-peer-reviewed-research-v1")
        self.assertEqual(5,pack["taxonCount"])
        self.assertEqual(15,pack["claimCount"])
        self.assertEqual(15,sum(len(x["items"]) for x in pack["records"]))
        registry=json.loads((ASSETS/"rc13_publication_coverage_v1.json").read_text())
        allowed=set(x["scientificName"] for x in registry["profiles"])
        for taxon in pack["records"]:
            self.assertIn(taxon["scientificName"],allowed)
            for fact in taxon["items"]:
                self.assertTrue(fact["url"].startswith("https://"))
                self.assertIn("peer_reviewed",fact["evidenceType"])
                self.assertGreaterEqual(fact["publicationYear"],1996)
                self.assertTrue(fact["sourceLocation"])
                self.assertTrue(fact["scopeLimit"])
                self.assertLessEqual(len(fact["statement"]),230)
                self.assertNotIn("specialist",fact["statement"].lower())
        self.assertFalse(any(fact["label"]=="Later Sibuyan records"
                             for taxon in pack["records"] for fact in taxon["items"]))

    def test_user_facing_copy_uses_publications_not_signoffs(self):
        view=(JAVA/"Rc11PrioritySpeciesView.java").read_text()
        panel=(JAVA/"Rc12ScientificReview.java").read_text()
        editorial=(JAVA/"Rc13FieldGuideCopy.java").read_text()
        self.assertIn("rc13_peer_reviewed_batch1_v1.json",view)
        self.assertIn("Rc13FieldGuideCopy.fieldText",view)
        self.assertIn("Bibliography",view)
        self.assertIn("Literature coverage",panel)
        self.assertIn("rc13_publication_coverage_v1.json",panel)
        self.assertNotIn("Specialist sign-off",panel)
        self.assertNotIn("Independent specialist approval",panel)
        self.assertNotIn("specialist review in progress",view)
        self.assertIn("EDITORIAL_TAILS",editorial)

if __name__=="__main__":
    unittest.main()
