"""RC29 source-integrity contracts for research-centric illustrated species pages."""
from pathlib import Path
import json
import re
import unittest

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"app/src/main/java/org/phwildlife/survey"
DATA=ROOT/"app/src/main/assets"

class Rc29ResearchGuide(unittest.TestCase):
    def test_all_96_original_accounts_remain_unchanged_and_cited(self):
        data=json.loads((DATA/"rc18_unified_96_priority_accounts_v1.json").read_text())
        self.assertEqual(data["prioritySpeciesCount"],96)
        self.assertEqual(data["sourceFactCount"],456)
        claims=[row for taxon in data["accounts"]
                for group in taxon["sections"].values() for row in group]
        self.assertEqual(len(claims),456)
        self.assertTrue(all(x["url"].startswith("https://") for x in claims))
        self.assertEqual(len({a["scientificName"] for a in data["accounts"]}),96)

    def test_verified_doi_uses_exact_urls_and_no_network_requests(self):
        apa=(SRC/"VerifiedApaCatalog.java").read_text()
        cited=(SRC/"ResearchCitationFormatter.java").read_text()
        self.assertEqual(14, len(re.findall(r'\bm.put\("https://doi.org/', apa)))
        self.assertIn("Reintar, A. R. T.",apa)
        self.assertIn('Sutton, L. J.',apa)
        self.assertIn('Meneses, C. G.',apa)
        self.assertIn("VerifiedApaCatalog.reference(url)", cited)
        self.assertIn("VerifiedApaCatalog.inText(url)", cited)
        self.assertNotIn('new URL(',apa)

    def test_photo_credits_and_non_recorder_visual_access(self):
        photo=(SRC/"OfflinePhotoEvidence.java").read_text()
        view=(SRC/"Rc11PrioritySpeciesView.java").read_text()
        compare=(SRC/"Rc8TraitComparisonActivity.java").read_text()
        conservation=(SRC/"Rc24ConservationDashboardActivity.java").read_text()
        self.assertIn('plate.setScaleType(ImageView.ScaleType.FIT_CENTER)',photo)
        self.assertIn('photo.optString("license")',photo)
        self.assertIn('Photo credits & original',photo)
        self.assertIn('ResearchCitationFormatter.citeClaim(',view)
        self.assertIn('ResearchCitationFormatter.citeClaim(',compare)
        self.assertIn('ResearchCitationFormatter.citeClaim(',conservation)
        self.assertNotIn('Source ("+year+',conservation)
        self.assertIn('new HashSet<String>(unique.keySet())',view)

if __name__=="__main__":
    unittest.main()
