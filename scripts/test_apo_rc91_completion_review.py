"""RC91 field method completion summaries do not modify protocol outcomes."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"
def src(n):return (CODE/n).read_text()

class MethodCompletionReviewContracts(unittest.TestCase):
    def test_one_inline_action_and_preserved_alternate_shortcuts(self):
        main=src("MainActivity.java")
        ui=src("RecorderMethodUiPolicy.java")
        for text in ('inlineQuickKind(String profile,String method)',
            'case AUDIO:return FieldQuickActions.Kind.CALLS',
            'case FIELD_EFFORT:return FieldQuickActions.Kind.METHOD_EVIDENCE',
            'case DEVICES:return FieldQuickActions.Kind.DEVICES'):
            self.assertIn(text,ui)
        self.assertIn('RecorderMethodUiPolicy.inlineQuickKind(profile,method)',main)
        self.assertIn('chosen[0]==inline?View.GONE:View.VISIBLE',main)
        self.assertIn('inline==FieldQuickActions.Kind.CALLS?View.GONE:View.VISIBLE',main)
        self.assertIn('inline==FieldQuickActions.Kind.DEVICES?View.GONE:View.VISIBLE',main)
        self.assertIn('inline==FieldQuickActions.Kind.METHOD_EVIDENCE?View.GONE:View.VISIBLE',main)
        self.assertIn('changeQuick.setOnClickListener',main)
        self.assertIn('quickPrefs.edit().putString(quickKey,chosen[0].name()).apply()',main)
    def test_method_review_is_read_only_and_source_specific(self):
        p=src("MethodCompletionReview.java")
        for token in ('FieldProtocolPolicy.canComplete(', 'TrapProtocolPolicy.canComplete(',
                      'Uninspected boards:', 'Invalid or incomplete effort entries:',
                      'Without a recorded check:', 'Missing capture details:',
                      'Still deployed:', 'Recorded sightings alone do not'):
            self.assertIn(token,p)
        for forbidden in ('execSQL(', 'UPDATE surveys', 'INSERT INTO',
                          'SQLiteDatabase', 'ContentValues', 'SurveyClosePolicy.resolve('):
            self.assertNotIn(forbidden,p)
        self.assertIn('Kind.NONE,false',p)
    def test_end_review_offers_fix_but_retains_outcomes(self):
        main=src("MainActivity.java")
        for text in ('MethodCompletionReview.Result methodReview=methodRecordReview()',
                     'methodReview.kind!=MethodCompletionReview.Kind.NONE',
                     'actions.addView(reviewTitle,3)', 'actions.addView(reviewDetail,4)',
                     'if(!methodReview.methodRecordsReady)',
                     'methodFix[0].setOnClickListener',
                     'if(requiresGenericUnit()&&samplingUnitId==null)',
                     'pointNextButton.performClick()',
                     'else openFieldProtocol();','openTrapProtocol();',
                     'new AlertDialog.Builder(this)', 'Show method completion checks',
                     'Finish Complete', 'End Incomplete', 'Cancel Survey · Not Counted',
                     'SurveyClosePolicy.resolve(', 'FieldProtocolPolicy.canComplete(',
                     'TrapProtocolPolicy.canComplete(', 'if(hasUnsavedObservation())'):
            self.assertIn(text,main)
        self.assertIn('super(context,NAME,null,29)',src("FieldDb.java"))
        build=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 154",build)
        self.assertIn("versionName '0.27.3-rc94-phlibrary-preview'",build)

if __name__=="__main__":unittest.main()
