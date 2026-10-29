"""RC10 first scientific batch and non-startup navigation contract tests."""
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
JAVA = ROOT / "app/src/main/java/org/phwildlife/survey"

class Rc10FirstResearchBatch(unittest.TestCase):
    def test_cited_biology_is_provisional_and_source_bounded(self):
        d = json.loads((ASSETS / "rc10_research_batch1_v1.json").read_text())
        p = json.loads((ASSETS / "rc9_profile_completion_v1.json").read_text())
        self.assertEqual(d["schemaVersion"], "rc10-research-batch-v1")
        self.assertIn("pending", d["reviewState"])
        keys = {x["scientificName"] for x in p["profiles"]}
        self.assertEqual(len(d["records"]), 4)
        self.assertEqual({r["scientificName"] for r in d["records"]},
            {"Penelopides mindorensis", "Penelopides affinis",
             "Varanus mabitang", "Varanus olivaceus"})
        for record in d["records"]:
            self.assertIn(record["scientificName"], keys)
            self.assertIn("pending",record["reviewState"])
            for claim in record["items"]:
                self.assertTrue(claim["url"].startswith("https://"))
                self.assertTrue(claim["source"])
                self.assertTrue(claim["statement"])
                self.assertTrue(claim["limitation"])
                self.assertIn("pending",claim["reviewState"])
                self.assertNotIn("siteOccurrence", claim)
        self.assertEqual(p["certifiedComplete"], 0)

    def test_reference_ui_and_field_screen_remain_safe(self):
        renderer=(JAVA/"Rc10ResearchBatch.java").read_text()
        readiness=(JAVA/"Rc9ProfileCompleteness.java").read_text()
        home=(JAVA/"WelcomeActivity.java").read_text()
        habitat=(JAVA/"HabitatRecordsActivity.java").read_text()
        recorder=(JAVA/"MainActivity.java").read_text()
        self.assertIn("rc10_research_batch1_v1.json", renderer)
        self.assertIn("Rc10ResearchBatch.add(a,parent,scientific)",readiness)
        self.assertIn('LikasPortalUi.hero(this)',home)
        self.assertIn("Habitat research tools",habitat)
        self.assertIn("researchTools.setVisibility(android.view.View.GONE)", habitat)
        self.assertIn('"End Survey"',recorder)
        self.assertIn("finishPrompt()",recorder)

if __name__ == "__main__":
    unittest.main()
