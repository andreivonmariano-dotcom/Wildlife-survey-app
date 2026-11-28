"""Static manifest and explicit Activity navigation contract; native tests cover behavior."""
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
ANDROID="{http://schemas.android.com/apk/res/android}"

class ApoNavigationGraph(unittest.TestCase):
    def test_declared_activity_routes(self):
        app=ET.parse(ROOT/"app/src/main/AndroidManifest.xml").getroot().find("application")
        declared=[node.attrib[ANDROID+"name"].split(".")[-1] for node in app.findall("activity")]
        self.assertEqual(len(declared),len(set(declared)))
        self.assertTrue(all((JAVA/(name+".java")).is_file() for name in declared))
        exported=[node.attrib[ANDROID+"name"].split(".")[-1] for node in app.findall("activity") if node.get(ANDROID+"exported")=="true"]
        self.assertEqual(exported,["LikasLaunchActivity"])
        for file in JAVA.glob("*.java"):
            text=file.read_text(encoding="utf-8")
            for target in re.findall(r"new\s+Intent\s*\([^;\r\n]*?,\s*(\w+Activity)\.class",text):
                self.assertIn(target,declared,(file.name,target))
    def test_setup_and_history_back_share_native_action(self):
        setup=(JAVA/"WelcomeActivity.java").read_text(encoding="utf-8")
        self.assertIn('backDraftProfile=profile;',setup)
        self.assertIn('if(restore)restoreSetup();',setup)
        self.assertIn('this::leaveSetup',setup)
        self.assertIn('leaveSetup();',setup)
        history=(JAVA/"HistoryActivity.java").read_text(encoding="utf-8")
        self.assertIn('onBackPressed(){returnToCaller();}',history)
        self.assertIn('fromRecorder()||fromMap()',history)
        backup=(JAVA/"FieldBackupActivity.java").read_text(encoding="utf-8")
        self.assertIn('onBackPressed(){requestBack();}',backup)
        self.assertIn('if(busy){',backup)

if __name__=="__main__":unittest.main()
