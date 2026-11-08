"""RC82 compact survey setup after selecting a wildlife group."""
from pathlib import Path
import unittest
R=Path(__file__).resolve().parents[1]
def read(path):return (R/path).read_text(encoding="utf-8")
class CompactSetupRC82(unittest.TestCase):
    def test_method_picker_is_primary_and_comparison_is_optional(self):
        s=read("app/src/main/java/org/phwildlife/survey/WelcomeActivity.java")
        setup=s.split("private void setup(String selected){",1)[1].split("private void refreshStudyTitle()",1)[0]
        for token in ('methodPicker=new Spinner(this)',
                      'SurveyMethods.displayNames(profile)',
                      'setupSpinner(methodPicker,"Choose survey method")',
                      'methodChoices.setVisibility(View.GONE)',
                      'compareMethods.setOnClickListener',
                      '"Show all survey methods"',
                      'methodChoices.check(1000+position)',
                      'methodPicker.setSelection(choice)'):
            self.assertIn(token,setup)
        self.assertLess(setup.index("methodsCard.addView(methodPicker"),setup.index("methodsCard.addView(methodChoices)"))
        self.assertIn('methodChoices.getCheckedRadioButtonId()-1000',s)
        self.assertIn("methodChoices.check(1000+choice)",s)
    def test_optional_setup_without_losing_science_context(self):
        s=read("app/src/main/java/org/phwildlife/survey/WelcomeActivity.java")
        for token in ('regionAssistance.setVisibility(View.GONE)',
                      'regionToggle.setOnClickListener',
                      '"Show GPS checklist area assistance"',
                      'routeOptions.setVisibility(View.GONE)',
                      'routeOptionsToggle.setOnClickListener',
                      '"Show repeat route options"',
                      'saveTemplateChoice.setChecked(false)',
                      'new Intent(this,SurveyGpsActivity.class)',
                      'SurveyMethods.timedInterval(choices[choice].code)',
                      'boundaryMessage.setText('):
            self.assertIn(token,s)
        self.assertIn('if(points&&routeOptionsToggle!=null',s)
        self.assertIn('routeOptionsToggle.setVisibility(points?View.VISIBLE:View.GONE)',s)
        self.assertIn('if(saveNewTemplate)',s)
        self.assertIn('saveNewTemplate=m.code.equals("point_count")',s)
    def test_core_setup_and_identity_remain(self):
        s=read("app/src/main/java/org/phwildlife/survey/WelcomeActivity.java")
        for token in ('step("01","Survey method")',
                      'step("02","Project and location")',
                      'step("03","Sampling effort")',
                      'step("04","Begin survey")',
                      'setupStartButton.setOnClickListener(v->startSelected())',
                      'setupFooter.setVisibility(View.VISIBLE)',
                      'persistSetup();',
                      'prepareFieldChecklist.isChecked()',
                      'studyTemplateIds.get('):
            self.assertIn(token,s)
        g=read("app/build.gradle")
        self.assertIn("versionCode 158",g)
        self.assertIn("0.27.3-rc98-phlibrary-editorial",g)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",g)
        self.assertIn('super(context,NAME,null,29)',read("app/src/main/java/org/phwildlife/survey/FieldDb.java"))
if __name__=="__main__":unittest.main()
