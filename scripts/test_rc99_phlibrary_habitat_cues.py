"""RC99 PHLibrary field-note coverage and scientific-scope checks."""
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app/src/main/assets"

class Rc99PhLibraryHabitatTest(unittest.TestCase):
    def test_habitat_pack_is_taxon_keyed_and_readable(self):
        data = json.loads((ROOT / "phlibrary_habitat_cues_v177.json").read_text())
        self.assertEqual(data["edition"], "1.77-habitat-cues")
        self.assertEqual(data["speciesCount"], 714)
        self.assertEqual(data["rarityModel"], "0.3.1")
        self.assertGreaterEqual(data["changed"], 100)
        rows = data["records"]
        self.assertEqual(len(rows), data["changed"])
        names = [row["s"] for row in rows]
        self.assertEqual(len(names), len(set(names)))
        self.assertEqual(
            data["counts"],
            {"Birds": 25, "Amphibians": 25, "Reptiles": 25, "Mammals": 25})
        base = {}
        for n in range(1, 9):
            source = json.loads((ROOT / f"phlibrary_book01_v175_{n:02d}.json").read_text())
            for row in source["records"]:
                self.assertNotIn(row["s"], base)
                base[row["s"]] = row
        self.assertEqual(len(base), 714)
        for row in rows:
            self.assertIn(row["s"], base)
            self.assertEqual(row["group"], base[row["s"]]["g"])
            self.assertTrue(45 <= len(row["fieldCue"]) <= 450)
            self.assertNotIn("\\n", row["fieldCue"])
            self.assertNotIn("\\r", row["fieldCue"])
            self.assertIn(row["basis"],
                          {"source_summary",
                           "field_guide_inference_or_prior-account-synthesis"})
            self.assertNotIn("IUCN", row["fieldCue"])
            self.assertNotIn("rarity", row["fieldCue"].lower())

    def test_app_uses_separate_habitat_layer(self):
        code = (ROOT.parent / "java/org/phwildlife/survey/PhLibraryBook01.java").read_text()
        self.assertNotIn("applyEditorial(context,rows,index);", code)
        self.assertNotIn("applyHabitatCues(context,rows,index);", code)
        self.assertIn("phlibrary_book01_v182.json.gz.b64", code)

if __name__ == "__main__":
    unittest.main()
