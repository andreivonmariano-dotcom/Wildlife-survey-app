"""Derive only APO RC111 launcher foreground from the exact owner-approved smaller-ribbon icon.
The original archived mountain icon and splash are never changed.
"""
from pathlib import Path
import hashlib
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/assets/apo_rc111_launcher_master.png"
TARGET = ROOT / "app/build/generated/rc111/res/drawable-nodpi/apo_rc111_launcher_foreground.png"
EXPECTED_SHA256 = "828b156748e5614c7a9b74efe90ee0272b1336b7f92334ebe5702a7959332c7b"
assert hashlib.sha256(SOURCE.read_bytes()).hexdigest() == EXPECTED_SHA256, "New APO launcher source changed"
image = Image.open(SOURCE).convert("RGB").resize((432, 432), Image.Resampling.LANCZOS)
transparent = Image.new("RGBA", image.size)
pixels, output = image.load(), transparent.load()
for y in range(image.height):
    for x in range(image.width):
        r, g, b = pixels[x, y]
        # Source foliage is near-zero red; ivory/jade ribbon is higher red.
        alpha = max(0, min(255, round((r - 10) * 255 / 23))) if g > 58 else 0
        output[x, y] = (r, g, b, alpha)
TARGET.parent.mkdir(parents=True, exist_ok=True)
transparent.save(TARGET, optimize=True)
assert TARGET.is_file() and TARGET.stat().st_size > 10000
print("Derived RC111 launcher:", hashlib.sha256(TARGET.read_bytes()).hexdigest())
