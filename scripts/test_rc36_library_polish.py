"""RC36: stable offline photo rails, measured card progress, and data safety."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
ASSETS=ROOT/"app/src/main/assets"

class LibraryPolishContract(unittest.TestCase):
    def test_swipe_rail_controls_and_large_text_layout(self):
        view=(JAVA/"LikasDiscoveryActivity.java").read_text()
        for expression in ('"1 / "+data.size()', "int stride=dp(cardWidth()+11)",
                           'previous.setContentDescription("Previous "+title+" card")',
                           'next.setContentDescription("Next "+title+" card")',
                           "scroll.setOnScrollChangeListener(",
                           'scroll.smoothScrollTo(',
                           "scroll.post(update)",
                           "Math.min(310,screen-56)",
                           'card.setMinimumHeight(dp(largeText()?336:280))',
                           'card.setMinimumHeight(dp(largeText()?355:296))'):
            self.assertIn(expression,view,expression)
        self.assertIn('out.putBoolean("habitats",showingHabitat)',view)
        self.assertIn('saved.getBoolean("habitats"',view)

    def test_species_missing_status_never_implies_low_threat(self):
        view=(JAVA/"LikasDiscoveryActivity.java").read_text()
        self.assertIn("if(status==null)return 0;",view)
        self.assertIn('TextView missing=note("Status unverified"',view)
        self.assertIn("if(assessment==null)continue;",view)
        self.assertIn('return UiKit.BORDER;',view)
        self.assertNotIn('"Priority species"',view)
        evidence=json.loads((ASSETS/"rc34_conservation_card_status_v1.json").read_text())
        core=json.loads((ASSETS/"rc18_unified_96_priority_accounts_v1.json").read_text())
        self.assertEqual(len(core["accounts"]),96)
        self.assertEqual(len(evidence["items"]),6)
        self.assertEqual(len({x["scientificName"] for x in core["accounts"]}-
                             {x["scientificName"] for x in evidence["items"]}),90)

    def test_only_approved_habitat_media_is_used(self):
        photo=(JAVA/"OfflinePhotoEvidence.java").read_text()
        self.assertIn("android.util.LruCache<String,Bitmap>(4*1024*1024)",photo)
        self.assertIn("options.inPreferredConfig=Bitmap.Config.RGB_565;",photo)
        self.assertIn('thumbnailForHabitat(Activity a,String photoId)',photo)
        self.assertIn('!"habitat".equals(item.optString("category"))',photo)
        self.assertIn('OfflinePhotoEvidence.thumbnailForHabitat(', (JAVA/"LikasDiscoveryActivity.java").read_text())
        original=json.loads((ASSETS/"offline_photo_manifest_v1.json").read_text())
        habitat=json.loads((ASSETS/"offline_habitat_profiles_v1.json").read_text())
        ids={i["id"] for i in original["items"] if i.get("category")=="habitat"}
        included={h.get("photoId") for h in habitat["profiles"] if h.get("photoId")}
        self.assertEqual(ids,{"habitat_mangrove","habitat_mossy","habitat_karst"})
        self.assertEqual(included,ids)
        for item in original["items"]:
            if item.get("category")=="habitat":
                self.assertTrue(item["license"].startswith("CC BY"))
                self.assertTrue(item["sourceUrl"].startswith("https://"))
                self.assertTrue(item["author"])

    def test_survey_and_database_safety(self):
        source=(JAVA/"LikasDiscoveryActivity.java").read_text()
        for forbidden in ("getWritableDatabase(", "execSQL(", "deleteDatabase("):
            self.assertNotIn(forbidden,source)
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 96",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("super(context,NAME,null,27)",(JAVA/"FieldDb.java").read_text())

if __name__=="__main__":
    unittest.main()
