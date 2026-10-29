"""RC25: read-only Philippine primary ecology and non-startup large-text UX contracts."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

def load(name):
    return json.loads((ASSETS/name).read_text(encoding="utf-8"))

class Rc25PrimaryEcologyAccessibility(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.batch=load("rc25_primary_ecology_v1.json")
        cls.corpus=load("rc18_unified_96_priority_accounts_v1.json")
        cls.book=load("rc14_96_species_literature_index_v1.json")

    def test_corpus_and_source_counts(self):
        self.assertEqual(self.corpus["prioritySpeciesCount"],96)
        self.assertEqual(self.corpus["sourceFactCount"],456)
        self.assertEqual(self.batch["recordCount"],7)
        self.assertEqual(len(self.batch["records"]),7)
        self.assertEqual(sum(p["referenceCount"] for p in self.book["profiles"]),306)
        self.assertEqual(self.corpus["sectionMissingCounts"],{
            "Identification":1,"Habitat":38,"Distribution":22,
            "Natural history":64,"Conservation":69})
        self.assertEqual(self.corpus["sourceFactCount"],sum(len(facts)
            for account in self.corpus["accounts"]
            for facts in account["sections"].values()))
        for p in self.book["profiles"]:
            self.assertEqual(p["referenceCount"],len(p["publications"]))

    def test_each_new_observation_is_scoped_to_published_study(self):
        sections={}
        profiles={p["scientificName"]:p for p in self.book["profiles"]}
        accounts={a["scientificName"]:a for a in self.corpus["accounts"]}
        expected={
            "Pithecophaga jefferyi":(2,"https://doi.org/10.1676/01-054"),
            "Leptobrachium lumadorum":(2,"https://doi.org/10.3897/zookeys.624.9814"),
            "Cynocephalus volans":(3,"https://doi.org/10.2307/1383020"),
        }
        for row in self.batch["records"]:
            name=row["scientificName"]
            sections[name]=sections.get(name,0)+1
            self.assertEqual(row["url"],expected[name][1])
            self.assertIn(row["section"],("Habitat","Natural history"))
            self.assertEqual(row["sourceYear"],
                {"Pithecophaga jefferyi":2003,
                 "Leptobrachium lumadorum":2016,
                 "Cynocephalus volans":1998}[name])
            self.assertTrue(row["sourceLocation"])
            self.assertGreater(len(row["studyScope"]),75)
            self.assertTrue(row["sourceType"].startswith("peer_reviewed_"))
            self.assertIn(row["url"],[p["url"] for p in profiles[name]["publications"]])
            installed=[f for f in accounts[name]["sections"][row["section"]]
                if f["text"]==row["text"] and f["url"]==row["url"]
                and f["origin"]=="rc25_primary_ecology_v1.json"]
            self.assertEqual(len(installed),1)
        self.assertEqual(sections,{k:v[0] for k,v in expected.items()})
        # Original uninspected flowerpecker morphology remains unclaimed.
        self.assertEqual(accounts["Dicaeum kampalili"]["sections"]["Identification"],[])

    def test_large_text_screen_source_and_emulator_gates(self):
        library=(JAVA/"Rc7SpeciesLibraryActivity.java").read_text()
        habitat=(JAVA/"HabitatRecordsActivity.java").read_text()
        instrument=(ROOT/"app/src/androidTest/java/org/phwildlife/survey/"
            "Rc10LibraryScreensInstrumentedTest.java").read_text()
        runner=(ROOT/"scripts/run_emulator_rc7_qa.sh").read_text()
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn('largeText?"Species":"Philippine Species Library"',library)
        self.assertIn("scrollBody.addView(researchPanel)",library)
        self.assertIn("status.setContentDescription(evidenceStatus",library)
        self.assertIn('largeText?"Habitats":"Habitat & Ecology"',habitat)
        self.assertIn("scrollBody.addView(researchTools)",habitat)
        self.assertIn("getGlobalVisibleRect",instrument)
        self.assertIn("rc25_species_library_200percent.png",instrument)
        self.assertIn("rc25_habitats_200percent.png",instrument)
        self.assertIn("species-library-200percent-font.png",runner)
        self.assertIn("habitats-200percent-font.png",runner)
        self.assertIn("16 tests?",runner)
        self.assertIn("versionCode 91",gradle)
        self.assertIn("0.27.3-rc31-priority-diagnostics-preview",gradle)

if __name__=="__main__":
    unittest.main()
