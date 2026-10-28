"""Audit each separately animated LIKAS Prototype D sprite before an APK build.

Usage:
  python3 scripts/inspect_likas_sprite_layers.py --archive path/to/likas_sprite_assets.zip --output path/to/review
Uses only standard Python for ZIP/manifest checks. Pillow is optional for actual
visual frame comparisons and per-layer contact sheets (pip install Pillow).
Does NOT synthesize or replace missing approved media.
"""
import argparse
from collections import Counter
import hashlib
import io
import json
import math
from pathlib import Path
import zipfile

KINDS = {"cloud", "river", "waterfall", "wildlife", "canopy", "fern"}
FRAME_COUNTS = {"cloud": 40, "river": 40, "waterfall": 20,
                "wildlife": 40, "canopy": 20, "fern": 20}
EXPECTED_COUNT = 28
EXPECTED_SHA = "def85e227587fdadee75956b6f105b57feb38f2bff27c6889c1f1edbfc042498"


def audit_archive(archive, expected_sha=EXPECTED_SHA, check_pixels=True, output=None):
    archive = Path(archive)
    result = {"archive": str(archive), "errors": [], "warnings": [],
              "layers": [], "counts": {}, "pixel_checks": False}
    if not archive.is_file():
        result["errors"].append("Required approved archive not found")
        return result
    data = archive.read_bytes()
    result["size_bytes"] = len(data)
    result["sha256"] = hashlib.sha256(data).hexdigest()
    if expected_sha and result["sha256"] != expected_sha:
        result["errors"].append("Source media SHA-256 differs from approved Prototype D")
    try:
        z = zipfile.ZipFile(io.BytesIO(data))
    except zipfile.BadZipFile:
        result["errors"].append("Invalid ZIP")
        return result
    with z:
        members = z.namelist()
        if len(members) > 250 or sum(i.file_size for i in z.infolist()) > 150_000_000:
            result["errors"].append("Archive exceeds permitted resource bounds")
            return result
        if z.testzip() is not None:
            result["errors"].append("ZIP member CRC mismatch")
            return result
        try:
            manifest = json.loads(z.read("manifest.json"))
            components = manifest["components"]
        except (KeyError, json.JSONDecodeError, UnicodeError, TypeError):
            result["errors"].append("Missing or invalid manifest.json components")
            return result
        if not isinstance(components, dict) or len(components) != EXPECTED_COUNT:
            result["errors"].append("Expected exactly 28 independent components")
            return result
        for mode in ("day", "night"):
            for kind in ("fixed", "rocks"):
                if f"scene/{kind}_{mode}.webp" not in members:
                    result["errors"].append(f"Missing {mode} {kind} backdrop")
        try:
            from PIL import Image, ImageChops, ImageStat, ImageDraw
        except ImportError:
            Image = ImageChops = ImageStat = ImageDraw = None
            if check_pixels:
                result["errors"].append("Pillow required to inspect individual pixels")
        grouped = Counter()
        for key, c in components.items():
            issues = []
            try:
                kind = c["kind"]
                lighting = c["lighting"]
                count = int(c["frame_count"])
                idx = int(c["index"])
                w, h = [int(n) for n in c["size"]]
                x, y = [int(n) for n in c["origin"]]
                atlas = c["spritesheet"]
                cols = int(atlas["columns"])
                image_path = atlas["file"]
            except (KeyError, TypeError, ValueError, IndexError):
                result["errors"].append("Invalid sprite description: " + key)
                continue
            if kind not in KINDS or lighting not in ("day", "night"):
                issues.append("Unsupported kind or lighting")
            grouped[(lighting, kind)] += 1
            if count != FRAME_COUNTS.get(kind):
                issues.append("Unexpected 5 FPS loop-frame count")
            if min(w, h, cols, count) < 1 or idx < 0:
                issues.append("Invalid frame size, column count or frame index")
            if not (0 <= x <= 432 and 0 <= y <= 756 and x+w <= 432 and y+h <= 756):
                issues.append("Out-of-scene coordinate or dimensions")
            if image_path not in members:
                issues.append("Missing atlas image")
            layer = {"name": key, "mode": lighting, "kind": kind,
                     "frames": count, "size": [w, h], "origin": [x, y],
                     "atlas": image_path, "errors": issues}
            if Image and image_path in members and check_pixels:
                try:
                    sheet = Image.open(io.BytesIO(z.read(image_path))).convert("RGBA")
                    required_width = cols*w
                    required_height = math.ceil(count/cols)*h
                    if sheet.width < required_width or sheet.height < required_height:
                        issues.append("Atlas too small for declared cells")
                    else:
                        # Inspect *every* frame, not just first/last.
                        frames = [sheet.crop(((j%cols)*w, (j//cols)*h,
                                               (j%cols+1)*w, (j//cols+1)*h))
                                  for j in range(count)]
                        alpha = [im.getchannel("A").getextrema() for im in frames]
                        if any(lo == 255 and hi == 255 for lo, hi in alpha):
                            issues.append("Sprite frame has no transparent alpha")
                        if any(hi == 0 for lo, hi in alpha):
                            issues.append("Sprite frame is completely empty")
                        # A sprite must truly move/change between source frames.
                        varied = sum(frames[j].tobytes() != frames[(j+1)%count].tobytes()
                                     for j in range(count))
                        layer["varying_transitions"] = varied
                        if varied < 2:
                            issues.append("Frames do not vary independently")
                        if output:
                            out = Path(output)
                            out.mkdir(parents=True, exist_ok=True)
                            samples = [frames[j] for j in sorted(set([0, count//4, count//2,
                                                                      3*count//4, count-1]))]
                            thumb_w = min(220, max(64, w))
                            thumb_h = max(1, int(thumb_w*h/w))
                            canvas = Image.new("RGBA", (thumb_w*len(samples), thumb_h),
                                               (18, 45, 42, 255))
                            for n, frame in enumerate(samples):
                                frame.thumbnail((thumb_w, thumb_h), Image.Resampling.NEAREST)
                                canvas.alpha_composite(frame, (n*thumb_w, 0))
                            safe = "".join(ch if ch.isalnum() or ch in "-_" else "_"
                                           for ch in key)
                            filename = safe + ".png"
                            canvas.convert("RGB").save(out/filename)
                            layer["contact_sheet"] = filename
                except (OSError, ValueError, RuntimeError) as exc:
                    issues.append("Cannot decode atlas frame: " + type(exc).__name__)
            result["layers"].append(layer)
            result["errors"].extend(key + ": " + issue for issue in issues)
        result["counts"] = {mode: {kind: grouped[(mode, kind)] for kind in sorted(KINDS)}
                            for mode in ("day", "night")}
        for mode in ("day", "night"):
            if grouped[(mode, "waterfall")] != 4 or grouped[(mode, "river")] != 1:
                result["errors"].append(mode + " missing four falls or river")
            for kind in ("cloud", "canopy", "wildlife", "fern"):
                if not grouped[(mode, kind)]:
                    result["errors"].append(mode + " missing " + kind)
        result["pixel_checks"] = bool(Image and check_pixels)
    return result


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--archive", required=True)
    p.add_argument("--output", default=None, help="Create PNG strips and JSON review report")
    p.add_argument("--skip-pixel-check", action="store_true")
    p.add_argument("--skip-hash-check", action="store_true",
                   help="For *synthetic tests only*, never an approved asset import")
    args = p.parse_args()
    result = audit_archive(args.archive,
                           expected_sha=None if args.skip_hash_check else EXPECTED_SHA,
                           check_pixels=not args.skip_pixel_check, output=args.output)
    if args.output:
        dest = Path(args.output)
        dest.mkdir(parents=True, exist_ok=True)
        (dest/"component_audit.json").write_text(json.dumps(result, indent=2),
                                                   encoding="utf-8")
    print(json.dumps({k:v for k,v in result.items() if k != "layers"}, indent=2))
    return 1 if result["errors"] else 0

if __name__ == "__main__":
    raise SystemExit(main())
