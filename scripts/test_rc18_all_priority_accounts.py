"""Every priority account renders the best source-backed facts, without synthetic filler."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSET=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class UnifiedPriorityCorpus(unittest.TestCase):
    def test_all_96_have_source_backed_sections(self):
        source=json.loads((ASSET/"rc8_evidence_comparisons_v1.json").read_text())
        corpus=json.loads((ASSET/"rc18_unified_96_priority_accounts_v1.json").read_text())
        refs=json.loads((ASSET/"rc14_96_species_literature_index_v1.json").read_text())
        orig={x["scientificName"]:x for x in source["species"]}
        catalog={x["scientificName"]:x for x in refs["profiles"]}
        self.assertEqual(corpus["schemaVersion"],"rc18-unified-96-priority-account-corpus-v1")
        self.assertEqual(corpus["prioritySpeciesCount"],96)
        self.assertEqual(corpus["sourceFactCount"],393)
        self.assertEqual(len(corpus["accounts"]),96)
        self.assertEqual(set(orig),{x["scientificName"] for x in corpus["accounts"]})
        self.assertEqual(set(catalog),set(orig))
        topics={"Identification","Habitat","Distribution","Natural history","Conservation"}
        count=0
        for item in corpus["accounts"]:
            name=item["scientificName"]
            self.assertEqual(item["sourceTaxonKey"],orig[name]["sourceTaxonKey"])
            self.assertEqual(item["group"],orig[name]["group"])
            self.assertEqual(item["family"],orig[name]["family"])
            self.assertEqual(set(item["sections"]),topics)
            self.assertGreater(item["coverage"]["citedBiologicalFacts"],0,name)
            allowed={p["url"] for p in catalog[name]["publications"]}
            used=set()
            for section,items in item["sections"].items():
                for fact in items:
                    count+=1
                    self.assertEqual(set(fact).issuperset({"section","label","text","source","url","sourceType","origin"}),True)
                    self.assertEqual(fact["section"],section)
                    self.assertTrue(fact["label"].strip())
                    self.assertTrue(fact["text"].strip())
                    self.assertTrue(fact["source"].strip())
                    self.assertIn(fact["url"],allowed,(name,fact["url"]))
                    self.assertNotIn((section,fact["text"].lower()),used)
                    used.add((section,fact["text"].lower()))
                    self.assertFalse(fact["text"].startswith(("Check the ","Record the ","Avoid assigning")))
            no_data={section for section,items in item["sections"].items() if not items}
            self.assertEqual(no_data,set(item["coverage"]["sectionsWithoutCitedData"]))
        self.assertEqual(count,393)
        self.assertEqual(refs["recentVerifiedTaxa"],13)
        self.assertEqual(sum(p["referenceCount"] for p in refs["profiles"]),269)

    def test_previous_fact_empty_taxa_have_original_references(self):
        corpus=json.loads((ASSET/"rc18_unified_96_priority_accounts_v1.json").read_text())
        by={x["scientificName"]:x for x in corpus["accounts"]}
        for name in ("Dicaeum anthonyi","Desmalopex microleucoptera"):
            x=by[name]
            self.assertGreater(x["coverage"]["citedBiologicalFacts"],1)
            self.assertTrue(x["sections"]["Distribution"])
        self.assertTrue(by["Desmalopex microleucoptera"]["sections"]["Conservation"])
        self.assertTrue(by["Desmalopex microleucoptera"]["sections"]["Identification"])

    def test_missing_science_remains_unfilled(self):
        c=json.loads((ASSET/"rc18_unified_96_priority_accounts_v1.json").read_text())
        self.assertEqual(c["sectionMissingCounts"],{
            "Identification":18,"Habitat":51,"Distribution":24,
            "Natural history":75,"Conservation":76})
        self.assertFalse(any("approved" in x["coverage"] for x in c["accounts"]))

    def test_actual_android_uses_unified_corpus(self):
        view=(JAVA/"Rc11PrioritySpeciesView.java").read_text()
        self.assertIn('rc18_unified_96_priority_accounts_v1.json',view)
        self.assertIn('unifiedCorpus.optJSONArray("accounts")',view)
        self.assertIn('"Range".equals(key)?"Distribution":key',view)
        self.assertIn('if(!list.isEmpty())return list;',view)
        self.assertIn('Rc13FieldGuideCopy.fieldText(value)',view)

if __name__=="__main__":
    unittest.main()
