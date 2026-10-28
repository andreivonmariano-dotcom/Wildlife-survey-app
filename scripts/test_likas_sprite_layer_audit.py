"""Synthetic scene contracts for isolated LIKAS component inspections.

Fixtures are deliberately NOT the user's reviewed Prototype D artwork.
They exercise validation logic without asserting real sprite-art quality.
"""
import io
import json
from pathlib import Path
import tempfile
import unittest
from zipfile import ZipFile

from inspect_likas_sprite_layers import audit_archive, FRAME_COUNTS

KINDS = {"cloud":3, "river":1, "waterfall":4, "wildlife":1, "canopy":3, "fern":2}

def make_fixture(dest, missing=None, duplicate_frames=False):
    try:
        from PIL import Image
    except ImportError:
        Image = None
    components = {}
    with ZipFile(dest, "w") as z:
        for mode in ("day", "night"):
            for kind in ("fixed", "rocks"):
                z.writestr(f"scene/{kind}_{mode}.webp", b"placeholder")
            index = 0
            for kind, n in KINDS.items():
                for j in range(n):
                    key = f"{mode}_{kind}_{j}"
                    path = f"atlases/{key}.webp"
                    if path == missing:
                        continue
                    count = FRAME_COUNTS[kind]
                    components[key] = {
                        "kind":kind, "lighting":mode, "frame_count":count,
                        "index":index, "origin":[15,20], "size":[8,8],
                        "spritesheet":{"file":path, "columns":4},
                    }
                    if Image:
                        sheet = Image.new("RGBA",(32,(count//4)*8),(0,0,0,0))
                        for f in range(count):
                            from PIL import ImageDraw
                            draw=ImageDraw.Draw(sheet)
                            x=(f%4)*8 + (0 if duplicate_frames else f%6)
                            y=(f//4)*8 + 1
                            draw.rectangle((x,y,x+1,y+1),fill=(52,200,145,255))
                        buf = io.BytesIO()
                        sheet.save(buf,format="WEBP",lossless=True)
                        z.writestr(path,buf.getvalue())
                    else:
                        z.writestr(path,b"RIFF-synthetic-WEBP")
                    index+=1
        z.writestr("manifest.json",json.dumps({"components":components}))

class SpriteLayerAuditTests(unittest.TestCase):
    def test_full_manifest_has_each_component_group(self):
        with tempfile.TemporaryDirectory() as tmp:
            archive=Path(tmp)/"fixture.zip"
            make_fixture(archive)
            result=audit_archive(archive,expected_sha=None,check_pixels=False)
            self.assertEqual([],result["errors"])
            self.assertEqual(28,len(result["layers"]))
            self.assertEqual(KINDS,result["counts"]["day"])
            self.assertEqual(KINDS,result["counts"]["night"])

    def test_missing_layer_fails(self):
        with tempfile.TemporaryDirectory() as tmp:
            archive=Path(tmp)/"fixture.zip"
            make_fixture(archive,missing="atlases/day_waterfall_0.webp")
            result=audit_archive(archive,expected_sha=None,check_pixels=False)
            self.assertTrue(any("Expected exactly 28" in msg for msg in result["errors"]))

    def test_approved_hash_cannot_be_faked_by_fixture(self):
        with tempfile.TemporaryDirectory() as tmp:
            archive=Path(tmp)/"fixture.zip"
            make_fixture(archive)
            result=audit_archive(archive,check_pixels=False)
            self.assertTrue(any("SHA-256 differs" in msg for msg in result["errors"]))

    def test_valid_synthetic_frames_vary_and_create_contact_strips(self):
        try:
            from PIL import Image
        except ImportError:
            self.skipTest("Optional Pillow not installed in CI")
        with tempfile.TemporaryDirectory() as tmp:
            archive=Path(tmp)/"fixture.zip"
            make_fixture(archive)
            out=Path(tmp)/"reports"
            result=audit_archive(archive,expected_sha=None,check_pixels=True,output=out)
            self.assertEqual([],result["errors"])
            self.assertTrue(result["pixel_checks"])
            self.assertEqual(28,len(list(out.glob("*.png"))))
            self.assertTrue(all(layer["varying_transitions"] > 1 for layer in result["layers"]))

if __name__ == "__main__":
    unittest.main()
