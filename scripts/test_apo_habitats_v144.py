"""RC84 habitat and species setup: fewer words, unchanged evidence distinction."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
def read(p):return (ROOT/p).read_text(encoding="utf-8")
class Rc84HabitatChecklist(unittest.TestCase):
    def test_essential_controls_visible_and_options_disclosed(self):
        s=read("app/src/main/java/org/phwildlife/survey/HabitatsSpeciesActivity.java")
        for token in ('"0 habitats confirmed"',
                      '"Choose habitats (16)"', '"Species checklist"',
                      '"Checklist: Suggested"', 'checklistMode.setVisibility(View.GONE)',
                      'mapOptions.setVisibility(View.GONE)',
                      'siteDetails.setVisibility(View.GONE)',
                      '"Source & license"', '"Map source & files"',
                      '"Begin survey","primary"',
                      'selectedSummary.setText(', 'updateSelectedSummary()'):
            self.assertIn(token,s)
        self.assertIn('body.addView(siteSummary)',s)
        self.assertIn('body.addView(checks)',s)
        self.assertLess(s.index('body.addView(speciesSummary)'),s.index('body.addView(mapOptions)'))
    def test_rank_mode_habitat_confirmation_and_scientific_provenance(self):
        s=read("app/src/main/java/org/phwildlife/survey/HabitatsSpeciesActivity.java")
        for token in ('confirmed.add(name)', 'confirmed.remove(name)',
                      'PaicHabitatPolicy.isKnown(item)',
                      'checklistMode.getCheckedRadioButtonId()==3001?"suggested":"all"',
                      'PaicHabitatPolicy.encode(confirmed)',
                      'PaicHabitatPolicy.encode(new HashSet<>(suggested))',
                      'PaicHabitatPolicy.ranking(',
                      'GpsElevation.read(TrackService.currentFix())',
                      'GpsFixPolicy.acceptableElapsed(',
                      'launch.removeExtra("start_gps_latitude")',
                      'launch.putExtra("start_without_gps",true)',
                      'Matching does not confirm species presence',
                      'historical satellite land cover',
                      'DOI: 10.5281/zenodo.5571936',
                      'License: CC BY 4.0'):
            self.assertIn(token,s)
        self.assertIn('state.putStringArray("chosen"',s)
        self.assertIn('state.putBoolean("ranked"',s)
        self.assertNotIn('matching is a presence prediction',s.lower())
    def test_signed_preview_and_system_identity(self):
        wf=read(".github/workflows/apo-habitats-v144-emulator-qa.yml")
        for t in ("versionCode='144'","0.27.3-rc84-apo-habitats-checklist",
                  "HabitatChecklistCompactEmulatorTest","font_scale 1.6",
                  "font_scale 2.0","wm size 960x2160","len(files)==4",
                  "PaicHabitatPolicy"):
            self.assertIn(t,wf)
        g=read("app/build.gradle")
        self.assertIn("versionCode 154",g)
        self.assertIn("0.27.3-rc94-phlibrary-preview",g)
        self.assertIn("org.phwildlife.survey.beta08",g)
        self.assertIn("super(context,NAME,null,29)",
                      read("app/src/main/java/org/phwildlife/survey/FieldDb.java"))
if __name__=="__main__":unittest.main()
