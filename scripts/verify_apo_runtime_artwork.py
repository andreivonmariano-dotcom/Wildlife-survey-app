#!/usr/bin/env python3
"""Keep original artwork and its provenance in the source archive; ship display resources."""
from pathlib import Path
import argparse
import hashlib
import zipfile

ROOT=Path(__file__).resolve().parents[1]
MASTERS={
    "apo_official_master.png":"6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7",
    "apo_compact_launcher_master.png":"828b156748e5614c7a9b74efe90ee0272b1336b7f92334ebe5702a7959332c7b",
}
DRAWABLES={"apo_splash_mark.png","apo_launcher_foreground.png","apo_compact_launcher_foreground.png"}

def verify(apk):
    for name,expected in MASTERS.items():
        source=ROOT/"app/src/main/assets"/name
        assert hashlib.sha256(source.read_bytes()).hexdigest()==expected,name
    with zipfile.ZipFile(apk) as archive:
        names=set(archive.namelist())
        for name in MASTERS:
            assert not any(n.rsplit("/",1)[-1]==name for n in names),f"Build-only artwork in APK: {name}"
        for name in DRAWABLES:
            matches=[n for n in names if n.startswith("res/") and n.rsplit("/",1)[-1]==name]
            assert len(matches)==1,(name,matches)
            assert archive.read(matches[0]).startswith(b"\x89PNG\r\n\x1a\n"),name
    print("Original artwork hashes retained; master copies excluded; three approved display resources present.")

if __name__=="__main__":
    parser=argparse.ArgumentParser()
    parser.add_argument("apk",type=Path)
    verify(parser.parse_args().apk)
