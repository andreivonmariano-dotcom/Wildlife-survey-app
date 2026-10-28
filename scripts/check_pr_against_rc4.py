"""Trusted, read-only LIKAS PR gate for the historical default branch.

Never imports/executes source from a PR commit. Inspects tracked files via
git show and rejects branches older than RC4, unauthorized package/schema
changes, and sprite releases missing the exact approved media pack.
"""
import hashlib
import re
import subprocess
import sys

RC4 = "458e786827ba73bebabf5c373e2080015c5fd38c"
PKG = "org.phwildlife.survey.beta08"
ASSET = "app/src/main/assets/likas_sprite_assets.zip"
APPROVED_SHA = "def85e227587fdadee75956b6f105b57feb38f2bff27c6889c1f1edbfc042498"

def git(*args):
    return subprocess.run(["git", *args], capture_output=True)

def text_at(sha, path):
    r = git("show", f"{sha}:{path}")
    if r.returncode:
        raise ValueError("File missing at PR head: " + path)
    return r.stdout.decode("utf-8")

def version(gradle, key):
    m = re.search(r"(?m)^\s*" + re.escape(key) + r"\s+['\"]?([^'\"\n ]+)", gradle)
    if not m:
        raise ValueError("Missing " + key)
    return m.group(1)

def audit(sha):
    errors = []
    if not re.fullmatch(r"[0-9a-f]{40}", sha):
        return ["Invalid PR commit ID"]
    if git("merge-base", "--is-ancestor", RC4, sha).returncode:
        errors.append("PR does not descend from LIKAS v0.27.3 RC4")
    try:
        gradle = text_at(sha,"app/build.gradle")
        field = text_at(sha,"app/src/main/java/org/phwildlife/survey/FieldDb.java")
        launch = text_at(sha,"app/src/main/java/org/phwildlife/survey/LikasLaunchActivity.java")
        sprite = text_at(sha,"app/src/main/java/org/phwildlife/survey/LikasSpriteForestView.java")
        code = int(version(gradle,"versionCode"))
        name = version(gradle,"versionName")
        if version(gradle,"applicationId") != PKG:
            errors.append("Changed Android applicationId")
        if code < 49:
            errors.append("Outdated versionCode; required >= 49")
        if "super(context,NAME,null,27)" not in field:
            errors.append("SQLite schema 27 missing")
        if "if(oldVersion<27)upgradeTwentySeven(d)" not in field:
            errors.append("SQLite v27 migration missing")
        if "Explore. Record. Conserve." not in launch or "Resume survey" not in launch:
            errors.append("RC4 launch identity or resume action missing")
        if "FRAME_MS=200L" not in sprite and code >= 50:
            errors.append("5 FPS independent sprite renderer missing")
        player_active = bool(re.search(r"new\s+LikasSpriteForestView\s*\(", launch))
        archive = git("show", f"{sha}:{ASSET}")
        if player_active or code >= 50:
            if code < 50 or name == "0.27.3-rc4":
                errors.append("Sprite activation must use new versionCode >= 50 and new name")
            if not player_active:
                errors.append("New sprite release has no active sprite player")
            if archive.returncode or hashlib.sha256(archive.stdout).hexdigest() != APPROVED_SHA:
                errors.append("Missing/mismatched reviewed Prototype D binary ZIP")
    except (ValueError, UnicodeError) as exc:
        errors.append(str(exc))
    return errors

def main():
    if len(sys.argv) != 2:
        print("Usage: check_pr_against_rc4.py <40-character PR head sha>",file=sys.stderr)
        return 2
    errors = audit(sys.argv[1])
    if errors:
        print("LIKAS PR LINEAGE FAIL:\n- "+"\n- ".join(errors),file=sys.stderr)
        return 1
    print("LIKAS PR LINEAGE PASS: RC4 ancestry, identity, database and sprite release gating")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
