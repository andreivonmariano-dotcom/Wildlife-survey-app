"""RC27 science provenance, workflow boundaries and 200%-font navigation gates."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

def read(name):
    return json.loads((ASSETS/name).read_text(encoding="utf-8"))

class Rc27PhilippineHerpetofauna(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.notes=read("rc27_primary_herpetofauna_v1.json")
        cls.corpus=read("rc18_unified_96_priority_accounts_v1.json")
        cls.index=read("rc14_96_species_literature_index_v1.json")

    def test_corpus_and_missing_sections(self):
        self.assertEqual(self.notes["recordCount"],4)
        self.assertEqual(self.corpus["prioritySpeciesCount"],96)
        self.assertEqual(self.corpus["sourceFactCount"],456)
        self.assertEqual(sum(x["referenceCount"] for x in self.index["profiles"]),306)
        self.assertEqual(self.corpus["sectionMissingCounts"],{
            "Identification":1,"Habitat":38,"Distribution":22,
            "Natural history":64,"Conservation":69})
        for account in self.corpus["accounts"]:
            sections=account["sections"]
            self.assertEqual(account["coverage"]["citedBiologicalFacts"],
                sum(map(len,sections.values())))
            self.assertEqual(set(account["coverage"]["sectionsWithoutCitedData"]),
                {key for key,value in sections.items() if not value})
        missing={x["scientificName"] for x in self.corpus["accounts"]
            if not x["sections"]["Identification"]}
        self.assertEqual(missing,{"Dicaeum kampalili"})

    def test_new_records_are_exact_primary_source_matches(self):
        names={"Hydrosaurus pustulatus":(2016,"https://doi.org/10.3897/zookeys.624.9814"),
            "Varanus marmoratus":(2013,"https://doi.org/10.3897/zookeys.266.3982"),
            "Varanus dalubhasa":(2021,"https://doi.org/10.1016/j.japb.2020.11.005")}
        accounts={a["scientificName"]:a for a in self.corpus["accounts"]}
        refs={p["scientificName"]:{x["url"] for x in p["publications"]}
            for p in self.index["profiles"]}
        seen=set()
        for fact in self.notes["records"]:
            name=fact["scientificName"]
            self.assertIn(name,names)
            self.assertEqual((fact["sourceYear"],fact["url"]),names[name])
            self.assertTrue(fact["sourceType"].startswith("peer_reviewed_"))
            self.assertGreater(len(fact["studyScope"]),75)
            self.assertTrue(fact["sourceLocation"])
            self.assertIn(fact["url"],refs[name])
            self.assertNotIn((name,fact["section"],fact["text"]),seen)
            seen.add((name,fact["section"],fact["text"]))
            found=[f for f in accounts[name]["sections"][fact["section"]]
                if f["text"]==fact["text"] and f["url"]==fact["url"]
                and f["origin"]=="rc27_primary_herpetofauna_v1.json"]
            self.assertEqual(len(found),1)

    def test_large_font_header_and_phone_safety(self):
        ui=(JAVA/"UiKit.java").read_text()
        habitat=(JAVA/"HabitatRecordsActivity.java").read_text()
        setup=(JAVA/"WelcomeActivity.java").read_text()
        history=(JAVA/"HistoryActivity.java").read_text()
        instrument=(ROOT/"app/src/androidTest/java/org/phwildlife/survey/Rc10LibraryScreensInstrumentedTest.java").read_text()
        build=(ROOT/"app/build.gradle").read_text()
        qa=(ROOT/"docs/LIKAS_RC27_PHONE_FIELD_QA.md").read_text()
        self.assertIn("if(largeText)back.setMinHeight(dp(a,48))",ui)
        self.assertIn("SOFT_INPUT_STATE_ALWAYS_HIDDEN",habitat)
        self.assertIn("tabs.setBaselineAligned(false)",habitat)
        self.assertIn("root.addView(tabs,new LinearLayout.LayoutParams(-1,dp(tabHeight)))",habitat)
        self.assertIn("largeTextHomeSetupAndHistoryHaveAccessibleActions",instrument)
        self.assertIn("largeTextSpeciesAndHabitatsPreserveFieldControls",instrument)
        self.assertIn("Continue to GPS",setup)
        self.assertIn("outcome",history)
        self.assertIn("NO PHYSICAL HANDSET TEST HAS BEEN PERFORMED",qa)
        self.assertIn("versionCode 87",build)
        self.assertIn("0.27.3-rc27c-habitat-tab-alignment-preview",build)

if __name__=="__main__":
    unittest.main()
