"""RC8 source-attributed comparison evidence and habitat expansion contracts."""
import json
from pathlib import Path
import unittest
from collections import Counter
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"
MATRIX=json.loads((ASSETS/"rc8_evidence_comparisons_v1.json").read_text())
HABITAT=json.loads((ASSETS/"offline_habitat_profiles_v1.json").read_text())
PLAN=json.loads((ASSETS/"rc7_priority_batches_v1.json").read_text())

class Rc8ScienceComparisons(unittest.TestCase):
    def test_all_96_source_keyed_species_have_individual_reference(self):
        self.assertEqual(MATRIX["schemaVersion"],"rc8-evidence-comparison-v1")
        self.assertEqual(len(MATRIX["species"]),96)
        self.assertEqual(MATRIX["speciesCount"],96)
        planned={x["sourceTaxonKey"] for b in PLAN["batches"] for x in b["entries"]}
        got={s["sourceTaxonKey"] for s in MATRIX["species"]}
        self.assertEqual(planned,got)
        groups=Counter(p["group"] for p in MATRIX["species"])
        self.assertEqual(groups,{"Birds":24,"Amphibians":24,"Reptiles":24,"Mammals":24})
        self.assertGreaterEqual(sum(len(p["traits"]) for p in MATRIX["species"]),467)

    def test_all_claims_are_attributed_and_no_blank_replaced_by_absence(self):
        approved={"Taxonomy","Identification","Range","Habitat & ecology",
                  "Conservation","Behavior & sound","Evidence limits"}
        for p in MATRIX["species"]:
            self.assertTrue(p["scientificName"])
            self.assertTrue(p["family"])
            self.assertGreaterEqual(len(p["traits"]),2)
            for claim in p["traits"]:
                with self.subTest(species=p["scientificName"],label=claim["label"]):
                    self.assertIn(claim["category"],approved)
                    self.assertTrue(claim["value"].strip())
                    self.assertTrue(claim["source"].strip())
                    self.assertTrue(claim["url"].startswith("https://"))
                    self.assertTrue(claim["review"])
                    self.assertNotIn(claim["value"].strip().lower(),{"no","absent","not present"})
        self.assertIn("not assessed",MATRIX["warning"].lower())

    def test_habitat_15_includes_denr_formations_and_metrics(self):
        self.assertEqual(len(HABITAT["profiles"]),15)
        self.assertEqual(len(MATRIX["habitats"]),15)
        self.assertEqual(MATRIX["habitatCount"],15)
        denr={p["id"] for p in HABITAT["profiles"] if "denr-pbsap-2025" in p["sourceIds"]}
        self.assertEqual(denr,{
            "tropical-subalpine","beach-forest","freshwater-swamp-forest",
            "peat-swamp-forest","semi-evergreen-rainforest","moist-deciduous-forest"})
        cites={s["id"]:s for s in HABITAT["sources"]}
        self.assertIn("denr-pbsap-2025",cites)
        self.assertTrue(cites["denr-pbsap-2025"]["url"].startswith("https://"))
        for h in MATRIX["habitats"]:
            self.assertEqual(len(h["traits"]),5)
            self.assertEqual({c["category"] for c in h["traits"]},
                {"Field indicators","Substrate & water","Vegetation",
                 "Measurements","Scientific cautions"})
            self.assertTrue(all(c["url"].startswith("https://") for c in h["traits"]))
            self.assertTrue(all(c["review"]=="habitat_reference_not_site_classification"
                for c in h["traits"]))

    def test_31_new_literature_accounts_match_source_specific_claims(self):
        p=json.loads((ASSETS/"rc7_literature_batch_b_v1.json").read_text())
        self.assertEqual(len(p["records"]),48)
        self.assertEqual(sum(r["reviewStatus"]=="literature_synthesis_pending_specialist_review"
            for r in p["records"]),48)
        by={r["scientificName"]:r for r in MATRIX["species"]}
        for entry in p["records"]:
            self.assertGreaterEqual(len(entry["facts"]),4)
            actual={t["value"] for t in by[entry["scientificName"]]["traits"]}
            for fact in entry["facts"]:
                self.assertIn(fact["value"],actual)
                self.assertTrue(fact["sourceUrl"].startswith("https://"))

    def test_compare_ui_is_offline_accessible_and_read_only(self):
        ui=(CODE/"Rc8TraitComparisonActivity.java").read_text()
        for token in ("rc8_evidence_comparisons_v1.json","getAssets().open",
                      "Species (96)","Habitats (15)","Not assessed",
                      "Open cited source","Compared with","getContentDescription",
                      "sourceTaxonKey"):
            # sourceTaxonKey is stored in the asset, not necessarily rendered.
            if token=="sourceTaxonKey":
                self.assertIn(token,(ASSETS/"rc8_evidence_comparisons_v1.json").read_text())
            else:self.assertIn(token,ui)
        self.assertNotIn("getWritableDatabase",ui)
        self.assertNotIn("INSERT INTO",ui)
        self.assertIn("Rc8TraitComparisonActivity.class",
                      (CODE/"Rc7SpeciesLibraryActivity.java").read_text())
        self.assertIn("Rc8TraitComparisonActivity.class",
                      (CODE/"HabitatRecordsActivity.java").read_text())
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        self.assertIn('android:name=".Rc8TraitComparisonActivity"',manifest)

if __name__=="__main__":unittest.main()
