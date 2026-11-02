"""RC87 regression: observed montane context must not rank coastal-only tern as a habitat match."""
import csv
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]

def source(name):
    return (ROOT/"app/src/main/java/org/phwildlife/survey"/name).read_text(encoding="utf-8")

class Rc87HabitatShortlistTest(unittest.TestCase):
    def test_source_aleutian_tern_is_coastal_and_not_montane(self):
        with (ROOT/"app/src/main/assets/paic714_master.csv").open(encoding="utf-8",newline="") as f:
            rows=[x for x in csv.DictReader(f) if x["common_name"]=="Aleutian Tern"]
        self.assertEqual(1,len(rows))
        self.assertIn("coastal and intertidal areas",rows[0]["habitat_text"].lower())
        self.assertIn("marine waters",rows[0]["habitat_text"].lower())
        self.assertNotIn("montane habitats",rows[0]["habitat_text"].lower())

    def test_site_habitat_is_a_real_filter_not_just_a_rank(self):
        picker=source("SpeciesPickerActivity.java")
        policy=source("PaicHabitatPolicy.java")
        self.assertIn("item.siteRelevance=PaicHabitatPolicy.siteHabitatRelevance",picker)
        self.assertIn("(!habitatShortlist||species.siteRelevance>0)",picker)
        self.assertIn("shortlistEnabled=!habitatCode.isEmpty()",picker)
        self.assertIn("Habitat shortlist · Show all species",picker)
        self.assertIn("All species · Show habitat shortlist",picker)
        self.assertIn('case "montane": exact="Montane habitats";broad="Forest"',policy)
        self.assertIn("return 0;",policy)
        self.assertIn("No matching habitat guide labels",picker)

    def test_recent_names_do_not_copy_measurements(self):
        recorder=source("MainActivity.java")
        helper=source("RecentTaxa.java")
        self.assertIn("showRecentSpecies()",recorder)
        self.assertIn("Recently recorded species",recorder)
        self.assertIn("confidenceSpinner.setSelection(0)",recorder)
        self.assertIn("SELECT TRIM(raw_identification) FROM field_records",helper)
        self.assertNotIn("UPDATE field_records",helper)
        self.assertNotIn("INSERT INTO field_records",helper)
        self.assertIn('if(largeText)export.setText("ZIP")',recorder)

if __name__=="__main__":
    unittest.main()
