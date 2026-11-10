"""RC97 PHLibrary editorial UI contract. Source/art, research and field records unchanged."""
import unittest
import json
from collections import Counter
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
ASSETS=ROOT/"app/src/main/assets"

class Rc97PHLibraryVisual(unittest.TestCase):
    def test_hub_has_one_book_and_no_photo_or_icon_navigation(self):
        hub=(JAVA/"LikasLibraryActivity.java").read_text()
        self.assertIn("PhLibraryPhotoHero.create",hub)
        self.assertEqual(hub.count("PhLibraryDirectoryActivity.class"),1)
        for unwanted in ('"Photo comparisons"','"Wildlife field guides"',
                         '"Habitat field guides"','LikasPortalUi.action',
                         'addApprovedIcon','OfflinePhotoEvidence.'):
            self.assertNotIn(unwanted,hub)
        self.assertIn("tile(body,",hub)
        self.assertIn("new ScrollView(this)",hub)
    def test_directory_is_searchable_and_text_only(self):
        directory=(JAVA/"PhLibraryDirectoryActivity.java").read_text()
        self.assertIn("PhLibraryBook01.entries(this)",directory)
        self.assertIn("PHLibrary",directory)
        self.assertIn("UiKit.fieldInput(this,search)",directory)
        self.assertIn("Rarity: ",directory)
        self.assertIn("new ListView(this)",directory)
        self.assertIn("phlibrary_scientific_name",directory)
        for unwanted in ("ImageView","AsyncImage","OfflinePhotoEvidence",
                         "LikasPortalUi.Glyph"):
            self.assertNotIn(unwanted,directory)
    def test_single_paragraph_unmodified_and_no_badge_symbols_or_photo(self):
        book=(JAVA/"PhLibraryBook01.java").read_text()
        self.assertIn('record.optString("p"),17.5f',book)
        self.assertIn("referencesDialog(a,refs)",book)
        self.assertIn('Citations (',book)
        self.assertIn('replace("✦","")',book)
        self.assertNotIn("OfflinePhotoEvidence.addPhLibraryPlate",book)
        self.assertNotIn("ImageView",book)
        self.assertIn("Next species",book)
    def test_same_714_source_records_and_tiers(self):
        rows=[]
        for i in range(1,9):
            data=json.loads((ASSETS/f"phlibrary_book01_v175_{i:02d}.json").read_text())
            self.assertEqual(data["edition"],"1.75")
            self.assertEqual(data["rarityModel"],"0.3.1")
            rows+=data["records"]
        self.assertEqual(len(rows),714)
        self.assertEqual(len({x["s"].lower() for x in rows}),714)
        self.assertEqual(Counter(x["g"] for x in rows),
                         {"Birds":408,"Amphibians":53,"Reptiles":154,"Mammals":99})
        self.assertEqual(Counter(x["t"] for x in rows),
                         {"Common":117,"Uncommon":343,"Rare":214,
                          "Exceptionally rare":37,"Legendary":3})
    def test_existing_apk_identity_and_science_schema_preserved(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 170",gradle)
        self.assertIn("0.27.3-rc110-highres-photo-galleries",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("super(context,NAME,null,29)",
                      (JAVA/"FieldDb.java").read_text())
        self.assertIn("SurveyClosePolicy.resolve(",
                      (JAVA/"MainActivity.java").read_text())

if __name__=="__main__": unittest.main()
