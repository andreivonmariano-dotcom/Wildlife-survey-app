"""RC20: source-typed biology, dated global conservation, compact evidence UI."""
import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"

class Rc20SourcedSpecies(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.batch=json.loads((ASSETS/"rc20_verified_gap_batch_v1.json").read_text())
        cls.accounts=json.loads((ASSETS/"rc18_unified_96_priority_accounts_v1.json").read_text())
        cls.book=json.loads((ASSETS/"rc14_96_species_literature_index_v1.json").read_text())

    def test_source_coverage_counts_and_96_unchanged(self):
        self.assertEqual(self.batch["addedCount"],16)
        self.assertEqual(len(self.batch["facts"]),16)
        self.assertEqual(len(set(self.batch["targetedTaxa"])),9)
        self.assertEqual(len(self.accounts["accounts"]),96)
        self.assertEqual(self.accounts["sourceFactCount"],393)
        self.assertEqual(sum(p["referenceCount"] for p in self.book["profiles"]),269)
        self.assertEqual(self.accounts["sectionMissingCounts"],{
            "Identification":18,"Habitat":51,"Distribution":24,
            "Natural history":75,"Conservation":76})
        for account in self.accounts["accounts"]:
            fact_count=sum(len(v) for v in account["sections"].values())
            self.assertEqual(account["coverage"]["citedBiologicalFacts"],fact_count)
            empty={k for k,v in account["sections"].items() if not v}
            self.assertEqual(empty,set(account["coverage"]["sectionsWithoutCitedData"]))

    def test_all_new_facts_are_tied_to_original_matching_urls(self):
        accounts={a["scientificName"]:a for a in self.accounts["accounts"]}
        refs={p["scientificName"]:{r["url"] for r in p["publications"]} for p in self.book["profiles"]}
        for row in self.batch["facts"]:
            self.assertIn(row["scientificName"],accounts)
            self.assertIn(row["url"],refs[row["scientificName"]])
            self.assertTrue(row["sourceLocation"])
            self.assertTrue(row["studyScope"])
            self.assertTrue(row["sourceType"])
            actual=accounts[row["scientificName"]]["sections"][row["section"]]
            self.assertTrue(any(x["text"]==row["text"] and x["sourceType"]==row["sourceType"]
                                and x["url"]==row["url"] for x in actual))
            self.assertFalse(any(x["sourceType"]=="original_species_description"
                                 and "BirdForum" in x["source"] for x in actual))
        self.assertTrue(any(f["sourceType"]=="original_species_description"
                            for f in self.batch["facts"]))
        self.assertTrue(any(f["sourceType"]=="curated_secondary_species_account"
                            for f in self.batch["facts"]))

    def test_conservation_not_relabelled_as_national_or_current(self):
        assessments=[x for x in self.batch["facts"] if x["section"]=="Conservation"]
        self.assertEqual({x["scientificName"] for x in assessments},
                         {"Rusa alfredi","Ceyx argentatus"})
        self.assertEqual({x["sourceYear"] for x in assessments},{2016,2022})
        for item in assessments:
            self.assertIn("global",item["text"])
            self.assertNotIn("DENR status:",item["text"])

    def test_accessible_topic_coverage_and_preview_version(self):
        view=(ROOT/"app/src/main/java/org/phwildlife/survey/Rc11PrioritySpeciesView.java").read_text()
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn('of 5 research topics documented',view)
        self.assertIn('Close cited sources and study details',view)
        self.assertIn('Collapse ',view)
        self.assertIn("versionCode 73",gradle)
        self.assertIn("0.27.3-rc20-documented-species-preview",gradle)

if __name__=="__main__":
    unittest.main()
