"""RC41 offline gallery memory, attribution and navigation regression contracts."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def src(name):return (JAVA/name).read_text(encoding="utf-8")
class GalleryPerf(unittest.TestCase):
    def test_only_nearby_cards_are_materialized(self):
        c=src("LikasDiscoveryActivity.java")
        for needle in (
            'ArrayList<FrameLayout> slots=new ArrayList<>();',
            'for(int i=0;i<data.size();i++){',
            'Runnable hydrate=()->{',
            'if(Math.abs(i-center)<=2)',
            'if(slot.getChildCount()==0)',
            'slot.removeAllViews();',
            'hydrate.run();',
            'scroll.scrollTo(restoreOffset,0);',
            'railOffsets.put(railKey,x);',
            'searchHandler.postDelayed(searchRefresh,180)',
            'if(animate)UiMotion.enter(this,content);'
        ):self.assertIn(needle,c)
        self.assertNotIn("for(JSONObject item:data){",c)
        self.assertIn("OfflinePhotoEvidence.galleryPhotoForTaxon(this,scientific)",c)
        self.assertIn("OfflinePhotoEvidence.galleryPhotoForHabitat(",c)

    def test_approved_photos_decode_in_workers_and_retain_rights(self):
        photos=src("OfflinePhotoEvidence.java")
        for needle in (
            'newFixedThreadPool(2)',
            'galleryImages.execute(()->{',
            'galleryThumbnail(a,result,found);',
            'options.inSampleSize=4',
            'options.inPreferredConfig=Bitmap.Config.RGB_565',
            'if(a.isFinishing()||a.isDestroyed()||target.getParent()==null)return;',
            'target.setVisibility(View.VISIBLE)',
            'galleryPhotoForTaxon(Activity a,String scientific)',
            'galleryPhotoForHabitat(Activity a,String photoId)',
            'getAssets().open(asset)',
            'new android.util.LruCache<String,Bitmap>(4*1024*1024)'
        ):self.assertIn(needle,photos)
        self.assertNotIn("new URL(",photos)
        self.assertIn('photo.optString("license")',photos)
        self.assertIn('photo.optString("author")',photos)

    def test_full_checklist_and_back_navigation(self):
        gallery=src("LikasDiscoveryActivity.java")
        self.assertIn('action("Full Philippine species checklist"',gallery)
        self.assertIn('Rc7SpeciesLibraryActivity.class',gallery)
        catalog=src("Rc7SpeciesLibraryActivity.java")
        self.assertIn('"Philippine species checklist","Library",this::finish',catalog)
        self.assertIn('"Backup & recovery","Settings",this::finish',src("FieldBackupActivity.java"))
        self.assertIn('"Manage saved data","Settings",this::finish',src("DataManagementActivity.java"))
        self.assertIn('"Status unverified"',gallery)
        self.assertNotIn('"Priority species"',gallery)
        self.assertIn('rc34_conservation_card_status_v1.json',gallery)

    def test_upgrade_identity_and_survey_outcomes(self):
        build=(ROOT/"app/build.gradle").read_text(encoding="utf-8")
        self.assertIn("versionCode 104",build)
        self.assertIn("versionName '0.27.3-rc44-ui-harmony-preview'",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn('super(context,NAME,null,27)',src("FieldDb.java"))
        main=src("MainActivity.java")
        self.assertIn('"Cancel Survey · Not Counted"',main)
        self.assertIn('"End Survey"',main)
        self.assertIn('eventView.setAlpha(!reducedMotion',main)
if __name__=="__main__":unittest.main()
