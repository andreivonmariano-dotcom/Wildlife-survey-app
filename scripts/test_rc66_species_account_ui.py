"""RC66 individual species-account reading layout checks, not scientific data tests."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/org/phwildlife/survey"

def source(name):
    return (JAVA / name).read_text(encoding="utf-8")

class Rc66SpeciesAccountUi(unittest.TestCase):
    def test_large_text_search_reuses_original_controls(self):
        page = source("TaxonDetailActivity.java")
        self.assertIn("SOFT_INPUT_ADJUST_RESIZE", page)
        self.assertIn('search.setHint("Search species")', page)
        self.assertIn("stackedSearch=widthDp<350f||", page)
        self.assertIn("getResources().getConfiguration().fontScale>=1.60f", page)
        self.assertIn("searchRow.removeAllViews()", page)
        self.assertIn("searchRow.addView(search,new LinearLayout.LayoutParams(-1,dp(64)))", page)
        self.assertIn("searchActions.addView(dropdown", page)
        self.assertIn("searchActions.addView(find", page)
        self.assertIn("search.setAdapter(autocomplete)", page)

    def test_detail_only_shows_reading_controls(self):
        page = source("TaxonDetailActivity.java")
        detail = page.split("private void loadKey(String key){", 1)[1]
        self.assertIn('conservation.setVisibility(View.GONE)', detail)
        self.assertIn('groupFilter.setVisibility(View.GONE)', detail)
        self.assertIn('searchRow.setVisibility(View.GONE)', detail)
        self.assertIn("new LinearLayout.LayoutParams(-1,-2)", detail)
        restore = page.split("private void showSearchControls(){", 1)[1].split(
            "private void showMessage(", 1)[0]
        self.assertIn("conservation.setVisibility(View.VISIBLE)", restore)
        self.assertIn("searchRow.setVisibility(View.VISIBLE)", restore)

    def test_research_cards_fit_and_keep_citations(self):
        guide = source("Rc11PrioritySpeciesView.java")
        self.assertIn("new LinearLayout.LayoutParams(0,-2,1f)", guide)
        self.assertIn("setHorizontalFadingEdgeEnabled(true)", guide)
        self.assertIn('Swipe horizontally to jump between documented research sections', guide)
        self.assertIn("ResearchCitationFormatter.citeClaim(", guide)
        self.assertIn("sourceDrawer(a,parent,scientific", guide)
        self.assertNotIn("card.addView(toggle,new LinearLayout.LayoutParams(-1,dp(a,45)))", guide)

    def test_scientific_identity_not_changed(self):
        page = source("TaxonDetailActivity.java")
        self.assertIn("SELECT * FROM taxon_catalog WHERE taxon_key=? LIMIT 1", page)
        self.assertIn("Rc11PrioritySpeciesView.add(this,content,c,scientific)", page)
        self.assertIn('paragraph("Regional occurrence evidence"', page)
        self.assertIn("OfflinePhotoEvidence.addForTaxon", page)

if __name__ == "__main__":
    unittest.main()
