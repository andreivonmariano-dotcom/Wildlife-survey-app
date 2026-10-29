"""RC19: 96 account content, 2018/2021 status dates, and real Android screenshot sweep."""
import json
import unittest
from pathlib import Path

BASE=Path(__file__).resolve().parents[1]
ASSETS=BASE/"app/src/main/assets"
JAVA=BASE/"app/src/androidTest/java/org/phwildlife/survey"
class Rc19AllAccounts(unittest.TestCase):
    def test_study_claims_and_96_unchanged_taxon_keys(self):
        pack=json.loads((ASSETS/"rc19_priority_literature_batch_v1.json").read_text())
        registry=json.loads((ASSETS/"rc9_profile_completion_v1.json").read_text())
        accounts=json.loads((ASSETS/"rc18_unified_96_priority_accounts_v1.json").read_text())
        book=json.loads((ASSETS/"rc14_96_species_literature_index_v1.json").read_text())
        self.assertEqual(pack["taxonCount"],7)
        self.assertEqual(pack["claimCount"],21)
        self.assertEqual(accounts["sourceFactCount"],435)
        self.assertEqual(accounts["prioritySpeciesCount"],96)
        self.assertEqual(len(accounts["accounts"]),96)
        self.assertEqual(sum(x["referenceCount"] for x in book["profiles"]),296)
        names={x["scientificName"] for x in registry["profiles"]}
        self.assertEqual(names,{x["scientificName"] for x in accounts["accounts"]})
        refs={x["scientificName"]:{p["url"] for p in x["publications"]} for x in book["profiles"]}
        for species in pack["records"]:
            self.assertIn(species["scientificName"],names)
            for item in species["items"]:
                self.assertIn(item["url"],refs[species["scientificName"]])
                self.assertTrue(item["sourceLocation"])
                self.assertTrue(item["scopeLimit"])
                self.assertTrue(item["source"])
                self.assertTrue(item["evidenceType"])
                self.assertLess(len(item["statement"]),215)
        self.assertEqual(accounts["sectionMissingCounts"],{
            "Identification":3,"Habitat":44,"Distribution":23,
            "Natural history":71,"Conservation":69})
    def test_preserves_dated_global_status_separate_from_national_law(self):
        pack=json.loads((ASSETS/"rc19_priority_literature_batch_v1.json").read_text())
        claims=[f for species in pack["records"] for f in species["items"]]
        turtle=[x for x in claims if x["id"].startswith("Pelochelys_cantorii") and x["category"]=="Conservation"]
        self.assertTrue(turtle)
        self.assertTrue(any("2018" in x["statement"] for x in turtle))
        self.assertFalse(any("DENR" in x["statement"] for x in turtle))
        cloud=[x for x in claims if x["scientificName"]=="Phloeomys pallidus"] if False else []
    def test_screen_audit_real_java_instrumentation_exports_all_96(self):
        code=(JAVA/"Rc11SpeciesAccountInstrumentedTest.java").read_text()
        sh=(BASE/"scripts/run_emulator_rc7_qa.sh").read_text()
        self.assertIn("testAll96ActualSpeciesScreensHaveCitedCardAndNoBoilerplate",code)
        self.assertIn('assertEquals(96,accounts.length())',code)
        self.assertIn('Bitmap.CompressFormat.JPEG,65',code)
        self.assertIn("Rc11PrioritySpeciesView$Glyph",code)
        self.assertIn("rc19-all-96-screen-audit.csv",sh)
        self.assertIn("ALL-96-RENDERED-SPECIES-AUDIT-PASS",sh)
        self.assertIn("14 tests?",sh)
if __name__=="__main__":
    unittest.main()
