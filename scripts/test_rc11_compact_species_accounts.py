"""RC11 compact species accounts preserve 96 source keys and hide editorial scaffolding."""
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSET = ROOT / "app/src/main/assets"
CODE = ROOT / "app/src/main/java/org/phwildlife/survey"

class CompactPrioritySpeciesContracts(unittest.TestCase):
    def test_all_96_priority_species_remain(self):
        p=json.loads((ASSET/"rc9_profile_completion_v1.json").read_text())
        rows=p["profiles"]
        self.assertEqual(96,len(rows))
        self.assertEqual(96,len({r["scientificName"] for r in rows}))
        self.assertEqual(0,p["certifiedComplete"])
        for group in ("Birds","Amphibians","Reptiles","Mammals"):
            self.assertEqual(24,sum(r["group"]==group for r in rows))
        body=(CODE/"TaxonDetailActivity.java").read_text()
        self.assertIn("Rc7FieldAccounts.priority(this,scientific)",body)
        self.assertIn("Rc11PrioritySpeciesView.add(this,content,c,scientific)",body)
        self.assertIn("searchRow.setVisibility(View.GONE)",body)
        self.assertIn("groupFilter.setVisibility(View.GONE)",body)
        self.assertIn("Search another species",body)
        self.assertIn("showSearchControls()",body)
        self.assertIn("if(Rc7FieldAccounts.priority(this,scientific)){",body)
        self.assertLess(body.index("Rc11PrioritySpeciesView.add(this,content,c,scientific)"),
                        body.index("OfflinePhotoEvidence.addForTaxon(this,content,scientific)"))

    def test_scientific_reference_layers_are_not_rewritten(self):
        # Existing evidence is untouched. UI is a filter over cited statements.
        m=json.loads((ASSET/"rc8_evidence_comparisons_v1.json").read_text())
        self.assertEqual(96,len(m["species"]))
        p=[json.loads((ASSET/f"rc10_research_batch{i}_v1.json").read_text()) for i in (1,2,3)]
        self.assertEqual(64,sum(len(r["items"]) for pack in p for r in pack["records"]))
        view=(CODE/"Rc11PrioritySpeciesView.java").read_text()
        for name in ("Identification","Habitat","Range","Natural history","Conservation"):
            self.assertIn('"'+name+'"',view)
        for name in ("search","leaf","pin","tracks","shield"):
            self.assertIn('case "'+name+'":',view)
        self.assertIn('case "leaf":',view)
        self.assertIn('case "pin":',view)
        self.assertIn('sourceDrawer(a,parent',view)
        self.assertIn("setVisibility(View.GONE)",view)
        self.assertNotIn("not yet included in this reference",view)
        self.assertNotIn("research notes are shown below",view)
        self.assertIn('if(area==null',view)
        self.assertIn('!url.startsWith("https://")',view)

if __name__=="__main__":
    unittest.main()
