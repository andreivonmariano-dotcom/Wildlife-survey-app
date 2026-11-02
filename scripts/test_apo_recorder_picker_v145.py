"""APO RC85 recorder and species picker: field-first layout and informative records."""
from pathlib import Path
import unittest
R=Path(__file__).resolve().parents[1]
def read(path):return (R/path).read_text(encoding="utf-8")
class RecorderPickerRC85(unittest.TestCase):
    def test_recorder_presents_observations_first_on_regular_text_too(self):
        s=read("app/src/main/java/org/phwildlife/survey/MainActivity.java")
        for token in ('final boolean compactObservationFirst=!hasStationUnits()',
                      '!method.equals("line_transect")&&!method.equals("ves_transect")',
                      'recorderTools.setVisibility(View.GONE)',
                      '"Field tools":"Field tools"',
                      'FieldVisuals.section(this,"Observation",null)',
                      'compactBrowse?"List":"Species list"',
                      'UiKit.button(this,"Save Observation","primary")',
                      'UiKit.button(this,"End Survey","outline-danger")',
                      'FieldQuickActions.preferenceKey(profile,method)',
                      'SurveyClosePolicy.resolve('):
            self.assertIn(token,s)
        self.assertLess(s.index('content.addView(speciesRow)'),s.index('content.addView(countLine)'))
        self.assertLess(s.index('content.addView(countLine)'),s.index('content.addView(speciesActions)'))
        self.assertLess(s.index('content.addView(photoRow)'),s.index('content.addView(birdQuick)'))
        self.assertIn('content.removeView(recorderTools)',s)
        self.assertIn('compactObservationFirst)methodRow.setVisibility(View.GONE)',s)
        self.assertIn('confidenceSpinner.getSelectedItemPosition()==1?',s)
        self.assertIn('"Field-identified (not expert verified)"',s) # canonical saved label
        self.assertNotIn('"Field-identified (not expert verified)","Probable"',s)
    def test_species_cards_use_real_reviewed_attributes_without_lecture(self):
        s=read("app/src/main/java/org/phwildlife/survey/SpeciesPickerActivity.java")
        start=s.index("private void addSpecies(Species item){")
        end=s.index("private void select(String label,String group){",start)
        card=s[start:end]
        for token in ('item.common','item.scientific','item.family','item.origin',
                      'item.iucn','item.ph','item.paicHabitat','item.paicElevation',
                      'item.reviewedHabitats','item.reviewedElevation',
                      'OccurrenceEvidencePolicy.title(item.evidence)',
                      '"Profile","secondary"','"Select","primary"',
                      'new Intent(this,TaxonDetailActivity.class)'):
            self.assertIn(token,card)
        for phrase in ('not proof of site presence','not probabilities',
                       'site compatibility is advisory','proof of absence'):
            self.assertNotIn(phrase,card)
        self.assertIn('if(SpeciesSortPolicy.keep(filter,species.screening.label)&&',s)
        self.assertIn('(!habitatShortlist||species.siteRelevance>0)',s)
        self.assertIn('if(paicCatalogActive&&surveyPreparation!=null&&siteSuggestions)',s)
        self.assertNotIn('if((!siteSuggestions||species.paicRank>0)',s)
        self.assertIn("taxon_synonyms",s)
        self.assertIn("e.review_status='source_listed'",s)
    def test_screens_built_with_original_identity(self):
        wf=read(".github/workflows/apo-recorder-picker-v145-emulator-qa.yml")
        for value in ("versionCode='146'","0.27.3-rc86-apo-interactive-recorder",
                      'RecorderPickerCompactEmulatorTest','RecorderUiEmulatorTest',
                      'font_scale 1.6','font_scale 2.0',
                      'wm size 960x2160','len(files)==6'):
            self.assertIn(value,wf)
        g=read("app/build.gradle")
        self.assertIn("versionCode 150",g)
        self.assertIn("0.27.3-rc90-apo-method-panels",g)
        self.assertIn("org.phwildlife.survey.beta08",g)
        self.assertIn('super(context,NAME,null,29)',
                      read("app/src/main/java/org/phwildlife/survey/FieldDb.java"))
if __name__=="__main__":unittest.main()
