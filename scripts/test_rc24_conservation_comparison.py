"""RC24: original 1997 source review, typed conservation and live 96-corpus comparisons."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def asset(name):
    return json.loads((ASSETS/name).read_text(encoding="utf-8"))

class Rc24ConservationComparison(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.corpus=asset("rc18_unified_96_priority_accounts_v1.json")
        cls.references=asset("rc14_96_species_literature_index_v1.json")
        cls.batch=asset("rc24_original_hazelae_isarog_v1.json")

    def test_species_identity_and_source_claim_counts(self):
        self.assertEqual(self.corpus["prioritySpeciesCount"],96)
        self.assertEqual(self.corpus["sourceFactCount"],448)
        self.assertEqual(sum(p["referenceCount"] for p in self.references["profiles"]),302)
        self.assertEqual(self.corpus["sectionMissingCounts"],{
            "Identification":1,"Habitat":40,"Distribution":23,
            "Natural history":67,"Conservation":69})
        missing={a["scientificName"] for a in self.corpus["accounts"]
                 if not a["sections"]["Identification"]}
        self.assertEqual(missing,{"Dicaeum kampalili"})

    def test_original_description_and_synonym_traceability(self):
        self.assertEqual(self.batch["recordCount"],6)
        records={a["scientificName"]:a for a in self.corpus["accounts"]}
        bibliography={p["scientificName"]:{r["url"] for r in p["publications"]}
                      for p in self.references["profiles"]}
        for item in self.batch["records"]:
            name=item["scientificName"]
            self.assertIn(name,{"Platymantis hazelae","Platymantis isarog"})
            self.assertIn(item["url"],bibliography[name])
            self.assertEqual(item["sourceYear"],1997)
            self.assertEqual(item["sourceType"],"original_taxonomic_revision")
            self.assertTrue(item["sourceLocation"])
            self.assertTrue(item["studyScope"])
            found=[r for r in records[name]["sections"][item["section"]]
                   if r["text"]==item["text"] and r["url"]==item["url"]]
            self.assertEqual(len(found),1)
        replacement=self.batch["replacementNameReference"]
        self.assertIn(replacement["url"],bibliography["Platymantis isarog"])
        self.assertIn("replacement",replacement["sourceType"])
        self.assertEqual(len(self.batch["records"]),6)
        self.assertIn("no confirmed deposition site",
            next(r["text"] for r in self.batch["records"]
                 if r["scientificName"]=="Platymantis isarog"
                 and r["section"]=="Natural history"))

    def test_no_assessment_inference(self):
        records=[r for a in self.corpus["accounts"]
                 for r in a["sections"]["Conservation"]]
        global_records=[r for r in records if r["sourceType"]=="iucn_dated_assessment"]
        national=[r for r in records if r["sourceType"]=="government_national_conservation_record"]
        self.assertEqual(len(global_records),7)
        self.assertEqual(len(national),1)
        self.assertTrue(all(isinstance(r["sourceYear"],int) for r in global_records))
        self.assertTrue(all(r["url"].startswith("https://") for r in records))

    def test_current_library_compare_and_conservation_access(self):
        comparison=(JAVA/"Rc8TraitComparisonActivity.java").read_text()
        dashboard=(JAVA/"Rc24ConservationDashboardActivity.java").read_text()
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        library=(JAVA/"TaxonDetailActivity.java").read_text()
        instrumentation=(ROOT/"app/src/androidTest/java/org/phwildlife/survey/Rc10RecorderInstrumentedTest.java").read_text()
        runner=(ROOT/"scripts/run_emulator_rc7_qa.sh").read_text()
        build=(ROOT/"app/build.gradle").read_text()
        self.assertIn('rc18_unified_96_priority_accounts_v1.json',comparison)
        self.assertIn("readCurrentSpecies(this)",comparison)
        self.assertIn('trait.put("studyScope"',comparison)
        self.assertIn("Open current species account",comparison)
        self.assertIn("ScrollView page",comparison)
        self.assertIn('government_national_conservation_record',dashboard)
        self.assertIn('iucn_dated_assessment',dashboard)
        self.assertIn('96-available',dashboard)
        self.assertIn("Conservation research · 96 species",library)
        self.assertIn(".Rc24ConservationDashboardActivity",manifest)
        self.assertIn("rc24_compare_200percent.png",instrumentation)
        self.assertIn("rc24_conservation_200percent.png",instrumentation)
        self.assertIn("compare-200percent-font.png",runner)
        self.assertIn("conservation-200percent-font.png",runner)
        self.assertIn("versionCode 81",build)
        self.assertIn("0.27.3-rc25-primary-ecology-accessibility-preview",build)

if __name__=="__main__":
    unittest.main()
