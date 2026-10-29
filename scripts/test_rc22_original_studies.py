"""RC22: five evidenced ID additions, primary Philippine habitat studies, and 96 stable keys."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSET=ROOT/"app/src/main/assets"
class Rc22OriginalStudies(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.batch=json.loads((ASSET/"rc22_original_diagnoses_habitat_field_studies_v1.json").read_text())
        cls.corpus=json.loads((ASSET/"rc18_unified_96_priority_accounts_v1.json").read_text())
        cls.library=json.loads((ASSET/"rc14_96_species_literature_index_v1.json").read_text())

    def test_totals_and_missing_diagnostic_sections_are_honest(self):
        self.assertEqual(self.batch["recordCount"],13)
        self.assertEqual(len(self.batch["records"]),13)
        self.assertEqual(self.corpus["prioritySpeciesCount"],96)
        self.assertEqual(self.corpus["sourceFactCount"],456)
        self.assertEqual(sum(p["referenceCount"] for p in self.library["profiles"]),306)
        self.assertEqual(self.corpus["sectionMissingCounts"],{
            "Identification":1,"Habitat":38,"Distribution":22,
            "Natural history":64,"Conservation":69})
        for account in self.corpus["accounts"]:
            self.assertEqual(account["coverage"]["citedBiologicalFacts"],
                             sum(len(x) for x in account["sections"].values()))
            self.assertEqual(set(account["coverage"]["sectionsWithoutCitedData"]),
                             {topic for topic,rows in account["sections"].items() if not rows})

    def test_five_id_gaps_get_targeted_characters_not_generic_filler(self):
        identified={r["scientificName"] for r in self.batch["records"]
                    if r["section"]=="Identification"}
        self.assertEqual(identified,{
            "Fejervarya vittigera","Trimeresurus schultzei",
            "Gekko gigante","Sanguirana everetti","Platymantis spelaeus"})
        for name in identified:
            account=next(a for a in self.corpus["accounts"] if a["scientificName"]==name)
            self.assertTrue(account["sections"]["Identification"])
        still_missing={a["scientificName"] for a in self.corpus["accounts"]
                       if not a["sections"]["Identification"]}
        self.assertEqual(len(still_missing),1)
        self.assertNotIn("Platymantis hazelae",still_missing)
        self.assertIn("Dicaeum kampalili",still_missing)

    def test_source_provenance_matches_exact_taxon_and_study_scope(self):
        accounts={a["scientificName"]:a for a in self.corpus["accounts"]}
        refs={p["scientificName"]:{x["url"] for x in p["publications"]}
              for p in self.library["profiles"]}
        self.assertEqual(len(accounts),96)
        for record in self.batch["records"]:
            name=record["scientificName"]
            self.assertIn(record["url"],refs[name])
            self.assertTrue(record["sourceLocation"])
            self.assertTrue(record["studyScope"])
            self.assertTrue(record["sourceType"])
            self.assertIn(record["sourceYear"],range(2000,2027))
            matching=[r for r in accounts[name]["sections"][record["section"]]
                      if r["text"]==record["text"] and r["url"]==record["url"]
                      and r["origin"]=="rc22_original_diagnoses_habitat_field_studies_v1.json"]
            self.assertEqual(len(matching),1,(name,record["label"]))
        self.assertTrue(all(r["section"]!="Conservation" for r in self.batch["records"]))

    def test_2018_panay_field_results_not_synthetic_national_occurrence(self):
        panay=[r for r in self.batch["records"] if r["scientificName"]=="Gallicolumba keayi"]
        self.assertEqual(len(panay),2)
        self.assertTrue(all("2018" in r["text"] for r in panay))
        self.assertTrue(all("Northwest Panay" in r["text"] for r in panay))
        self.assertTrue(all("not" in r["studyScope"] for r in panay))
        croc=[r for r in self.batch["records"] if r["scientificName"]=="Crocodylus mindorensis"]
        self.assertEqual({x["section"] for x in croc},{"Habitat","Distribution"})
        self.assertTrue(all(x["sourceYear"]==2013 for x in croc))

    def test_same_app_id_sqlite_and_new_preview_code(self):
        build=(ROOT/"app/build.gradle").read_text()
        db=(ROOT/"app/src/main/java/org/phwildlife/survey/FieldDb.java").read_text()
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("versionCode 96",build)
        self.assertIn("0.27.3-rc36-library-polish-preview",build)
        self.assertIn("super(context,NAME,null,27)",db)

if __name__=="__main__":
    unittest.main()
