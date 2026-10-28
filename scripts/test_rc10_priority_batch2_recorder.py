"""Research batch 2 claims, taxonomic linkage and live-recorder UI safety."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class Rc10BatchTwoContracts(unittest.TestCase):
    def test_source_limited_amphibian_mammal_research(self):
        pack=json.loads((ASSETS/"rc10_research_batch2_v1.json").read_text())
        complete=json.loads((ASSETS/"rc9_profile_completion_v1.json").read_text())
        names={p["scientificName"] for p in complete["profiles"]}
        self.assertEqual(pack["schemaVersion"],"rc10-research-batch-v1")
        self.assertEqual(pack["edition"],"batch2")
        expected={
            "Barbourula busuangensis":"Amphibians",
            "Platymantis insulatus":"Amphibians",
            "Manis culionensis":"Mammals",
            "Bubalus mindorensis":"Mammals",
        }
        self.assertEqual({p["scientificName"]:p["group"] for p in pack["records"]},expected)
        self.assertEqual(sum(len(p["items"]) for p in pack["records"]),24)
        self.assertEqual(complete["certifiedComplete"],0)
        for profile in pack["records"]:
            self.assertIn(profile["scientificName"],names)
            self.assertIn("pending",profile["reviewState"])
            for claim in profile["items"]:
                self.assertTrue(claim["source"])
                self.assertTrue(claim["url"].startswith("https://"))
                self.assertTrue(claim["limitation"])
                self.assertTrue(claim["statement"])
                self.assertIn("pending",claim["reviewState"])
                self.assertEqual(claim["retrievedDate"],"2026-10-28")
                if claim["publicationYear"] is not None:
                    self.assertGreaterEqual(claim["publicationYear"],2008)
                else:
                    self.assertIn(claim["sourceType"],("curated_taxonomic_reference","specialist_summary"))
                self.assertNotIn("siteOccurrence",claim)

    def test_both_research_packs_are_displayed_and_recorder_exit_retained(self):
        reader=(JAVA/"Rc10ResearchBatch.java").read_text()
        self.assertIn("rc10_research_batch1_v1.json",reader)
        self.assertIn("rc10_research_batch2_v1.json",reader)
        self.assertIn("new JSONObject[]{source,secondSource}",reader)
        self.assertIn("Read original reference",reader)
        recorder=(JAVA/"MainActivity.java").read_text()
        self.assertIn('UiKit.button(this,"Mark detection","secondary")',recorder)
        self.assertIn("Mark sighting time and available fresh observer GPS",recorder)
        self.assertIn("Show optional identification confidence, habitat, behavior and observation notes",recorder)
        self.assertIn('UiKit.button(this,"End Survey","outline-danger")',recorder)
        self.assertIn("endSurveyButton.setOnClickListener(v->finishPrompt())",recorder)
        self.assertIn("showNotCountedReason()",recorder)

if __name__=="__main__":
    unittest.main()
