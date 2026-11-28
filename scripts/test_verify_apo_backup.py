import hashlib,json,sqlite3,tempfile,unittest,zipfile
from pathlib import Path
from verify_apo_backup import verify,DB,FORMAT

class BackupVerifier(unittest.TestCase):
    def setUp(self):
        self.dir=tempfile.TemporaryDirectory()
        self.root=Path(self.dir.name)
        self.db=self.root/'test.db'
        with sqlite3.connect(self.db) as c:
            c.executescript('PRAGMA user_version=29; CREATE TABLE surveys(id TEXT, ended TEXT); CREATE TABLE field_records(id INTEGER); CREATE TABLE projects(id TEXT); CREATE TABLE sites(id TEXT); INSERT INTO surveys VALUES (\'synthetic\',\'2026-11-27\'); INSERT INTO field_records VALUES(1);')
    def tearDown(self): self.dir.cleanup()
    def make(self, changes=None, extra=None, schema=29, wrong_hash=False, duplicate=False):
        items={DB:self.db.read_bytes(),'media/record_media/qa.jpg':b'fixture','media/acoustic/qa.wav':b'fixture','prefs/qa.xml':b'<map />'}
        if changes: items.update(changes)
        manifest={'format':FORMAT,'sqliteSchema':schema,'files':{n:hashlib.sha256(b).hexdigest() for n,b in items.items()}}
        if wrong_hash: manifest['files']['media/acoustic/qa.wav']='0'*64
        p=self.root/'backup.zip'
        with zipfile.ZipFile(p,'w',zipfile.ZIP_DEFLATED) as z:
            for n,b in items.items(): z.writestr(n,b)
            z.writestr('manifest.json',json.dumps(manifest))
            if extra: z.writestr(extra,b'unlisted')
            if duplicate:
                import warnings
                with warnings.catch_warnings():
                    warnings.simplefilter('ignore');z.writestr(DB,items[DB])
        return p
    def test_valid(self):
        r=verify(self.make());self.assertEqual(r['row_counts']['surveys'],1);self.assertEqual(r['acoustic_files'],1);self.assertEqual(r['active_surveys'],0)
    def test_tampered_hash(self):
        with self.assertRaises(ValueError): verify(self.make(wrong_hash=True))
    def test_unlisted(self):
        with self.assertRaises(ValueError): verify(self.make(extra='prefs/extra.xml'))
    def test_traversal(self):
        with self.assertRaises(ValueError): verify(self.make(changes={'media/acoustic/../evil.wav':b'x'}))
    def test_nested(self):
        with self.assertRaises(ValueError): verify(self.make(changes={'media/acoustic/nested/qa.wav':b'x'}))
    def test_duplicate(self):
        with self.assertRaises(ValueError): verify(self.make(duplicate=True))
    def test_schema_mismatch(self):
        with self.assertRaises(ValueError): verify(self.make(schema=28))
    def test_active_survey(self):
        with sqlite3.connect(self.db) as c: c.execute("UPDATE surveys SET ended=NULL")
        with self.assertRaises(ValueError): verify(self.make())
    def test_invalid_database(self):
        with self.assertRaises((ValueError,sqlite3.Error)): verify(self.make(changes={DB:b'broken'}))
    def test_missing_required_table(self):
        with sqlite3.connect(self.db) as c: c.execute('DROP TABLE sites')
        with self.assertRaises(ValueError): verify(self.make())
if __name__=='__main__': unittest.main()
