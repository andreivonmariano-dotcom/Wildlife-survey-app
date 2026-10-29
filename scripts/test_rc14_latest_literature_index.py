"""RC14: all 96 names stay indexed; recent-literature claims never exceed their sources."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"

class LiteratureIndex(unittest.TestCase):
    def test_all_96_accounts_indexed_with_clickable_sources(self):
        base=json.loads((ASSETS/"rc9_profile_completion_v1.json").read_text())
        index=json.loads((ASSETS/"rc14_96_species_literature_index_v1.json").read_text())
        p={r["scientificName"]:r for r in base["profiles"]}
        self.assertEqual(index["profileCount"],96)
        self.assertEqual(len(index["profiles"]),96)
        self.assertEqual(len(p),96)
        self.assertEqual(sum(x["latestConfirmed"] for r in index["profiles"] for x in r["publications"])>0,True)
        self.assertEqual(index["recentVerifiedTaxa"],12)
        for row in index["profiles"]:
            self.assertEqual(row["sourceTaxonKey"],p[row["scientificName"]]["sourceTaxonKey"])
            self.assertEqual(row["group"],p[row["scientificName"]]["group"])
            self.assertEqual(row["referenceCount"],len(row["publications"]))
            self.assertTrue(row["publications"],row["scientificName"])
            uris=set()
            for source in row["publications"]:
                self.assertTrue(source["url"].startswith("https://"))
                self.assertNotIn(source["url"],uris)
                uris.add(source["url"])
                if source["latestConfirmed"]:
                    self.assertGreaterEqual(source["publicationYear"],2025)
                    self.assertLessEqual(source["publicationYear"],2026)

    def test_recent_study_claims_are_narrow_and_cited(self):
        pack=json.loads((ASSETS/"rc14_recent_peer_reviewed_2025_2026_v1.json").read_text())
        names={p["scientificName"] for p in json.loads((ASSETS/"rc9_profile_completion_v1.json").read_text())["profiles"]}
        self.assertEqual(pack["taxonCount"],10)
        self.assertEqual(pack["claimCount"],13)
        self.assertEqual(13,sum(len(x["items"]) for x in pack["records"]))
        for p in pack["records"]:
            self.assertIn(p["scientificName"],names)
            for item in p["items"]:
                self.assertTrue(item["url"].startswith("https://doi.org/"))
                self.assertIn("peer_reviewed",item["evidenceType"])
                self.assertIn(item["publicationYear"],(2025,2026))
                self.assertTrue(item["sourceLocation"])
                self.assertTrue(item["scopeLimit"])
                self.assertLess(len(item["statement"]),210)

    def test_ui_renders_recent_papers_and_one_source_index(self):
        view=(CODE/"Rc11PrioritySpeciesView.java").read_text()
        bibliography=(CODE/"Rc14Bibliography.java").read_text()
        self.assertIn('rc14_recent_peer_reviewed_2025_2026_v1.json',view)
        self.assertIn('Rc14Bibliography.add(a,references,scientific)',view)
        self.assertNotIn('link(a,references,"View publication",fact.url)',view)
        self.assertIn('rc14_96_species_literature_index_v1.json',bibliography)
        self.assertIn('Uri.parse(url)',bibliography)
        self.assertIn('final int remaining=refs.length()-3',bibliography)
        self.assertIn('Show "+remaining+" more sources',bibliography)
        self.assertIn('Open study',bibliography)
        self.assertNotIn('specialist sign-off',bibliography.lower())

if __name__=="__main__":
    unittest.main()
