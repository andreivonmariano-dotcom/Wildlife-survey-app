"""RC96 APO PHLibrary edition 1.75: full text, one-paragraph reader and data isolation."""
import json
import re
import unittest
from collections import Counter
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class PhLibraryBook01(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.records=[]
        for part in range(1,9):
            filename=ASSETS/f"phlibrary_book01_v175_{part:02d}.json"
            batch=json.loads(filename.read_text(encoding="utf-8"))
            assert batch["edition"]=="1.75"
            assert batch["rarityModel"]=="0.3.1"
            cls.records.extend(batch["records"])

    def test_exact_714_and_taxonomic_counts(self):
        self.assertEqual(len(self.records),714)
        self.assertEqual(len({x["s"].lower() for x in self.records}),714)
        self.assertEqual(Counter(x["g"] for x in self.records),
            {"Birds":408,"Amphibians":53,"Reptiles":154,"Mammals":99})
        self.assertEqual(Counter(x["t"] for x in self.records),
            {"Common":117,"Uncommon":343,"Rare":214,
             "Exceptionally rare":37,"Legendary":3})
        manifest=json.loads((ASSETS/"phlibrary_book01_manifest.json").read_text())
        self.assertEqual(manifest["speciesCount"],714)
        self.assertEqual(manifest["parts"],8)

    def test_all_accounts_are_single_complete_text_with_source_labels(self):
        for row in self.records:
            with self.subTest(scientific=row["s"]):
                self.assertGreaterEqual(len(row["p"]),100)
                self.assertNotIn("\n",row["p"])
                self.assertTrue(row["o"] and row["f"] and row["c"])
                self.assertIn("✦ "+row["t"],row["b"])
                self.assertTrue(row["r"])
                self.assertTrue(all(ref["short"] and ref["short"] in row["p"]
                                    for ref in row["r"]))
                self.assertTrue(all(not ref["url"] or
                    ref["url"].startswith("https://") for ref in row["r"]))

    def test_confirmed_revision_examples(self):
        index={x["s"]:x for x in self.records}
        self.assertEqual(index["Pithecophaga jefferyi"]["t"],"Legendary")
        self.assertEqual(index["Crocodylus mindorensis"]["t"],"Legendary")
        self.assertEqual(index["Penelopides affinis"]["t"],"Uncommon")
        self.assertEqual(index["Baletemys kampalili"]["t"],"Exceptionally rare")

    def test_reader_routing_and_compact_references(self):
        text=(JAVA/"PhLibraryBook01.java").read_text()
        self.assertIn('label(a,"REFERENCES",12',text)
        self.assertIn('record.optString("p"),16.5f',text)
        self.assertIn("referenceDialog(a,refs)",text)
        self.assertIn("addPhLibraryPlate",text)
        self.assertIn("this::showPhLibraryBook",(JAVA/"TaxonDetailActivity.java").read_text())
        self.assertIn('getStringExtra("phlibrary_scientific_name")',
            (JAVA/"TaxonDetailActivity.java").read_text())
        self.assertIn("PhLibraryDirectoryActivity.class",
            (JAVA/"LikasLibraryActivity.java").read_text())
        self.assertIn(".PhLibraryDirectoryActivity",
            (ROOT/"app/src/main/AndroidManifest.xml").read_text())

    def test_no_database_or_signing_identity_change(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 156",gradle)
        self.assertIn("0.27.3-rc96-phlibrary-reader",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        db=(JAVA/"FieldDb.java").read_text()
        self.assertIn("super(context,NAME,null,29)",db)

if __name__=="__main__":
    unittest.main()
