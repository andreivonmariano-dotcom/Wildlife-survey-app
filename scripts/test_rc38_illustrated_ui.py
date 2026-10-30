"""RC38 illustrated navigation, science-safe collection cards and offline boundaries."""
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

def src(name):
    return (JAVA/name).read_text()

class IllustratedUiContract(unittest.TestCase):
    def test_three_destinations_and_illustrated_portal(self):
        portal=src("LikasPortalUi.java")
        welcome=src("WelcomeActivity.java")
        for piece in ("public static View tile(","public static View mini(",
                      "new GradientDrawable(", "new RippleDrawable(",
                      'case "bird":','case "frog":','case "mammal":'):
            self.assertIn(piece,portal)
        home=welcome.split("private void home(){",1)[1].split("private void fieldworkHub(){",1)[0]
        for title in ('"Fieldwork"','"Library"','"Settings"'):
            self.assertIn(title,home)
        self.assertIn("Resume survey in progress",home)
        field=welcome.split("private void fieldworkHub(){",1)[1].split("/** A brief, read-only",1)[0]
        for label in ('"Birds"','"Herpetofauna"','"Mammals"',
                      '"History"','"Analysis"','HistoryActivity.class',
                      'DashboardActivity.class'):
            self.assertIn(label,field)
        self.assertIn("LikasPortalUi.mini(this,title,detail,art,tap)",welcome)

    def test_library_is_separate_and_visual(self):
        library=src("LikasLibraryActivity.java")
        self.assertIn('discover(body,"Species"',library)
        self.assertIn('discover(body,"Habitats"',library)
        self.assertIn("LikasPortalUi.tile(this,title,subtitle,icon",library)
        self.assertIn("LikasPortalUi.mini(this,first,firstDetail,firstIcon",library)
        self.assertIn("Rc24ConservationDashboardActivity.class",library)
        self.assertNotIn("MainActivity.class",library)
        settings=src("LikasSettingsActivity.java")
        self.assertIn("LikasPortalUi.mini(this,first,firstHint,firstArt",settings)
        self.assertIn("FieldBackupActivity.class",settings)
        self.assertIn("DataManagementActivity.class",settings)
        self.assertIn("ImportActivity.class",settings)
        self.assertIn("AuditActivity.class",settings)
        self.assertIn('getBoolean("reduced_motion",false)',settings)

    def test_cards_avoid_invented_classification(self):
        cards=src("LikasDiscoveryActivity.java")
        for piece in ('if(status==null)return 0;',
                      'return UiKit.BORDER;',
                      'categoryLevel(status.optJSONObject("iucn"))',
                      'categoryLevel(status.optJSONObject("denr"))',
                      '"Status unverified"',
                      'new GradientDrawable(', 'severity>0'):
            self.assertIn(piece,cards)
        self.assertNotIn('"Priority species"',cards)
        for unsafe in ("getWritableDatabase(", "execSQL(", "deleteDatabase("):
            self.assertNotIn(unsafe,cards)

    def test_app_identity_is_stable(self):
        build=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(__import__("re").search(r"versionCode\s+(\d+)",build).group(1)),109)
        self.assertIn("versionName '0.27.3-rc'",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,27)",src("FieldDb.java"))

if __name__=="__main__":
    unittest.main()
