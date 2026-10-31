"""RC56 live recorder UI-only contract; do not alter survey lifecycle or data."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
J=ROOT/"app/src/main/java/org/phwildlife/survey"
def src(n):return (J/n).read_text(encoding="utf8")
class Rc56RecorderLayout(unittest.TestCase):
    def test_compact_header_and_scalable_field_actions(self):
        a=src("MainActivity.java")
        block=a.split("private void draw(){",1)[1].split("private void showGroups(){",1)[0]
        self.assertIn('UiKit.button(this,"‹","quiet")',block)
        self.assertIn('home.setContentDescription("Back to Home")',block)
        self.assertIn('export.setContentDescription("Export research ZIP")',block)
        self.assertIn('boolean stackSessionControls=FieldLayoutPolicy.stackPortalPair(',block)
        self.assertIn('utilityRow.setOrientation(LinearLayout.VERTICAL)',block)
        self.assertIn('if(stackCaptureActions)row.setOrientation(LinearLayout.VERTICAL)',block)
        self.assertIn('detectionSpinner.setContentDescription("Choose observation detection type")',block)
        self.assertIn('detectionSpinner.setMinimumHeight(dp(56))',block)
        self.assertIn('distanceEntry.setContentDescription(',block)
        self.assertIn('TextView detectionLabel=label("Detection type"',block)
        self.assertIn('lineRow=vertical()',block)
        self.assertIn('Transect magnetic bearing (0–359°)',block)
        self.assertIn('"Herp survey":profile+" Survey"',block)
        self.assertIn('title.setContentDescription(profile+" Survey")',block)

    def test_count_and_selected_group_accessibility(self):
        a=src("MainActivity.java")
        for term in (
            'minus.setContentDescription("Decrease individuals or evidence count")',
            'plus.setContentDescription("Increase individuals or evidence count")',
            'countView.setContentDescription(count+" individuals or evidence items")',
            'b.setContentDescription(name+" observation group"+(selected?", selected":""))',
            'b.setSelected(selected)',
            'persistDraft();group=name;showGroups();refreshAutocomplete();queueDraft();',
            'sessionToggle.setContentDescription("Show optional haptic interval alert")',
            'evidenceToggle.setContentDescription("Show other evidence capture tools")'
        ):self.assertIn(term,a)

    def test_survey_lifecycle_and_scientific_provenance_unchanged(self):
        a=src("MainActivity.java")
        for term in (
            'endSurveyButton=UiKit.button(this,"End Survey","outline-danger")',
            'endSurveyButton.setOnClickListener(v->finishPrompt())',
            'footer.addView(footerOptions,optionsParams)',
            'root.addView(footer)',
            'saveButton.setOnClickListener(v->save())',
            'reviewButton.setOnClickListener(v->{',
            'new Intent(this,HistoryActivity.class).putExtra("event_id",eventId)',
            'finish(boolean complete,Double length,String disposition,String reason)',
            '"not_counted"',
            'SurveyStartGpsPolicy.classify(withoutGps,',
            'TrackService.currentFix()',
            'markedObserverFix',
            'FieldQuickActions.preferenceKey(profile,method)',
            'ResearchExports.',
        ):self.assertIn(term,a)
        self.assertIn('gpsView.setText(permissionMissing?',a)
        self.assertIn('"Precise GPS permission needed · details":"Device GPS off · details"',a)
        self.assertIn('countView.setContentDescription(count+" individuals or evidence items")',a)
        self.assertIn("versionCode 124",(ROOT/"app/build.gradle").read_text())
        self.assertIn("0.27.3-rc64-library-phone-icons-preview",(ROOT/"app/build.gradle").read_text())
        self.assertIn("super(context,NAME,null,27)",src("FieldDb.java"))
if __name__=="__main__":unittest.main()
