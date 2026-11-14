"""One canonical Atlas directory; detailed connections require a selected taxon."""
from pathlib import Path
import unittest

BASE=Path(__file__).resolve().parents[1]
SRC=BASE/"app/src/main/java/org/phwildlife/survey"
def source(name):return (SRC/name).read_text(encoding="utf-8")

class AtlasSingleEntryTest(unittest.TestCase):
    def test_landing_exposes_one_species_search_and_useful_scoped_routes(self):
        ui=source("LikasLibraryActivity.java")
        self.assertEqual(ui.count('search.setHint("Search species")'),1)
        self.assertEqual(ui.count('tile(body,"Species Atlas"'),1)
        self.assertIn('new Intent(this,PhLibraryDirectoryActivity.class)',ui)
        self.assertNotIn('PhLibrary2Ui.panel(this,"Explore connections"',ui)
        self.assertIn('tile(body,"Habitat Atlas"',ui)
        self.assertIn('tile(body,"References"',ui)
        self.assertIn('tile(body,"Saved species"',ui)
        self.assertIn('tile(body,"Offline Map"',ui)
        self.assertEqual(ui.count('tile(body,"'),5)

    def test_taxonomic_connections_open_from_a_real_book_account(self):
        detail=source("TaxonDetailActivity.java")
        self.assertIn('PhLibraryBook01.render(this,content,scientific',detail)
        self.assertIn('PhLibrary2Ui.panel(this,"Taxonomic connections"',detail)
        self.assertIn('LivingAtlasExploreActivity.EXTRA_SCIENTIFIC_NAME,scientific',detail)
        self.assertIn('new Intent(this,LivingAtlasExploreActivity.class)',detail)
        explorer=source("LivingAtlasExploreActivity.java")
        self.assertIn('selectedScientific=getIntent().getStringExtra(EXTRA_SCIENTIFIC_NAME)',explorer)
        self.assertIn('showSpecies(selectedScientific)',explorer)
        self.assertIn('atlas.approvedHabitatEdges(selected.scientificName)',explorer)
        self.assertIn('section("Taxonomic connections"',explorer)

    def test_scientific_sources_and_field_records_are_unchanged(self):
        data=source("LivingAtlasDataLayer.java")
        self.assertIn('approvedHabitatLink(',data)
        self.assertIn('return !siteOccurrence',data)
        self.assertIn('BOOK_EDITION = "1.82"',data)
        self.assertIn('EXPECTED_COUNT=715',source("PhLibraryBook01.java"))
        self.assertIn('super(context,NAME,null,29)',source("FieldDb.java"))

if __name__=="__main__":
    unittest.main()
