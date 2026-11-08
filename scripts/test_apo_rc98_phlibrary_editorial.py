"""RC98 PHLibrary habitat prose, editorial provenance and destination uniqueness."""
import json
import re
import unittest
from collections import Counter
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
ASSETS=ROOT/"app/src/main/assets"

class Rc98BookEditorial(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.original=[]
        for i in range(1,9):
            cls.original.extend(json.loads((ASSETS/f"phlibrary_book01_v175_{i:02d}.json").read_text(encoding="utf-8"))["records"])
        cls.overlay=json.loads((ASSETS/"phlibrary_editorial_v176.json").read_text(encoding="utf-8"))
        cls.edited={r["s"]:r for r in cls.overlay["records"]}
        cls.before={r["s"]:r for r in cls.original}

    def test_exact_714_unmodified_source_and_structured_overlay(self):
        self.assertEqual(len(self.original),714)
        self.assertEqual(len(set(self.before)),714)
        self.assertEqual(self.overlay["baseEdition"],"1.75")
        self.assertEqual(self.overlay["edition"],"1.76-editorial")
        self.assertEqual(self.overlay["speciesCount"],714)
        self.assertEqual(self.overlay["rarityModel"],"0.3.1")
        self.assertEqual(len(self.edited),self.overlay["changed"])
        self.assertEqual(len(self.overlay["records"]),len(self.edited))
        self.assertGreaterEqual(len(self.edited),400)
        self.assertEqual(Counter(r["t"] for r in self.original),
            {"Common":117,"Uncommon":343,"Rare":214,"Exceptionally rare":37,"Legendary":3})
        self.assertEqual(Counter(r["g"] for r in self.original),
            {"Birds":408,"Amphibians":53,"Reptiles":154,"Mammals":99})

    def test_valid_editorial_citations_and_only_article_overrides(self):
        for sci,fix in self.edited.items():
            with self.subTest(scientific=sci):
                self.assertIn(sci,self.before)
                self.assertGreaterEqual(len(fix["p"]),100)
                self.assertNotIn("\n",fix["p"])
                self.assertNotEqual(fix["p"],self.before[sci]["p"])
                self.assertGreaterEqual(len(fix["r"]),1)
                self.assertTrue(fix["note"])
                self.assertIn(fix["kind"],("source-enriched","editorial-refinement"))
                for cite in fix["r"]:
                    self.assertTrue(cite["short"])
                    self.assertIn(cite["short"],fix["p"])
                    self.assertTrue(not cite["url"] or cite["url"].startswith("https://"))

    def test_habitat_edits_are_not_confused_with_new_field_evidence(self):
        self.assertEqual(self.overlay["enriched"],7)
        self.assertEqual(sum(f["kind"]=="source-enriched"
                             for f in self.edited.values()),7)
        self.assertEqual(self.overlay["summary"]["habitatPhraseSpeciesBefore"],427)
        self.assertLess(self.overlay["summary"]["habitatPhraseSpeciesAfter"],30)
        self.assertGreater(self.overlay["summary"]["contrastSpeciesAfter"],30)
        self.assertIn("source-enriched",self.edited["Draco mindanensis"]["kind"])
        self.assertIn("secondary",self.edited["Draco mindanensis"]["p"])
        self.assertIn("mossy",self.edited["Baletemys kampalili"]["p"])
        self.assertIn("nest",self.edited["Pithecophaga jefferyi"]["p"])

    def test_reader_applies_overlay_without_touching_field_database(self):
        java=(JAVA/"PhLibraryBook01.java").read_text(encoding="utf-8")
        self.assertIn('phlibrary_editorial_v176.json',java)
        self.assertIn("applyEditorial(context,rows,index)",java)
        self.assertIn('target.put("p",paragraph)',java)
        self.assertIn('target.put("r",sources)',java)
        self.assertIn('getString("short")',java)
        self.assertNotIn("getWritableDatabase",java)
        self.assertNotIn("execSQL(",java)
        self.assertIn("Text v1.76",java)
        self.assertIn("super(context,NAME,null,29)",
                      (JAVA/"FieldDb.java").read_text(encoding="utf-8"))

    def test_home_has_only_one_book_and_three_distinct_actions(self):
        hub=(JAVA/"LikasLibraryActivity.java").read_text(encoding="utf-8")
        directory=(JAVA/"PhLibraryDirectoryActivity.java").read_text(encoding="utf-8")
        self.assertEqual(hub.count("PhLibraryStyle.bookCover(this"),1)
        self.assertEqual(hub.count("add(body,"),3)
        for title in ('"Browse wildlife groups"','"Discovery Rarity"',
                      '"Habitat associations"'):
            self.assertIn(title,hub)
        for old in ('"Conservation evidence"','"Similar species"',
                    '"Source collections"','"Photo comparisons"'):
            self.assertNotIn(old,hub)
        self.assertIn('getStringExtra("phlibrary_group")',directory)
        self.assertIn('getStringExtra("phlibrary_rarity")',directory)
        self.assertNotIn("ImageView",hub)
        self.assertNotIn("ImageView",directory)
        gradle=(ROOT/"app/build.gradle").read_text(encoding="utf-8")
        self.assertIn("versionCode 158",gradle)
        self.assertIn("0.27.3-rc98-phlibrary-editorial",gradle)

if __name__=="__main__":
    unittest.main()
