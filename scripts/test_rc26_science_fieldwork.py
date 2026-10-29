"""RC26 scientific evidence and 200%-font Home/Setup/History workflow gates."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class Rc26ScienceAndFieldwork(unittest.TestCase):
    def test_new_primary_study_facts_keep_scope_and_coverage(self):
        batch=json.loads((ASSETS/"rc26_primary_trophic_montane_v1.json").read_text())
        corpus=json.loads((ASSETS/"rc18_unified_96_priority_accounts_v1.json").read_text())
        book=json.loads((ASSETS/"rc14_96_species_literature_index_v1.json").read_text())
        self.assertEqual(batch["recordCount"],4)
        self.assertEqual(corpus["prioritySpeciesCount"],96)
        self.assertEqual(corpus["sourceFactCount"],456)
        self.assertEqual(sum(p["referenceCount"] for p in book["profiles"]),306)
        self.assertEqual(corpus["sectionMissingCounts"],{
            "Identification":1,"Habitat":38,"Distribution":22,
            "Natural history":64,"Conservation":69})
        taxon={a["scientificName"]:a for a in corpus["accounts"]}
        refs={p["scientificName"]:{r["url"] for r in p["publications"]}
              for p in book["profiles"]}
        for fact in batch["records"]:
            self.assertIn(fact["scientificName"],taxon)
            self.assertIn(fact["url"],refs[fact["scientificName"]])
            self.assertIn(fact["section"],("Habitat","Natural history"))
            self.assertTrue(fact["sourceLocation"])
            self.assertTrue(fact["sourceType"])
            self.assertGreater(len(fact["studyScope"]),100)
            a=taxon[fact["scientificName"]]
            matches=[x for x in a["sections"][fact["section"]]
                     if x["text"]==fact["text"]
                     and x["origin"]=="rc26_primary_trophic_montane_v1.json"]
            self.assertEqual(len(matches),1)
        for a in corpus["accounts"]:
            self.assertEqual(a["coverage"]["citedBiologicalFacts"],
                sum(len(s) for s in a["sections"].values()))
            self.assertEqual(set(a["coverage"]["sectionsWithoutCitedData"]),
                {k for k,v in a["sections"].items() if not v})
        self.assertEqual(taxon["Dicaeum kampalili"]["sections"]["Identification"],[])
        mouse=[r for r in batch["records"] if r["scientificName"]=="Baletemys kampalili"]
        self.assertEqual(len(mouse),2)
        self.assertTrue(any("does not confirm" in r["studyScope"] for r in mouse))

    def test_compact_home_setup_history_habitat_and_phone_gate(self):
        home=(JAVA/"WelcomeActivity.java").read_text()
        history=(JAVA/"HistoryActivity.java").read_text()
        habitat=(JAVA/"HabitatRecordsActivity.java").read_text()
        test=(ROOT/"app/src/androidTest/java/org/phwildlife/survey/"
              "Rc10LibraryScreensInstrumentedTest.java").read_text()
        runner=(ROOT/"scripts/run_emulator_rc7_qa.sh").read_text()
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("setContentDescription(\"Continue \"+profile",home)
        self.assertIn("setupFooter.addView(setupStartButton",home)
        self.assertIn("largeText()?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL",home)
        self.assertIn("Filter saved surveys by outcome",history)
        self.assertIn("Search survey group, method, station or date",history)
        self.assertIn("FieldLayoutPolicy.actionHeightDp",habitat)
        self.assertIn("largeTextHomeSetupAndHistoryHaveAccessibleActions",test)
        for filename in ("home-200percent-font.png",
                         "survey-setup-200percent-font.png",
                         "history-200percent-font.png",
                         "species-library-200percent-font.png",
                         "habitats-200percent-font.png"):
            self.assertIn(filename,runner)
        self.assertIn("16 tests?",runner)
        self.assertIn("versionCode 89",gradle)
        self.assertIn("0.27.3-rc29-research-field-guide-preview",gradle)
        matrix=(ROOT/"docs/LIKAS_RC26_PHONE_FIELD_QA.md").read_text()
        self.assertIn("NO PHYSICAL PHONE TEST PERFORMED",matrix)
        self.assertIn("Complete",matrix)
        self.assertIn("Incomplete",matrix)
        self.assertIn("Not Counted",matrix)
        self.assertIn("GNSS",matrix)
        self.assertIn("WAV",matrix)

if __name__=="__main__":
    unittest.main()
