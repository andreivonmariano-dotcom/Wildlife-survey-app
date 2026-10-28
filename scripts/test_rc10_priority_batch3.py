"""RC10 third batch: exact source identities, restrained claims and UI linkage."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"

class BatchThreeEvidenceContracts(unittest.TestCase):
    def test_three_batches_have_distinct_source_taxa(self):
        packs=[json.loads((ASSETS/f"rc10_research_batch{i}_v1.json").read_text()) for i in (1,2,3)]
        profile=json.loads((ASSETS/"rc9_profile_completion_v1.json").read_text())
        expected={p["scientificName"]:p["group"] for p in profile["profiles"]}
        seen=set()
        for p in packs:
            self.assertEqual("rc10-research-batch-v1",p["schemaVersion"])
            self.assertIn("pending",p["reviewState"])
            for row in p["records"]:
                self.assertIn(row["scientificName"],expected)
                self.assertEqual(row["group"],expected[row["scientificName"]])
                self.assertNotIn(row["scientificName"],seen)
                seen.add(row["scientificName"])
                for statement in row["items"]:
                    self.assertTrue(statement["url"].startswith("https://"))
                    self.assertTrue(statement["source"])
                    self.assertTrue(statement["limitation"])
                    self.assertTrue(statement["statement"])
                    self.assertIn("pending",statement["reviewState"])
        self.assertEqual(len(seen),12)
        self.assertEqual(sum(len(r["items"]) for p in packs for r in p["records"]),64)
        self.assertEqual(profile["certifiedComplete"],0)

    def test_batch_three_dates_and_differential_cautions(self):
        d=json.loads((ASSETS/"rc10_research_batch3_v1.json").read_text())
        self.assertEqual("batch3",d["edition"])
        self.assertEqual(len(d["records"]),4)
        self.assertEqual(sum(len(x["items"]) for x in d["records"]),18)
        groups={r["scientificName"]:r["group"] for r in d["records"]}
        self.assertEqual(groups,{
            "Ansonia mcgregori":"Amphibians",
            "Limnonectes magnus":"Amphibians",
            "Acerodon jubatus":"Mammals",
            "Carlito syrichta":"Mammals"
        })
        for r in d["records"]:
            for claim in r["items"]:
                self.assertEqual("2026-10-28",claim["retrievedDate"])
                self.assertIsInstance(claim["sourceType"],str)
                self.assertTrue(claim["sourceType"])
                if claim["publicationYear"] is not None:
                    self.assertTrue(2002<=claim["publicationYear"]<=2026)
        self.assertIn("taxonomic correction",str(d["records"]).lower())
        screen=(CODE/"Rc10ResearchBatch.java").read_text()
        self.assertIn('rc10_research_batch3_v1.json',screen)
        self.assertIn('new JSONObject[]{source,secondSource,thirdSource}',screen)
        self.assertIn("publication date not stated",screen)

if __name__=="__main__":
    unittest.main()
