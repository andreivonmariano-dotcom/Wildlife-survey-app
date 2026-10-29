"""RC28 minimalist research UI and source-preservation contracts."""
from pathlib import Path
import json
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/org/phwildlife/survey"
ASSETS = ROOT / "app/src/main/assets"

class ResearchUi(unittest.TestCase):
    def test_apa_intext_and_single_reference_ledger(self):
        species = (JAVA / "Rc11PrioritySpeciesView.java").read_text()
        bibliography = (JAVA / "Rc14Bibliography.java").read_text()
        habitat = (JAVA / "OfflineFieldLibrary.java").read_text()
        compare = (JAVA / "Rc8TraitComparisonActivity.java").read_text()
        formatter = (JAVA / "ResearchCitationFormatter.java").read_text()
        self.assertIn("ResearchCitationFormatter.citeClaim(", species)
        self.assertNotIn('"Source: "+item.source',species)
        self.assertIn('new HashSet<String>(unique.keySet())',species)
        self.assertIn("excludedUrls.contains(url)",bibliography)
        self.assertIn("ResearchCitationFormatter.groupReference(",habitat)
        self.assertIn("habitatCitations(",habitat)
        self.assertIn("ResearchCitationFormatter.citeClaim(",compare)
        self.assertIn("sourceRecord(",formatter)
        self.assertIn('"n.d."',formatter)
        self.assertNotIn("Date.now()",formatter)

    def test_quiet_motion_home_workflow_and_full_source_preservation(self):
        palette=(JAVA/"UiKit.java").read_text()
        motion=(JAVA/"UiMotion.java").read_text()
        home=(JAVA/"WelcomeActivity.java").read_text()
        copy=(JAVA/"Rc13FieldGuideCopy.java").read_text()
        self.assertIn("back.setMinHeight(dp(a,48))",palette)
        self.assertIn("ValueAnimator.areAnimatorsEnabled()",motion)
        self.assertIn("fontScale<1.60f",motion)
        self.assertIn('UiKit.button(this,"Resume Survey","primary")',home)
        self.assertIn('UiKit.button(this,"More tools  ▾","quiet")',home)
        self.assertIn('new Intent(this,FieldBackupActivity.class)',home)
        self.assertIn('new Intent(this,DataManagementActivity.class)',home)
        self.assertIn("EDITS.getOrDefault(value.trim(),value.trim())",copy)
        self.assertIn("Bat Conservation International describes distinctive",copy)
        corpus=json.loads((ASSETS/"rc18_unified_96_priority_accounts_v1.json").read_text())
        self.assertEqual(corpus["prioritySpeciesCount"],96)
        self.assertEqual(corpus["sourceFactCount"],456)
        self.assertEqual(
            sum(len(facts) for a in corpus["accounts"] for facts in a["sections"].values()),
            456)

if __name__ == "__main__":
    unittest.main()
