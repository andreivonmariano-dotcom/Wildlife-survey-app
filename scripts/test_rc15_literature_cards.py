"""RC15 publication-backed diagnosis, habitat and compact source-display contracts."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSET=ROOT/"app/src/main/assets"
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"

class CurrentPublicationBatch(unittest.TestCase):
    def test_unchanged_species_identity_and_new_claims(self):
        registry=json.loads((ASSET/"rc9_profile_completion_v1.json").read_text())
        pack=json.loads((ASSET/"rc15_publication_batch_v1.json").read_text())
        index=json.loads((ASSET/"rc14_96_species_literature_index_v1.json").read_text())
        original={x["scientificName"]:x for x in registry["profiles"]}
        indexed={x["scientificName"]:x for x in index["profiles"]}
        self.assertEqual(len(original),96)
        self.assertEqual(set(original),set(indexed))
        self.assertEqual(len(indexed),96)
        self.assertEqual(pack["taxonCount"],6)
        self.assertEqual(pack["claimCount"],13)
        self.assertEqual(index["recentVerifiedTaxa"],13)
        self.assertEqual(sum(x["referenceCount"] for x in indexed.values()),303)
        self.assertEqual(set(x["scientificName"] for x in pack["records"]),{
            "Platymantis pygmaeus","Hydrosaurus pustulatus",
            "Harpyionycteris whiteheadi","Pithecophaga jefferyi",
            "Penelopides panini","Rhabdotorrhinus waldeni"})
        for entry in pack["records"]:
            name=entry["scientificName"]
            self.assertEqual(indexed[name]["sourceTaxonKey"],original[name]["sourceTaxonKey"])
            self.assertEqual(indexed[name]["group"],original[name]["group"])
            for claim in entry["items"]:
                self.assertEqual(claim["publicationYear"],int(claim["publicationYear"]))
                self.assertTrue(2022<=claim["publicationYear"]<=2026)
                self.assertTrue(claim["url"].startswith("https://doi.org/"))
                self.assertTrue(claim["doi"])
                self.assertTrue(claim["journal"])
                self.assertTrue(claim["authors"])
                self.assertTrue(claim["sourceLocation"])
                self.assertTrue(claim["scopeLimit"])
                self.assertIn("peer_reviewed",claim["evidenceType"])
                self.assertLess(len(claim["statement"]),215)
                self.assertFalse(any(x in claim["statement"].lower() for x in
                    ("specialist approval","not proof of","this is not","unverified field observation")))
                self.assertIn(claim["url"],[r["url"] for r in indexed[name]["publications"]])
        self.assertEqual(13,sum(len(x["items"]) for x in pack["records"]))

    def test_source_display_has_metadata_with_clickable_doi(self):
        ui=(CODE/"Rc11PrioritySpeciesView.java").read_text()
        source=(CODE/"Rc14Bibliography.java").read_text()
        self.assertIn("rc15_publication_batch_v1.json",ui)
        self.assertIn("Rc14Bibliography.add(a,references,scientific)",ui)
        for field in ('paper.optString("authors")','paper.optString("journal")',
                      'paper.isNull("doi")?"":paper.optString("doi")','paper.optInt("publicationYear")',
                      'imprint+=" · DOI: "+doi','"Open study ↗"'):
            self.assertIn(field,source)
        self.assertIn("final int remaining=matches.size()-first",source)
        self.assertIn("Uri.parse(url)",source)

if __name__=="__main__":
    unittest.main()
