"""RC16 literature sources and compact bibliography filters cover all 96 species."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
SOURCE=ROOT/"app/src/main/java/org/phwildlife/survey"

class Rc16EvidenceAndFilters(unittest.TestCase):
    def test_96_taxon_keys_and_factual_source_categories(self):
        old=json.loads((ASSETS/"rc9_profile_completion_v1.json").read_text())
        new=json.loads((ASSETS/"rc14_96_species_literature_index_v1.json").read_text())
        by_name={x["scientificName"]:x for x in old["profiles"]}
        self.assertEqual(96,len(by_name))
        self.assertEqual(new["profileCount"],96)
        self.assertEqual(len(new["profiles"]),96)
        self.assertEqual(sum(p["referenceCount"] for p in new["profiles"]),290)
        self.assertEqual(new["recentVerifiedTaxa"],13)
        expected={"Identification","Habitat","Distribution","Ecology",
                  "Conservation","Taxonomy","Other"}
        self.assertEqual(set(new["topicValues"]),expected)
        for row in new["profiles"]:
            name=row["scientificName"]
            self.assertEqual(row["sourceTaxonKey"],by_name[name]["sourceTaxonKey"])
            self.assertEqual(row["group"],by_name[name]["group"])
            self.assertEqual(row["referenceCount"],len(row["publications"]))
            seen=set()
            for source in row["publications"]:
                self.assertTrue(source["url"].startswith("https://"))
                self.assertNotIn(source["url"],seen)
                seen.add(source["url"])
                self.assertTrue(set(source["topics"])<=expected)
                self.assertTrue(source["topics"])

    def test_14_new_claims_keep_exact_source_and_scope(self):
        pack=json.loads((ASSETS/"rc16_publication_batch_v1.json").read_text())
        index=json.loads((ASSETS/"rc14_96_species_literature_index_v1.json").read_text())
        pubs={r["scientificName"]:{x["url"] for x in r["publications"]} for r in index["profiles"]}
        self.assertEqual(pack["taxonCount"],9)
        self.assertEqual(pack["claimCount"],14)
        self.assertEqual(sum(len(r["items"]) for r in pack["records"]),14)
        for r in pack["records"]:
            self.assertIn(r["scientificName"],pubs)
            for fact in r["items"]:
                for k in ("label","statement","source","url","authors",
                          "journal","sourceLocation","scopeLimit","evidenceType"):
                    self.assertTrue(fact[k],(r["scientificName"],k))
                self.assertIn(fact["url"],pubs[r["scientificName"]])
                self.assertLess(len(fact["statement"]),190)
                self.assertNotIn("specialist approval",fact["statement"].lower())
                self.assertTrue(fact["evidenceType"].startswith(("peer_reviewed","curated")))
        p=next(r for r in pack["records"] if r["scientificName"]=="Acerodon jubatus")
        self.assertIn("reassessed",p["items"][0]["statement"])
        self.assertIn("Pteropus",p["items"][0]["statement"])
        b=next(r for r in pack["records"] if r["scientificName"]=="Brachymeles hilong")
        self.assertTrue(all("transcription" in x["evidenceType"] for x in b["items"]))

    def test_activated_and_collapsible_bibliography_year_topic_filters(self):
        view=(SOURCE/"Rc11PrioritySpeciesView.java").read_text()
        code=(SOURCE/"Rc14Bibliography.java").read_text()
        self.assertIn("rc16_publication_batch_v1.json",view)
        self.assertIn("Rc14Bibliography.add(a,references,scientific)",view)
        for part in ('"All years"','"2025–2026"','"2020–2024"',
                     '"Before 2020"','"Year not listed"','"Filter references  ▾"',
                     '"Research topic"','"Identification"','"Distribution"',
                     '"Conservation"','"Taxonomy"','"Other"'):
            self.assertIn(part,code)
        self.assertIn("yearMatches(p,year)",code)
        self.assertIn("topicMatches(p,topic)",code)
        self.assertIn("filterPanel.setVisibility(View.GONE)",code)
        self.assertIn("matches.size()-first",code)
        self.assertIn("Uri.parse(url)",code)
        self.assertNotIn("specialist sign-off",code.lower())

if __name__=="__main__":
    unittest.main()
