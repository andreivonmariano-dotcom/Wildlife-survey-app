"""RC23: 96 species, scoped acoustic/taxonomic claims, conservation studies and field UI."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class Rc23SpeciesResearch(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.batch=json.loads((ASSETS/"rc23_original_id_and_threatened_habitat_v1.json").read_text())
        cls.corpus=json.loads((ASSETS/"rc18_unified_96_priority_accounts_v1.json").read_text())
        cls.bib=json.loads((ASSETS/"rc14_96_species_literature_index_v1.json").read_text())

    def test_source_and_gap_ledger(self):
        self.assertEqual(self.batch["recordCount"],9)
        self.assertEqual(len(self.batch["records"]),9)
        self.assertEqual(len(self.corpus["accounts"]),96)
        self.assertEqual(self.corpus["sourceFactCount"],456)
        self.assertEqual(sum(p["referenceCount"] for p in self.bib["profiles"]),306)
        self.assertEqual(self.corpus["sectionMissingCounts"],{
            "Identification":1,"Habitat":38,"Distribution":22,
            "Natural history":64,"Conservation":69})
        for account in self.corpus["accounts"]:
            self.assertEqual(account["coverage"]["citedBiologicalFacts"],
                             sum(len(v) for v in account["sections"].values()))
            self.assertEqual(set(account["coverage"]["sectionsWithoutCitedData"]),
                             {k for k,v in account["sections"].items() if not v})
        missing={x["scientificName"] for x in self.corpus["accounts"]
                 if not x["sections"]["Identification"]}
        self.assertEqual(missing,{"Dicaeum kampalili"})

    def test_all_new_facts_correspond_to_exact_named_species_and_study(self):
        accounts={a["scientificName"]:a for a in self.corpus["accounts"]}
        refs={p["scientificName"]:{r["url"] for r in p["publications"]}
              for p in self.bib["profiles"]}
        for row in self.batch["records"]:
            self.assertIn(row["url"],refs[row["scientificName"]])
            self.assertTrue(row["sourceLocation"])
            self.assertTrue(row["sourceType"])
            self.assertTrue(row["studyScope"])
            self.assertTrue(row["url"].startswith("https://"))
            matches=[v for v in accounts[row["scientificName"]]["sections"][row["section"]]
                     if v["text"]==row["text"] and v["url"]==row["url"]
                     and v["sourceType"]==row["sourceType"]]
            self.assertEqual(len(matches),1)
        identified={f["scientificName"] for f in self.batch["records"]
                    if f["section"]=="Identification"}
        self.assertEqual(identified,{
            "Pelophryne albotaeniata","Limnonectes acanthi",
            "Platymantis negrosensis","Platymantis banahao"})

    def test_historic_camera_records_and_acoustic_cautions(self):
        records=self.batch["records"]
        for r in records:
            if r["section"]=="Identification":
                self.assertTrue("not" in r["studyScope"].lower())
        pangolin=next(r for r in records
                     if r["scientificName"]=="Manis culionensis")
        self.assertIn("three times",pangolin["text"])
        self.assertIn("do not prove",pangolin["studyScope"].lower())
        bleeding=[r for r in records if r["scientificName"]=="Gallicolumba platenae"]
        self.assertEqual({r["section"] for r in bleeding},{"Habitat","Conservation"})
        self.assertTrue(all(r["sourceYear"]==2026 for r in bleeding))

    def test_species_ui_does_not_hide_sources_or_imply_new_compare_index(self):
        bibliography=(JAVA/"Rc14Bibliography.java").read_text()
        account=(JAVA/"Rc11PrioritySpeciesView.java").read_text()
        compare=(JAVA/"Rc8TraitComparisonActivity.java").read_text()
        picker=(JAVA/"SpeciesPickerActivity.java").read_text()
        detail=(JAVA/"TaxonDetailActivity.java").read_text()
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("out.size()+n>300000",bibliography)
        self.assertIn("Evidence type: ",bibliography)
        self.assertIn('Source: "+item.source',account)
        self.assertIn("Compare current cited traits",account)
        self.assertIn("latest cited 96-species accounts",compare)
        self.assertIn("Search offline species by common, scientific or former name",picker)
        self.assertIn("Search species by scientific, common or former name",detail)
        self.assertIn("versionCode 85",gradle)
        self.assertIn("0.27.3-rc27-herpetofauna-field-preview",gradle)

if __name__=="__main__":
    unittest.main()
