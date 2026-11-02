"""RC86 field-recorder and species catalog regression contracts (no synthetic taxa)."""
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parents[1]
def src(name):
    return (ROOT/"app/src/main/java/org/phwildlife/survey"/name).read_text(encoding="utf-8")

class Rc86InteractiveTest(unittest.TestCase):
    def test_additive_database_identity_and_observation_export(self):
        db=src("FieldDb.java")
        self.assertIn("super(context,NAME,null,29)",db)
        self.assertIn("if(oldVersion<29)upgradeTwentyNine(d)",db)
        self.assertIn("upgradeTwentyNine(d);",db)
        self.assertIn("ALTER TABLE field_records ADD COLUMN taxon_key TEXT",db)
        self.assertIn("ALTER TABLE field_records ADD COLUMN taxon_source_version TEXT",db)
        recorder=src("MainActivity.java")
        match=re.search(r'INSERT INTO field_records\(([^"]+)\) VALUES\(([^"]+)\)"',recorder)
        self.assertIsNotNone(match)
        cols=match.group(1).split(",")
        self.assertEqual(len(cols),match.group(2).count("?"))
        self.assertIn("taxon_key",cols)
        self.assertIn("taxon_source_version",cols)
        self.assertIn('r.taxon_key,r.taxon_source_version FROM field_records',recorder)
        self.assertIn("identification_review_note,taxon_key,taxon_source_version",recorder)
    def test_selected_species_identity_survives_picker_and_drafts(self):
        picker=src("SpeciesPickerActivity.java")
        recorder=src("MainActivity.java")
        for field in ('"taxon_key"','"taxon_source_version"'):
            self.assertIn("result.putExtra("+field,picker)
            self.assertIn('data.getStringExtra('+field+')',recorder)
            self.assertIn('d.put('+field,recorder)
        self.assertIn("resolveExactCatalogTaxon(raw,group)",recorder)
        self.assertIn("cursor.moveToNext()?null:match",recorder)
        self.assertIn("selectedTaxonKey=\"\";selectedTaxonVersion=\"\"",recorder)
    def test_single_record_capture_and_neutral_default(self):
        recorder=src("MainActivity.java")
        self.assertIn('new String[]{"Not assessed","Identified","Probable","Uncertain","Unidentified"}',recorder)
        self.assertIn('d.put("confidence_v2",true)',recorder)
        self.assertIn('d.optBoolean("confidence_v2",false)',recorder)
        self.assertIn("content.addView(confidenceBlock)",recorder)
        self.assertIn('UiKit.button(this,"Attach photo","secondary")',recorder)
        self.assertIn("detectRow.removeView(markTime)",recorder)
        self.assertIn("utilityRow.removeView(pauseButton)",recorder)
        self.assertIn('UiKit.button(this,"All data","secondary")',recorder)
        for part in ("filled(microhabitatEntry)","filled(herpHeightEntry)",
                     "filled(perchEntry)","filled(deviceEntry)",
                     "callScoreSpinner.setSelection(0)"):
            self.assertIn(part,recorder)
        self.assertIn("Photo ready to attach",recorder)
        quick=src("FieldQuickActions.java")
        self.assertNotIn("return Kind.PHOTO;",quick)
        self.assertNotIn("result.add(Kind.PHOTO);",quick)
    def test_catalog_scope_search_and_honest_cards(self):
        picker=src("SpeciesPickerActivity.java")
        suggestions=src("SpeciesReferenceSuggestionAdapter.java")
        self.assertIn("Mindanao PAIC (714)",picker)
        self.assertIn("Philippine catalog",picker)
        self.assertIn("likas_prior_taxon_catalog",picker)
        self.assertIn("likas_prior_taxon_occurrence_evidence",picker)
        self.assertIn("nameSuggestions.setArchivedCatalog(pos==1)",picker)
        self.assertIn("setArchivedCatalog(boolean archived)",suggestions)
        self.assertIn("FROM \"+catalog+\" c WHERE",suggestions)
        self.assertIn('conservation.append("DENR: ")',picker)
        self.assertIn('!value.trim().equals("—")',picker)
        self.assertIn('Habitat / elevation overlap',picker)
        self.assertIn("Site context",picker)
        self.assertNotIn('TextView match=text("Site match"',picker)
    def test_archived_profiles_and_source_attribution(self):
        profile=src("TaxonDetailActivity.java")
        recorder=src("MainActivity.java")
        for token in ("likas_prior_taxon_catalog",
                      "likas_prior_taxon_synonyms",
                      "likas_prior_taxon_habitat_profiles",
                      "likas_prior_taxon_occurrence_evidence",
                      "detailSuggestions.setArchivedCatalog(prior)"):
            self.assertIn(token,profile)
        self.assertIn("catalogSourceForKey(recordTaxonKey",recorder)
        self.assertIn('"SELECT source,source_version FROM "+catalog+',recorder)
    def test_candidate_presence_needs_assessed_identification(self):
        self.assertIn('!confidence.equals("Not assessed")',src("MainActivity.java"))
        self.assertIn("'Not assessed'",src("ResearchExports.java"))
    def test_preview_metadata(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 148",gradle)
        self.assertIn("0.27.3-rc88-apo-detection-first",gradle)
        wf=(ROOT/".github/workflows/apo-recorder-picker-v146-emulator-qa.yml").read_text()
        self.assertIn("versionCode='146'",wf)
        self.assertIn("RecorderPickerCompactEmulatorTest",wf)

if __name__=="__main__":
    unittest.main()
