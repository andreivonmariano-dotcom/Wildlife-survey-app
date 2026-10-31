"""RC61 PNG structural integrity and source-mapping guarantees."""
from pathlib import Path
import hashlib, struct, zlib, unittest
ROOT=Path(__file__).resolve().parents[1]
ICONS=ROOT/"app/src/main/res/drawable-nodpi"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def verify_png(path):
    data=path.read_bytes()
    assert data[:8]==b"\x89PNG\r\n\x1a\n",str(path)
    offset=8;chunks=[];image_data=b"";size=None
    while offset<len(data):
        count=struct.unpack_from(">I",data,offset)[0];offset+=4
        kind=data[offset:offset+4];offset+=4
        chunk=data[offset:offset+count];offset+=count
        crc=struct.unpack_from(">I",data,offset)[0];offset+=4
        assert zlib.crc32(kind+chunk)&0xffffffff==crc,(str(path),kind)
        chunks.append(kind)
        if kind==b"IHDR":size=struct.unpack_from(">II",chunk,0)
        if kind==b"IDAT":image_data+=chunk
        if kind==b"IEND":break
    assert offset==len(data),(path,"trailing PNG data")
    assert b"PLTE" in chunks and b"tRNS" in chunks,(path,"transparent indexed art required")
    assert b"IEND" in chunks and size==(64,64),(path,size)
    assert len(zlib.decompress(image_data))>1000,path
    return hashlib.sha256(data).hexdigest()
class Rc61IconArt(unittest.TestCase):
    def test_generated_pngs_are_valid_and_transparent(self):
        for name in ("conservation","habitat_links"):
            path=ICONS/f"likas_generated_{name}.png"
            self.assertGreater(path.stat().st_size,1200)
            print(name,verify_png(path))
    def test_approved_18_resources_never_replaced(self):
        existing=list(ICONS.glob("likas_approved_*.png"))
        self.assertEqual(len(existing),18)
        portal=(JAVA/"LikasPortalUi.java").read_text()
        library=(JAVA/"LikasLibraryActivity.java").read_text()
        self.assertIn('case "library-photo":',portal)
        self.assertIn('case "library-sources":',portal)
        self.assertIn("art=image;size=approvedDp",portal)
        self.assertIn('"Photos","Offline images","library-photo"',library)
        self.assertIn('"Sources","Checklists","library-sources"',library)
        self.assertIn('"Conservation","Dated assessments","library-conservation"',library)
        self.assertIn('"Habitat links","Cited associations","library-habitat-links"',library)
        self.assertIn("ImageView.ScaleType.FIT_CENTER:ImageView.ScaleType.CENTER_INSIDE",portal)
        self.assertIn("R.drawable.likas_generated_conservation",portal)
        self.assertIn("R.drawable.likas_generated_habitat_links",portal)
if __name__=="__main__":unittest.main()
