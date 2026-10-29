"""RC37: cited species-section links and restored library swipe positions."""
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class LibraryNavigationContract(unittest.TestCase):
    def test_research_chapters_only_appear_with_source_facts(self):
        source=(JAVA/"Rc11PrioritySpeciesView.java").read_text()
        for token in ("LinkedHashMap<String,View> sections",
                      "if(card!=null)sections.put(AREAS[i],card)",
                      "if(sections.size()>1)", '"Jump to section"',
                      "target.getTop()-dp(a,8)",
                      '"Jump to "+area+" research section"'):
            self.assertIn(token,source)
        self.assertIn("if(available.isEmpty())return null;",source)
        for unsafe in ("execSQL(", "getWritableDatabase(", "deleteDatabase("):
            self.assertNotIn(unsafe,source)

    def test_library_cards_remember_positions_and_respect_motion(self):
        source=(JAVA/"LikasDiscoveryActivity.java").read_text()
        for token in ("railOffsets=new HashMap<>()",
                      'String railKey=(isHabitat?"habitats:":"species:")+title;',
                      "int restoreOffset=railOffsets.getOrDefault(railKey,0);",
                      "railOffsets.put(railKey,x)",
                      "scroll.scrollTo(restoreOffset,0)",
                      'saved.getBundle("railOffsets")',
                      'out.putBundle("railOffsets",positions)',
                      'getBoolean("reduced_motion",false)',
                      "ValueAnimator.areAnimatorsEnabled()"):
            self.assertIn(token,source)
        self.assertIn("if(status==null)return 0;",source)
        for unsafe in ("execSQL(", "getWritableDatabase(", "deleteDatabase("):
            self.assertNotIn(unsafe,source)

    def test_version_identity_schema_continuity(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 100",gradle)
        self.assertIn("0.27.3-rc",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("super(context,NAME,null,27)",
                      (JAVA/"FieldDb.java").read_text())

if __name__=="__main__":
    unittest.main()
