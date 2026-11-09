"""RC105 inherited source regressions for large-font atlas header and reader masthead.
The native instrumentation checks actual Android text bounds and line wrapping.
"""
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/org/phwildlife/survey'
ANDROID=ROOT/'app/src/androidTest/java/org/phwildlife/survey'
class Rc104LargeText(unittest.TestCase):
    def test_bounded_200percent_header_and_heading(self):
        explorer=(JAVA/'PhLibraryDirectoryActivity.java').read_text()
        reader=(JAVA/'PhLibraryBook01.java').read_text()
        tests=(ANDROID/'Rc103PhLibrary2InstrumentedTest.java').read_text()
        self.assertIn('dp(compactFilters?82:71)',explorer)
        self.assertIn('savedButton.setTextSize(12);sortButton.setTextSize(12)',explorer)
        self.assertIn('large?16.5f:30',reader)
        self.assertIn('text must fit vertically at 200% scale',tests)
        self.assertIn('Clear label must fit inside its button',tests)
        self.assertIn('Reader heading breaks in the middle of a word',tests)
    def test_version_and_invariants(self):
        build=(ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 165',build)
        self.assertIn("versionName '0.27.3-rc105-phlibrary-clean-atlas'",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn('super(context,NAME,null,29)',(JAVA/'FieldDb.java').read_text())
        self.assertIn('EXPECTED_COUNT=715',(JAVA/'PhLibraryBook01.java').read_text())
if __name__=='__main__':unittest.main()
