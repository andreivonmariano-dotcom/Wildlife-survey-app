#!/usr/bin/env python3
"""Create a transparent Android adaptive foreground from the approved compact APO art."""
from pathlib import Path
from hashlib import sha256
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
MASTER = ROOT / "app/src/main/assets/apo_compact_launcher_master.png"
OUTPUT = ROOT / "app/build/generated/apo-compact/res/drawable-nodpi/apo_compact_launcher_foreground.png"
SHA = "828b156748e5614c7a9b74efe90ee0272b1336b7f92334ebe5702a7959332c7b"

def main():
    if sha256(MASTER.read_bytes()).hexdigest() != SHA:
        raise SystemExit("APO compact icon master digest mismatch")
    image = Image.open(MASTER).convert("RGB")
    if image.size != (1254, 1254):
        raise SystemExit("Unexpected approved APO icon dimensions")
    red = image.getchannel("R")
    alpha = red.point(lambda r: 0 if r <= 22 else 255 if r >= 45 else round((r-22)*255/23))
    foreground = image.convert("RGBA")
    foreground.putalpha(alpha)
    # Reduce the artwork, never enlarge it. The Android adaptive icon mask
    # crops its 432px viewport; an inset prevents the ribbon nearing the edges.
    foreground = foreground.resize((310, 310), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
    canvas.alpha_composite(foreground, ((432 - 310)//2, (432 - 310)//2))
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(OUTPUT, optimize=True)
    print("Prepared approved compact APO launcher foreground:", OUTPUT)

if __name__ == "__main__":
    main()
