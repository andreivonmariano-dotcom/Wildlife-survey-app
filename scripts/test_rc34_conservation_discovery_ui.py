"""RC34 no invented rarity, distinct sourced IUCN and DENR, and carousel QA."""
import json
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/"app/src/main/assets"
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"

class ConservationCardQA(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.pack=json.loads((DATA/"rc34_conservation_card_status_v1.json").read_text())
        cls.index=json.loads((DATA/"rc18_unified_96_priority_accounts_v1.json").read_text())
        cls.habitat=json.loads((DATA/"offline_habitat_profiles_v1.json").read_text())

    def test_only_individually_reviewed_dated_categories(self):
        rows=self.pack["items"]
        self.assertEqual(len(rows),6)
        self.assertEqual(len({r["scientificName"] for r in rows}),6)
        self.assertEqual(sum("iucn" in r for r in rows),3)
        self.assertEqual(sum("denr" in r for r in rows),3)
        self.assertEqual(len(self.index["accounts"]),96)
        names={r["scientificName"] for r in self.index["accounts"]}
        for row in rows:
            self.assertIn(row["scientificName"],names)
            self.assertNotIn("priority",row)
            self.assertNotIn("rarity",row)
            for source in ("iucn","denr"):
                if source not in row:continue
                item=row[source]
                self.assertIn(item["code"],["CR","EN","VU","NT","LC","DD","EX","EW"])
                self.assertTrue(item["url"].startswith("https://"))
                self.assertTrue(item["year"]>=2019)
                if source=="denr":
                    self.assertEqual(item["edition"],"DAO 2019-09")
                    self.assertEqual(item["year"],2019)
                if source=="iucn":
                    self.assertNotEqual(item["provenance"],"specialist_group_secondary")
        self.assertIn("Missing means not yet independently verified",self.pack["dataPolicy"])

    def test_research_corpus_and_habitats_unchanged(self):
        self.assertEqual(self.index["sourceFactCount"],456)
        self.assertEqual(self.habitat["schemaVersion"],"offline-habitat-profiles-v1")
        self.assertEqual(len(self.habitat["profiles"]),15)

    def test_only_assessment_categories_choose_card_frame(self):
        src=(CODE/"LikasDiscoveryActivity.java").read_text()
        self.assertIn('categoryLevel(JSONObject data)',src)
        self.assertIn('return Math.max(categoryLevel(status.optJSONObject("iucn"))',src)
        self.assertIn('statusIndex.containsKey(s.optString("scientificName"))',src)
        self.assertIn('new HorizontalScrollView(this)',src)
        self.assertIn('setSmoothScrollingEnabled(true)',src)
        self.assertIn('OfflinePhotoEvidence.thumbnailForTaxon(this,scientific)',src)
        self.assertIn('UiMotion.enter(this,content)',src)
        self.assertIn('putExtra("taxon_key",entry.optString("sourceTaxonKey"))',src)
        self.assertIn("Rarity unassessed",src)
        self.assertNotIn('Priority species',src)
        self.assertNotIn('rare"',''.join(src.splitlines()[:80]))

    def test_source_first_habitats_and_split_portal(self):
        habitat=(CODE/"LikasHabitatProfileActivity.java").read_text()
        self.assertIn("OfflinePhotoEvidence.add",habitat)
        self.assertIn('"Field signs"',habitat)
        self.assertIn('"Limits"',habitat)
        self.assertIn('"Sources"',habitat)
        library=(CODE/"LikasLibraryActivity.java").read_text()
        self.assertIn('"species","species"',library)
        self.assertIn('"habitat","habitats"',library)
        catalog=(CODE/"Rc7SpeciesLibraryActivity.java").read_text()
        self.assertNotIn('"Priority species (96)"',catalog)
        self.assertNotIn('"Priority: diagnosis unassessed"',catalog)
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        self.assertIn(".LikasDiscoveryActivity",manifest)
        self.assertIn(".LikasHabitatProfileActivity",manifest)
        self.assertIn("versionCode 94",(ROOT/"app/build.gradle").read_text())

if __name__=="__main__":
    unittest.main()
