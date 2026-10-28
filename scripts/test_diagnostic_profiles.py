"""Source-backed diagnostic profile schema and reference UI contracts."""
from pathlib import Path
import json
import unittest
from urllib.parse import urlparse

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
ASSET=ROOT/"app/src/main/assets/diagnostic_profiles_v1.json"
pack=json.loads(ASSET.read_text(encoding="utf-8"))

class DiagnosticProfileReferences(unittest.TestCase):
    def test_profiles_are_offline_and_source_checked_not_editor_approved(self):
        self.assertEqual(1,pack["schemaVersion"])
        self.assertIn("pending",pack["reviewLevel"].lower())
        self.assertEqual(8,len(pack["profiles"]))
        self.assertTrue({"Pithecophaga jefferyi","Spilornis holospilus",
                         "Polypedates leucomystax","Kaloula pulchra",
                         "Hydrosaurus pustulatus","Varanus cumingi",
                         "Carlito syrichta","Cynocephalus volans"}.issubset(
                             {p["scientificName"] for p in pack["profiles"]}))
    def test_every_morphology_trait_has_resolvable_primary_reference(self):
        references={s["id"]:s for s in pack["sourceList"]}
        self.assertGreaterEqual(len(references),3)
        for source in references.values():
            u=urlparse(source["url"])
            self.assertEqual("https",u.scheme)
            self.assertTrue(u.netloc)
            self.assertEqual("2026-10-28",source["accessed"])
            self.assertTrue(source["publisher"])
        for profile in pack["profiles"]:
            self.assertIn(profile["group"],("Birds","Amphibians","Reptiles","Mammals"))
            self.assertGreaterEqual(len(profile["traits"]),5)
            for trait in profile["traits"]:
                self.assertTrue(trait["field"] and trait["value"])
                self.assertTrue(trait["sourceIds"])
                for ref in trait["sourceIds"]:
                    self.assertIn(ref,references)
    def test_no_unsourced_automated_identification_or_occurrence_claim(self):
        text=ASSET.read_text(encoding="utf-8")
        self.assertIn("not a verified key",text.lower())
        self.assertIn("Philippine taxonomic reviewer approval pending",text)
    def test_comparison_is_read_only_and_navigable(self):
        s=(JAVA/"DiagnosticGuideActivity.java").read_text(encoding="utf-8")
        assert "showComparison()" in s
        assert "evidenceLine(card,first" in s
        assert "evidenceLine(card,second" in s
        self.assertIn("Open publisher source",s)
        self.assertNotIn("getWritableDatabase()",s)
        self.assertNotIn("INSERT INTO",s)
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
        self.assertIn('android:name=".DiagnosticGuideActivity"',manifest)
        self.assertIn('DiagnosticGuideActivity.hasProfile(this,scientific)',
                      (JAVA/"TaxonDetailActivity.java").read_text(encoding="utf-8"))

if __name__=="__main__":
    unittest.main()
