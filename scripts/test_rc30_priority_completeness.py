"""RC30 primary/species-specialist evidence must be scoped, append-only and complete."""
from pathlib import Path
import json
import re
import unittest

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
SECTIONS=("Identification","Habitat","Distribution","Natural history","Conservation")
TARGET={("Pithecophaga jefferyi","Distribution"),
        ("Penelopides panini","Distribution"),
        ("Rhabdotorrhinus waldeni","Natural history"),
        ("Bubalus mindorensis","Natural history"),
        ("Varanus cumingi","Distribution"),
        ("Acerodon leucotis","Distribution"),
        ("Acerodon leucotis","Habitat"),
        ("Acerodon leucotis","Natural history")}

class PriorityCompletion(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.core=json.loads((ASSETS/"rc18_unified_96_priority_accounts_v1.json").read_text())
        cls.added=json.loads((ASSETS/"rc30_reviewed_priority_evidence_v1.json").read_text())

    def test_source_corpus_is_not_rewritten(self):
        self.assertEqual(self.core["prioritySpeciesCount"],96)
        self.assertEqual(self.core["sourceFactCount"],456)
        self.assertEqual(len(self.core["accounts"]),96)
        self.assertEqual(sum(len(v) for a in self.core["accounts"]
                             for v in a["sections"].values()),456)

    def test_8_records_fill_only_source_audited_missing_fields(self):
        records=self.added["records"]
        self.assertEqual(self.added["recordCount"],len(records))
        self.assertEqual(len(records),8)
        self.assertEqual({(r["scientificName"],r["section"]) for r in records},TARGET)
        accounts={a["scientificName"]:a for a in self.core["accounts"]}
        for r in records:
            self.assertIn(r["scientificName"],accounts)
            self.assertEqual(len(accounts[r["scientificName"]]["sections"][r["section"]]),0)
            for field in ("text","source","sourceType","sourceLocation","studyScope","label"):
                self.assertTrue(isinstance(r.get(field),str) and r[field].strip(),(r,field))
            self.assertTrue(r["url"].startswith("https://"))
            self.assertTrue(r["sourceYear"] is None or 1900<=r["sourceYear"]<=2026)
            self.assertNotIn("currently occurs",r["text"].lower())
            self.assertNotIn("observer location",r["text"].lower())

    def test_coverage_baseline_to_merged(self):
        copied=json.loads(json.dumps(self.core))
        for record in self.added["records"]:
            for a in copied["accounts"]:
                if a["scientificName"]==record["scientificName"]:
                    a["sections"][record["section"]].append(record)
                    break
        self.assertEqual(sum(len(v) for a in copied["accounts"]
                             for v in a["sections"].values()),464)
        self.assertEqual(sum(sum(bool(a["sections"][s]) for s in SECTIONS)
                             for a in copied["accounts"]),294)
        self.assertEqual(sum(all(a["sections"][s] for s in SECTIONS)
                             for a in copied["accounts"]),13)
        self.assertEqual({s:sum(not a["sections"][s] for a in copied["accounts"])
                          for s in SECTIONS},
            {"Identification":1,"Habitat":37,"Distribution":18,
             "Natural history":61,"Conservation":69})

    def test_global_iucn_types_remain_distinct_from_dated_denr(self):
        conservation=(JAVA/"Rc24ConservationDashboardActivity.java").read_text()
        for label in ("iucn_dated_assessment","dated_global_conservation_assessment",
                      "dated_global_iucn_assessment","dated_iucn_assessment"):
            self.assertIn('"'+label+'"',conservation)
        self.assertIn('isDatedIucn(kind)',conservation)
        self.assertIn('"government_national_conservation_record"',conservation)

    def test_all_display_paths_merge_same_read_only_data(self):
        cls=(JAVA/"Rc30SpeciesEvidence.java").read_text()
        self.assertIn('new JSONObject(corpus.toString())',cls)
        self.assertIn('rc30ReviewedAdditions',cls)
        for name in ("Rc11PrioritySpeciesView.java",
                     "Rc8TraitComparisonActivity.java",
                     "Rc24ConservationDashboardActivity.java"):
            self.assertIn("Rc30SpeciesEvidence.merge(",
                (JAVA/name).read_text(),name)
        apa=(JAVA/"VerifiedApaCatalog.java").read_text()
        self.assertEqual(apa.count('m.put("https://'),27)
        for url in {r["url"] for r in self.added["records"]}:
            self.assertIn(url,apa)

if __name__=="__main__":
    unittest.main()
