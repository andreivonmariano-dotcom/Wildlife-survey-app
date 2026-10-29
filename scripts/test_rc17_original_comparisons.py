"""RC17: original diagnostic citations and tappable similar-species comparisons."""
import json
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"
class OriginalComparisonTests(unittest.TestCase):
    def test_all_96_taxa_and_source_links(self):
        base=json.loads((ASSETS/"rc9_profile_completion_v1.json").read_text())
        idx=json.loads((ASSETS/"rc14_96_species_literature_index_v1.json").read_text())
        old={x["scientificName"]:x for x in base["profiles"]}
        self.assertEqual(96,len(old))
        self.assertEqual(96,len(idx["profiles"]))
        self.assertEqual(299,sum(x["referenceCount"] for x in idx["profiles"]))
        self.assertEqual(13,idx["recentVerifiedTaxa"])
        for x in idx["profiles"]:
            self.assertEqual(x["sourceTaxonKey"],old[x["scientificName"]]["sourceTaxonKey"])
            self.assertEqual(x["referenceCount"],len(x["publications"]))
    def test_research_evidence_is_original_and_traceable(self):
        pack=json.loads((ASSETS/"rc17_verified_research_v1.json").read_text())
        self.assertEqual(pack["taxonCount"],5)
        self.assertEqual(pack["claimCount"],16)
        expected={"Platymantis pygmaeus","Platymantis naomii","Varanus bitatawa",
                  "Carlito syrichta","Penelopides panini"}
        self.assertEqual(expected,{x["scientificName"] for x in pack["records"]})
        for species in pack["records"]:
            for claim in species["items"]:
                self.assertTrue(claim["url"].startswith("https://"))
                self.assertTrue(claim["sourceLocation"])
                self.assertTrue(claim["scopeLimit"])
                self.assertIn("peer_reviewed",claim["evidenceType"])
                self.assertLess(len(claim["statement"]),220)
                self.assertNotIn("specialist approval",claim["statement"].lower())
    def test_two_new_original_description_pairs_and_live_button(self):
        pairs=json.loads((ASSETS/"rc9_differential_traits_v1.json").read_text())
        self.assertEqual(4,len(pairs["pairs"]))
        paired={frozenset((p["a"],p["b"])):p for p in pairs["pairs"]}
        for species in [("Platymantis pygmaeus","Platymantis naomii"),
                        ("Varanus bitatawa","Varanus olivaceus")]:
            row=paired[frozenset(species)]
            self.assertGreaterEqual(len(row["evidence"]),3)
            for trait in row["evidence"]:
                self.assertTrue(trait["sourceA"].startswith("https://"))
                self.assertTrue(trait["sourceB"].startswith("https://"))
        compare=(CODE/"Rc9DifferentialActivity.java").read_text()
        view=(CODE/"Rc11PrioritySpeciesView.java").read_text()
        self.assertIn("boolean hasPair(",compare)
        self.assertIn('new Intent(a,Rc9DifferentialActivity.class)',view)
        self.assertIn('"Compare similar species"',view)
        self.assertIn('"Read published source"',compare)
        self.assertNotIn("specialist-approved",compare)
        self.assertIn('rc17_verified_research_v1.json',view)
if __name__=="__main__":
    unittest.main()
