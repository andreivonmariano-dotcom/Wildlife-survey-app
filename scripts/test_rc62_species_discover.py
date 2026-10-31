"""RC62 Species Discover presentation contracts, without scientific-content changes."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
class Rc62SpeciesVisual(unittest.TestCase):
    def test_large_font_species_intro_is_compact_and_specific(self):
        s=(JAVA/"LikasDiscoveryActivity.java").read_text()
        self.assertIn('if(!largeText())content.addView(LikasPortalUi.kicker(this,"WILDLIFE"))',s)
        self.assertIn('note("Species",largeText()?23:27,UiKit.INK,true)',s)
        self.assertIn('if(!largeText()){\n                TextView sub=note("Offline species guides"',s)
        self.assertIn("Species group filters. Swipe horizontally for more groups.",s)
        self.assertIn("rail.setFadingEdgeLength(dp(22))",s)
    def test_illustration_not_confused_with_evidence_photo(self):
        s=(JAVA/"LikasDiscoveryActivity.java").read_text()
        self.assertIn('note("ILLUSTRATION",9,UiKit.MUTED,true)',s)
        self.assertIn("OfflinePhotoEvidence.galleryPhotoForTaxon(this,scientific)",s)
        self.assertIn("Status unverified",s)
        self.assertIn('putExtra("taxon_key",entry.optString("sourceTaxonKey"))',s)
        self.assertIn("statusIndex.get",s)
        self.assertIn('"IUCN ":"DENR "',s)
    def test_version_and_original_identity(self):
        b=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 124",b)
        self.assertIn("0.27.3-rc64-library-phone-icons-preview",b)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",b)
        self.assertIn("super(context,NAME,null,27)",(JAVA/"FieldDb.java").read_text())
if __name__=="__main__":unittest.main()
