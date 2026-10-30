"""RC43 exact pixel-asset lock and source UI wiring tests."""
import hashlib
import re
import struct
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/"app/src/main/res/drawable-nodpi"
UI=ROOT/"app/src/main/java/org/phwildlife/survey/LikasPortalUi.java"
APP=ROOT/"app/src/main/java/org/phwildlife/survey/WelcomeActivity.java"

# Byte-for-byte reference hashes from the user's approved locked 18-icon ZIP.
APPROVED={
    "likas_approved_analysis.png": "5c767efcc98dbdae6df55886ac793920baec4bbd6e4b59d189e80971c551c226",
    "likas_approved_backup.png": "4069bd770371765aa59e49e1473475a7bcf3e75eeec7c1d0ac6a4db20cbdb2ba",
    "likas_approved_birds_writhed_hornbill.png": "dac6a57debcfc0d0095479050c3ed8d386919cd944cd4136126cf055f7ec7517",
    "likas_approved_camera.png": "07b46b321121a918f3c29c109b4612b72ca9fa997aa259485ae674901f40f275",
    "likas_approved_fieldwork.png": "e0b162cd02cdeefe203534b2196bcf4264b8be3f4d8979a9f40bd3b01ec4d8dc",
    "likas_approved_gps.png": "b19225a93f18b4b880ee902939cdf3be072270b06a202028cb1520e9611f86ee",
    "likas_approved_habitats.png": "fb5eb290185a4d0abfabce4274cb90fe342259d28c7693efc2df4b3e4c7663bc",
    "likas_approved_help.png": "ef6abd373882d192eeb884a7da151dc195f6e913376314b00f6517a6118ebbc9",
    "likas_approved_herpetofauna_staurois_natator.png": "ab6564abd068928033ed1bf689f0dae0162aaf3a04179c58761ca3b0d83c4b27",
    "likas_approved_history.png": "86dde0f33658a092170ee52b264263d84b52a24791796e9cf9474b52c255980f",
    "likas_approved_mammals_rusa_marianna.png": "1bf7d177a0175febbb515e5038061d9c87093e0198e669c13c11b3ee96d586ae",
    "likas_approved_news.png": "8dec0de1fd6529d1940befa8239b7f66d36789d45cc71210163c810bfb8f240f",
    "likas_approved_offline.png": "6eba566562773eddedd41f40f35e9ce0d6c442d721b738b219d91a08841a125c",
    "likas_approved_projects.png": "de7ba0ef433d1f27d480840ef40aa0ba54e4a01125cb565a1560ae4060803e49",
    "likas_approved_reports.png": "1a3a09b98441ab0fccfc3f27ac69222161784186496880a5f92a17c2571a1c09",
    "likas_approved_research.png": "1846d11f4ef14ba9b1592b685489f451151a13fb911e2f9fa121608d2ace07a7",
    "likas_approved_settings.png": "96163bc89ed44e462d022e12bbec9b2fbb112113d692cf96009788ccc9cc217d",
    "likas_approved_species_library.png": "b9cf683442be60ea5397542124fd4a3119fc6c7e6d98538864e04d9a670ce139",
}

class Rc43ApprovedIconLock(unittest.TestCase):
    def test_all_18_exact_png_bytes_and_dimensions(self):
        actual={p.name for p in RES.glob("likas_approved_*.png")}
        self.assertEqual(actual,set(APPROVED))
        for name,digest in APPROVED.items():
            data=(RES/name).read_bytes()
            self.assertEqual(hashlib.sha256(data).hexdigest(),digest,name)
            self.assertEqual(data[:8],b"\x89PNG\r\n\x1a\n")
            self.assertEqual(struct.unpack(">II",data[16:24]),(512,512))

    def test_navigation_uses_approved_bitmaps_and_crisp_sampling(self):
        ui=UI.read_text(encoding="utf-8")
        mapped=set(re.findall(r'R\.drawable\.(likas_approved_[a-z_]+)',ui))
        self.assertEqual(mapped,{n[:-4] for n in APPROVED})
        self.assertIn("addApprovedIcon(art,a,icon,field?CREAM:CANOPY,62,36)",ui)
        self.assertIn("addApprovedIcon(art,a,symbol,CANOPY,49,30)",ui)
        self.assertIn("addApprovedIcon(plate,a,symbol,CANOPY,47,28)",ui)
        self.assertIn("setFilterBitmap(false)",ui)
        self.assertIn("IMPORTANT_FOR_ACCESSIBILITY_NO",ui)
        self.assertIn("new Glyph(a,symbol,glyphColor)",ui) # fallback for unmapped tools

    def test_survey_animals_are_the_locked_philippine_references(self):
        ui=UI.read_text(encoding="utf-8")
        self.assertIn('case "bird":case "birds":return R.drawable.likas_approved_birds_writhed_hornbill;',ui)
        self.assertIn('return R.drawable.likas_approved_herpetofauna_staurois_natator;',ui)
        self.assertIn('return R.drawable.likas_approved_mammals_rusa_marianna;',ui)
        app=APP.read_text(encoding="utf-8")
        for kind in ("Birds","Herpetofauna","Mammals"):
            self.assertIn('()->setup("'+kind+'")',app)
        self.assertNotIn('surveyTile(second,"Custom"',app)
        self.assertNotIn('private void customPicker()',app)
        for illegal in ("likas_approved_plants.png","likas_approved_invertebrates.png"):
            self.assertNotIn(illegal,APPROVED)
            self.assertFalse((RES/illegal).exists())

    def test_launcher_identity_is_not_replaced(self):
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        self.assertIn('android:icon="@mipmap/ic_launcher"',manifest)
        self.assertIn('android:roundIcon="@mipmap/ic_launcher"',manifest)
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 103",gradle)
        self.assertIn("versionName '0.27.3-rc43-approved-icons-preview'",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        field=(ROOT/"app/src/main/java/org/phwildlife/survey/FieldDb.java").read_text()
        self.assertIn("super(context,NAME,null,27)",field)

if __name__=="__main__":
    unittest.main()
