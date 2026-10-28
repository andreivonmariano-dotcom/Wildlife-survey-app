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
        code = int(version(gradle,"versionCode"))
        name = version(gradle,"versionName")
        if version(gradle,"applicationId") != PKG:
            errors.append("Changed Android applicationId")
        if code < 49:
            errors.append("Outdated versionCode; required >= 49")
        if code >= 50 and name == "0.27.3-rc4":
            errors.append("VersionCode increased without changing versionName")
        if "super(context,NAME,null,27)" not in field:
            errors.append("SQLite schema 27 missing")
        if "if(oldVersion<27)upgradeTwentySeven(d)" not in field:
            errors.append("SQLite v27 migration missing")
        # Two owner-approved RC4 startup paths exist. The original has an
        # on-splash Resume action. The newer minimal splash forwards to Home,
        # where Resume Survey remains available for an active field event.
        old_startup = "Explore. Record. Conserve." in launch and "Resume survey" in launch
        if not old_startup:
            home = text_at(sha,"app/src/main/java/org/phwildlife/survey/WelcomeActivity.java")
            minimal_startup = (
                'brand.setText("LIKAS")' in launch
                and "WelcomeActivity.class" in launch
                and 'putExtra("show_menu",true)' in launch
                and "root.setOnClickListener" in launch
                and 'UiKit.button(this,"Resume Survey","primary")' in home
                and re.search(r'new\s+Intent\(this,\s*MainActivity\.class\)\.putExtra\(\s*"resume"\s*,\s*true\s*\)', home)
            )
            if not minimal_startup:
                errors.append("No verified RC4 launch-to-Home Resume Survey path")
        player_active = bool(re.search(r"new\s+LikasSpriteForestView\s*\(", launch))
        if player_active:
            sprite = text_at(sha,"app/src/main/java/org/phwildlife/survey/LikasSpriteForestView.java")
            if "FRAME_MS=200L" not in sprite:
                errors.append("5 FPS independent sprite renderer missing")
        archive = git("show", f"{sha}:{ASSET}")
        if player_active:
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
