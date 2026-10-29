"""RC21 verifies attributed diagnostics, dated conservation and field-accessibility gates."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class Rc21EndemicsFieldwork(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.batch=json.loads((ASSETS/"rc21_diagnosis_conservation_batch_v1.json").read_text())
        cls.corpus=json.loads((ASSETS/"rc18_unified_96_priority_accounts_v1.json").read_text())
        cls.bibliography=json.loads((ASSETS/"rc14_96_species_literature_index_v1.json").read_text())

    def test_six_identity_gaps_resolved_and_twelve_still_open(self):
        self.assertEqual(self.batch["newFactCount"],20)
        self.assertEqual(len(self.batch["facts"]),20)
        self.assertEqual(self.corpus["sourceFactCount"],441)
        self.assertEqual(sum(x["referenceCount"] for x in self.bibliography["profiles"]),299)
        self.assertEqual(len(self.corpus["accounts"]),96)
        self.assertEqual(self.corpus["sectionMissingCounts"],{
            "Identification":1,"Habitat":42,"Distribution":23,
            "Natural history":69,"Conservation":69})
        names={a["scientificName"] for a in self.corpus["accounts"]}
        self.assertEqual(names,{p["scientificName"] for p in self.bibliography["profiles"]})
        for account in self.corpus["accounts"]:
            total=sum(len(x) for x in account["sections"].values())
            self.assertEqual(account["coverage"]["citedBiologicalFacts"],total)
            self.assertEqual(set(account["coverage"]["sectionsWithoutCitedData"]),
                             {k for k,v in account["sections"].items() if not v})

    def test_every_new_claim_has_correct_per_species_citation(self):
        records={x["scientificName"]:x for x in self.corpus["accounts"]}
        refs={x["scientificName"]:{p["url"] for p in x["publications"]}
              for x in self.bibliography["profiles"]}
        for f in self.batch["facts"]:
            self.assertIn(f["scientificName"],records)
            self.assertIn(f["url"],refs[f["scientificName"]])
            self.assertTrue(f["sourceLocation"])
            self.assertTrue(f["studyScope"])
            self.assertTrue(f["sourceType"])
            matches=[x for x in records[f["scientificName"]]["sections"][f["section"]]
                     if x["text"]==f["text"] and x["url"]==f["url"]]
            self.assertEqual(len(matches),1)
        newly_identified={x["scientificName"] for x in self.batch["facts"]
                           if x["section"]=="Identification"}
        self.assertEqual(len(newly_identified),6)
        self.assertIn("Varanus samarensis",newly_identified)
        self.assertIn("Sus oliveri",newly_identified)

    def test_explicit_global_and_national_conservation_scope(self):
        assessment=[f for f in self.batch["facts"] if f["section"]=="Conservation"]
        self.assertEqual(len(assessment),6)
        national=[f for f in assessment if f["scientificName"]=="Crocodylus mindorensis"]
        self.assertEqual(len(national),1)
        self.assertEqual(national[0]["sourceType"],"government_national_conservation_record")
        self.assertIn("2019",national[0]["text"])
        for f in assessment:
            if f["scientificName"]=="Crocodylus mindorensis":
                continue
            self.assertEqual(f["sourceType"],"iucn_dated_assessment")
            self.assertIn("global",f["text"].lower())
            self.assertIsInstance(f["sourceYear"],int)

    def test_survey_lifecycle_and_large_text_regression_contract(self):
        main=(JAVA/"MainActivity.java").read_text()
        close=(JAVA/"SurveyClosePolicy.java").read_text()
        recorder=(ROOT/"app/src/androidTest/java/org/phwildlife/survey/Rc10RecorderInstrumentedTest.java").read_text()
        runner=(ROOT/"scripts/run_emulator_rc7_qa.sh").read_text()
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn('endSurveyButton=UiKit.button(this,"End Survey"',main)
        self.assertIn('Choose Complete, Incomplete, or Not Counted.',main)
        self.assertIn('gpsView.setContentDescription(gpsView.getText()+". Open GPS details")',main)
        self.assertIn('public static final String NOT_COUNTED="not_counted";',close)
        self.assertIn('if(cancelled)return NOT_COUNTED;',close)
        self.assertIn('largeTextKeepsEndSurveyAndGpsAccessible',recorder)
        self.assertIn('font_scale 2.0',recorder)
        self.assertIn('recorder-200percent-font.png',runner)
        self.assertIn('14 tests?',runner)
        self.assertIn("versionCode 80",gradle)
        self.assertIn("0.27.3-rc24c-large-text-header-preview",gradle)

if __name__=="__main__":
    unittest.main()
