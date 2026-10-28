"""Block LIKAS builds on older development branches and unverified sprite packages.

--stage permits unconnected RC4-derived experiments at versionCode 49.
--release requires a connected, reviewed 5 FPS sprite pack and code >= 50.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
POLICY = ROOT / "config/likas_active_lineage.json"

def version(text, field):
    match = re.search(r"(?m)^\s*" + re.escape(field) + r"\s+(\d+|['\"][^'\"]+['\"])", text)
    if not match:
        raise ValueError("Missing Gradle field: " + field)
    return match.group(1).strip("'\"")

def source_audit(policy, gradle, db, launch, sprite, asset_exists):
    errors = []
    code = int(version(gradle, "versionCode"))
    name = version(gradle, "versionName")
    if version(gradle, "applicationId") != policy["application_id"]:
        errors.append("Android applicationId changed")
    if code < policy["minimum_version_code"]:
        errors.append("Version code is older than RC4 code 49")
    if f"super(context,NAME,null,{policy['sqlite_schema']})" not in db:
        errors.append("SQLite schema 27 not preserved")
    if "if(oldVersion<27)upgradeTwentySeven(d)" not in db:
        errors.append("Required schema 27 migration absent")
    if "Explore. Record. Conserve." not in launch:
        errors.append("Approved startup tagline missing")
    if "Resume survey" not in launch or "Open LIKAS" not in launch:
        errors.append("Startup navigation actions missing")
    if "FRAME_MS=200L" not in sprite:
        errors.append("Approved independent sprite rate is not 5 FPS")
    active = bool(re.search(r"new\s+LikasSpriteForestView\s*\(", launch))
    if active and not asset_exists:
        errors.append("Activated sprite player lacks reviewed media pack")
    if active and code < policy["next_release_version_code_minimum"]:
        errors.append("Activated sprites require versionCode >= 50")
    return errors, code, name, active

def git_ancestry(root, ancestor):
    shallow = subprocess.run(["git", "rev-parse", "--is-shallow-repository"],
                            cwd=root, capture_output=True, text=True)
    if shallow.returncode or shallow.stdout.strip() != "false":
        return "Full Git history required (actions/checkout fetch-depth: 0)"
    result = subprocess.run(["git", "merge-base", "--is-ancestor", ancestor, "HEAD"],
                            cwd=root, capture_output=True)
    if result.returncode:
        return "Wrong LIKAS source lineage: RC4 commit " + ancestor + " is not an ancestor of HEAD"
    return None

def validate_pack(path, expected_sha):
    if not path.is_file():
        return ["Reviewed sprite pack missing"]
    if path.stat().st_size > 20_000_000:
        return ["Sprite pack exceeds review envelope"]
    if hashlib.sha256(path.read_bytes()).hexdigest() != expected_sha:
        return ["Sprite pack fingerprint differs from reviewed Prototype D"]
    errors = []
    try:
        with zipfile.ZipFile(path) as z:
            infos = z.infolist()
            if len(infos) > 200 or sum(m.file_size for m in infos) > 140_000_000:
                errors.append("Sprite archive resource bounds exceeded")
            if z.testzip() is not None:
                errors.append("Sprite archive CRC failed")
            names = set(z.namelist())
            for mode in ("day", "night"):
                for kind in ("fixed", "rocks"):
                    if f"scene/{kind}_{mode}.webp" not in names:
                        errors.append(f"Missing {kind} {mode} plate")
            components = json.loads(z.read("manifest.json").decode("utf-8"))["components"]
            if len(components) != 28:
                errors.append("Expected 28 independent sprite components")
            for key, obj in components.items():
                if obj["spritesheet"]["file"] not in names:
                    errors.append("Missing atlas: " + key)
                if int(obj["frame_count"]) < 2 or int(obj["spritesheet"]["columns"]) < 1:
                    errors.append("Invalid frame specification: " + key)
    except (OSError, KeyError, ValueError, zipfile.BadZipFile) as err:
        errors.append("Sprite pack could not be checked: " + type(err).__name__)
    return errors

def check(release=False):
    policy = json.loads(POLICY.read_text(encoding="utf-8"))
    java = ROOT / "app/src/main/java/org/phwildlife/survey"
    asset = ROOT / policy["sprite_archive"]
    errors, code, name, active = source_audit(
        policy,
        (ROOT/"app/build.gradle").read_text(encoding="utf-8"),
        (java/"FieldDb.java").read_text(encoding="utf-8"),
        (java/"LikasLaunchActivity.java").read_text(encoding="utf-8"),
        (java/"LikasSpriteForestView.java").read_text(encoding="utf-8"),
        asset.exists()
    )
    git_issue = git_ancestry(ROOT, policy["required_ancestor_commit"])
    if git_issue:
        errors.append(git_issue)
    if asset.exists():
        errors.extend(validate_pack(asset, policy["sprite_archive_sha256"]))
    if release or code > policy["minimum_version_code"] or active:
        if code < policy["next_release_version_code_minimum"]:
            errors.append("New sprite release needs versionCode >= 50")
        if not active:
            errors.append("New sprite release must connect its native player")
        if not asset.exists():
            errors.append("New sprite release requires matching approved archive")
        if name == policy["baseline_version_name"]:
            errors.append("New release cannot reuse RC4 versionName")
    if errors:
        print("LIKAS LINEAGE FAILED:\n- " + "\n- ".join(errors), file=sys.stderr)
        return 1
    print(f"LIKAS lineage PASS: {name}, code {code}, sprite {'active' if active else 'staged'}, "
          f"pack {'verified' if asset.exists() else 'not installed'}")
    return 0

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--release", action="store_true")
    parser.add_argument("--stage", action="store_true")
    args = parser.parse_args()
    sys.exit(check(args.release))
