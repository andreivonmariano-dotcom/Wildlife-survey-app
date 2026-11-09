"""RC107 habitat field-guide and Saved Species bug regression.

The habitat article is a readable editorial guide; species-account source data
and previously reviewed habitat evidence stay byte-identical.
"""
import base64
import gzip
import hashlib
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

def java(name):
    return (JAVA/name).read_text(encoding="utf-8")

class Rc107HabitatSaved(unittest.TestCase):
    def test_fifteen_single_paragraph_accounts_use_original_habitat_ids(self):
        original=(ASSETS/"offline_habitat_profiles_v1.json").read_bytes()
        reader=json.loads((ASSETS/"phlibrary_habitat_reader_v1.json").read_text())
        profiles=json.loads(original)["profiles"]
        self.assertEqual(len(profiles),15)
        self.assertEqual(reader["sourceSha256"],hashlib.sha256(original).hexdigest())
        self.assertEqual(set(reader["profiles"]),{row["id"] for row in profiles})
        for label,paragraph in reader["profiles"].items():
            self.assertGreater(len(paragraph.split()),75,label)
            self.assertNotIn("\n",paragraph,label)
            self.assertEqual(paragraph.count(". "),paragraph.count(". "),label)

    def test_saved_collection_is_stable_and_clear_deletes_bookmarks(self):
        directory=java("PhLibraryDirectoryActivity.java")
        bookmarks=java("PhLibraryBookmarks.java")
        self.assertIn('savedCollectionMode=saved.getBoolean("collectionMode",false)',directory)
        self.assertIn('savedOnly=savedCollectionMode;',directory)
        self.assertIn('confirmClearSaved()',directory)
        self.assertIn('PhLibraryBookmarks.clear(this)',directory)
        self.assertIn('out.putBoolean("collectionMode",savedCollectionMode)',directory)
        self.assertIn('savedCollectionMode?"Saved":"Saved species"',directory)
        self.assertIn('PhLibraryBookmarks.all(this).isEmpty()',directory)
        self.assertIn('store(c).edit().remove(KEY).commit()',bookmarks)
        self.assertNotIn("FieldDb",bookmarks)
        self.assertNotIn("execSQL(",directory)

    def test_educational_habitat_filter_has_all_atlas_categories(self):
        directory=java("PhLibraryDirectoryActivity.java")
        habitat=java("PhLibraryHabitatFilter.java")
        atlas=java("HabitatSpeciesEvidenceActivity.java")
        self.assertIn('"Habitat: "',directory)
        self.assertIn('"Rarity: "',directory)
        self.assertIn('choose("Habitat",PhLibraryHabitatFilter.NAMES',directory)
        self.assertIn('PhLibraryHabitatFilter.mentions(r,selectedHabitat)',directory)
        self.assertIn('getIntent().getStringExtra("phlibrary_habitat_id")',directory)
        self.assertIn('{"riparian","streamside"',habitat)
        self.assertIn('"lowland-forest"',habitat)
        self.assertIn('"moist-deciduous-forest"',habitat)
        self.assertIn('articles.optString(id),17.5f',atlas)
        self.assertIn('article.setTextIsSelectable(true)',atlas)
        self.assertIn('"Browse species"',atlas)
        self.assertNotIn("Literature-linked species",atlas)
        self.assertNotIn('"About Habitat"',directory)
        self.assertNotIn('"Discovery Rarity:',directory)

    def test_source_account_search_expands_previous_riparian_matches(self):
        raw=(ASSETS/"phlibrary_book01_v182.json.gz.b64").read_text(encoding="ascii")
        book=json.loads(gzip.decompress(base64.b64decode(raw)))
        self.assertEqual(book["speciesCount"],715)
        previous=[row for row in book["records"] if "riparian" in row["p"].lower()]
        expanded=[row for row in book["records"] if any(
            phrase in row["p"].lower() for phrase in ("riparian","streamside","riverine forest"))]
        self.assertGreater(len(expanded),len(previous))
        self.assertEqual(len({row["s"] for row in book["records"]}),715)

    def test_preserved_surveys_and_version(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 167",gradle)
        self.assertIn("0.27.3-rc107-habitat-reader-saved",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("super(context,NAME,null,29)",java("FieldDb.java"))
        self.assertIn('EXPECTED_COUNT=715',java("PhLibraryBook01.java"))
        self.assertEqual(len(json.loads(
            (ASSETS/"rc8_species_habitat_links_v1.json").read_text())["edges"]),21)

if __name__=="__main__": unittest.main()
