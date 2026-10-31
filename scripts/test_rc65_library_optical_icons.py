"""RC65 optical icons: pixel-faithful generated variants from locked source PNGs."""
from pathlib import Path
from hashlib import sha256
import runpy
import tempfile
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
SOURCE=ROOT/"app/src/main/res/drawable-nodpi"
SCRIPT=ROOT/"scripts/prepare_rc65_library_icons.py"

class Rc65OpticalArt(unittest.TestCase):
    def test_all_source_hashes_locked_and_crops_pixel_faithful(self):
        module=runpy.run_path(str(SCRIPT))
        with tempfile.TemporaryDirectory() as tmp:
            generated=module["generate"](SOURCE,Path(tmp))
            self.assertEqual(4,len(generated))
            for label,(name,crop,hash_lock) in module["SPEC"].items():
                original=(SOURCE/name).read_bytes()
                self.assertEqual(hash_lock,sha256(original).hexdigest())
                w,h,bpp,kind,rows,_=module["read_png"](original)
                path=Path(tmp)/("likas_library_"+label+"_display.png")
                data=path.read_bytes()
                self.assertEqual(data,module["make_display_png"](original,crop))
                dw,dh,bpp2,kind2,outrows,_=module["read_png"](data)
                self.assertEqual((dw,dh,bpp2,kind2),(256,256,bpp,kind))
                span=w-crop*2
                for y in (0,1,32,128,255):
                    for x in (0,8,64,128,255):
                        oy=crop+y*span//256
                        ox=crop+x*span//256
                        self.assertEqual(outrows[y][x*bpp:(x+1)*bpp],
                            rows[oy][ox*bpp:(ox+1)*bpp],label)
        self.assertEqual(18,len(list(SOURCE.glob("likas_approved_*.png"))))
    def test_four_library_routes_use_dedicated_art_only(self):
        portal=(JAVA/"LikasPortalUi.java").read_text()
        library=(JAVA/"LikasLibraryActivity.java").read_text()
        for label in ("conservation","habitat_links","photos","sources"):
            self.assertIn("R.drawable.likas_library_"+label+"_display",portal)
        for old in ("R.drawable.likas_approved_camera","R.drawable.likas_approved_reports",
                    "R.drawable.likas_approved_species_library"):
            self.assertIn(old,portal)
        self.assertIn("setFilterBitmap(false)",portal)
        self.assertIn("ImageView.ScaleType.FIT_CENTER:ImageView.ScaleType.CENTER_INSIDE",portal)
        for route in ("LikasDiscoveryActivity.class","Rc9DifferentialActivity.class",
                      "Rc8PhotoComparisonActivity.class","Rc24ConservationDashboardActivity.class",
                      "CatalogActivity.class","HabitatSpeciesEvidenceActivity.class"):
            self.assertIn(route,library)
    def test_gradle_generates_before_resource_merge_and_keeps_identity(self):
        build=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 125",build)
        self.assertIn("0.27.3-rc65-library-optical-icons-preview",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("prepareRc65LibraryIcons",build)
        self.assertIn("task.name.contains('Resources')",build)
        self.assertIn("task.name.endsWith('SourceSetPaths')",build)
        self.assertIn("super(context,NAME,null,27)",(JAVA/"FieldDb.java").read_text())

if __name__=="__main__":unittest.main()
