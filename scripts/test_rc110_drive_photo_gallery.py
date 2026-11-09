#!/usr/bin/env python3
"""Repository-level regression gates for the RC110 offline photo gallery."""
import json
import re
from collections import Counter
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
MANIFEST=ROOT/"app/src/main/assets/offline_drive_gallery_manifest_v1.json"
DATA=json.loads(MANIFEST.read_text(encoding="utf-8"))
ITEMS=DATA["items"]
assert len(ITEMS)==333 and DATA["count"]==333
assert len({x["fileName"] for x in ITEMS})==333
assert len({x["driveFileId"] for x in ITEMS})==333
assert len({x["offlineAsset"] for x in ITEMS})==333
assert len({x["taxon"] for x in ITEMS})==197, "Trinomial images map to binomial taxa"
assert set(x["group"] for x in ITEMS)=={"Birds","Reptiles and Amphibians","Mammals"}
counter=Counter(x["taxon"] for x in ITEMS)
assert sum(n>1 for n in counter.values())==77
assert counter["Pithecophaga jefferyi"]==9
assert counter["Platymantis corrugatus"]==4
assert counter["Draco ornatus"]==2
assert counter["Acerodon jubatus"]==4
assert {x["originalTaxon"] for x in ITEMS if x["taxon"]!=x["originalTaxon"]}=={
    "Loriculus philippensis apicalis","Lipinia pulchella pulchella",
    "Boiga dendrophila latifasciata","Ahaetulla prasina preocularis"
}
for n,x in enumerate(sorted(ITEMS,key=lambda x:x["fileName"]),1):
    assert x["offlineAsset"]==f"offline_photos/drive_{n:04d}.jpg"
    assert re.fullmatch(r"[A-Za-z0-9_-]{20,80}",x["driveFileId"])
    assert x["rightsBasis"]=="owner_requested_in_app_preview"
java=ROOT/"app/src/main/java/org/phwildlife/survey"
directory=(java/"PhLibraryDirectoryActivity.java").read_text()
gallery=(java/"PhLibraryDriveGallery.java").read_text()
spotlight=(java/"ApoSpeciesOfDay.java").read_text()
media=(java/"PhLibrarySpeciesMedia.java").read_text()
assert "h.photo" not in directory and "photoOrPlaceholder(" not in directory
assert "addReaderGallery(a,parent,scientific)" in media
assert "MotionEvent.ACTION_UP" in gallery and "Swipe photos" in gallery
assert "TODAY IN THE FIELD GUIDE" in spotlight
assert "FROM THE SPECIES ACCOUNT" in spotlight
assert "private static final ZoneId PH_TIME=ZoneId.of(\"Asia/Manila\")" in spotlight
print("Manifest, 333 photo mappings, all-gallery controls and atlas-no-photo regression passed.")

# After preparing images, check every pack asset is present and EXIF-free.
assets=ROOT/"app/src/main/assets"
if (assets/ITEMS[0]["offlineAsset"]).exists():
    from PIL import Image
    total=0
    for record in ITEMS:
        p=assets/record["offlineAsset"]
        assert p.is_file(), str(p)
        data=p.read_bytes()
        assert data.startswith(b"\\xff\\xd8\\xff") and data.endswith(b"\\xff\\xd9")
        assert b"Exif\\x00\\x00" not in data
        with Image.open(p) as im:
            assert im.format=="JPEG" and max(im.size)<=1920
            assert "exif" not in im.info, p
        total+=len(data)
    print("Verified 333 offline JPEG assets:", total, "bytes, no EXIF.")
