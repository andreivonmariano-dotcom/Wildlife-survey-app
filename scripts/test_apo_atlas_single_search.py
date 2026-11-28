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
        self.assertEqual(ui.count('tile(body,"Species Atlas"'),0)
        self.assertIn('new Intent(this,PhLibraryDirectoryActivity.class)',ui)
        self.assertNotIn('PhLibrary2Ui.panel(this,"Explore connections"',ui)
        self.assertIn('tile(body,"Habitat Atlas"',ui)
        self.assertIn('tile(body,"Research Library"',ui)
        self.assertIn('tile(body,"Saved species"',ui)
        self.assertIn('tile(body,"Offline Map"',ui)
        self.assertEqual(ui.count('tile(body,"'),4)

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

    def test_contextual_back_and_full_directory_access(self):
        explorer=source("LivingAtlasExploreActivity.java")
        self.assertIn('openedFromAccount=incoming!=null&&!incoming.trim().isEmpty()',explorer)
        self.assertIn('if(openedFromAccount || selectedScientific.isEmpty())finish()',explorer)
        self.assertIn('new Intent(this,PhLibraryDirectoryActivity.class)',explorer)
        self.assertIn('directory.putExtra("phlibrary_query",lastQuery)',explorer)
        self.assertIn('directory.putExtra("phlibrary_group",filterGroup)',explorer)
        self.assertIn('atlas.search(q,atlas.taxa().size())',explorer)
        self.assertNotIn('atlas.search(q,150)',explorer)
        self.assertIn('chip.setMinHeight(dp(LikasDesignSystem.MIN_TOUCH_DP))',explorer)
        self.assertIn('chip.setSelected(active)',explorer)

    def test_canonical_directory_preserves_all_accounts_and_recorder_dock(self):
        directory=source("PhLibraryDirectoryActivity.java")
        self.assertIn('for(JSONObject r:PhLibraryBook01.entries(this))',directory)
        self.assertIn('matches.add(r)',directory)
        self.assertIn('list=new ListView(this)',directory)
        self.assertIn('list.setAdapter(adapter)',directory)
        recorder=source("MainActivity.java")
        self.assertNotIn('private Button button(String txt)',recorder)
        self.assertIn('UiKit.button(this,"End Survey","outline-danger")',recorder)
        self.assertIn('UiKit.button(this,"Save Observation","primary")',recorder)
        self.assertIn('MIN_TOUCH_DP=48',source("LikasDesignSystem.java"))

if __name__=="__main__":
    unittest.main()
