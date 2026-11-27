#!/usr/bin/env python3
"""Read-only offline preflight for APO's likas-field-backup-v1 archives.

No network, no archive extraction into the app, no record-level data printed.
A passing check does not certify Android restore compatibility.
"""
import argparse
from contextlib import closing
import hashlib
import json
import re
import sqlite3
import tempfile
import zipfile
from pathlib import Path

DB = 'db/wildlife_field_v03.db'
FORMAT = 'likas-field-backup-v1'
MAX_FILE = 256 * 1024 * 1024
MAX_TOTAL = 768 * 1024 * 1024
MAX_ENTRIES = 8000
CORE = ('surveys', 'field_records', 'projects', 'sites')
COUNTS = CORE + ('point_counts', 'survey_stations', 'sampling_units',
    'survey_outcome_audit', 'track_points', 'observation_media', 'protocol_media',
    'acoustic_clips', 'acoustic_labels', 'record_edits', 'taxa_favorites',
    'route_templates', 'study_protocol_templates', 'trap_deployments', 'trap_checks',
    'trap_captures', 'coverboard_units', 'coverboard_checks')

def require(condition, message):
    if not condition:
        raise ValueError(message)

def safe(name):
    if len(name) > 230 or '..' in name or '\\' in name or '\x00' in name:
        return False
    if name in (DB, 'manifest.json'):
        return True
    return bool(re.fullmatch(r'media/(record_media|acoustic)/[A-Za-z0-9_-][A-Za-z0-9_.-]{0,150}', name)
        or re.fullmatch(r'prefs/[A-Za-z0-9_-][A-Za-z0-9_.-]{0,149}\.xml', name))

def no_duplicate_keys(pairs):
    result = {}
    for key, val in pairs:
        require(key not in result, 'Duplicate JSON key')
        result[key] = val
    return result

def sha_file(path):
    h = hashlib.sha256()
    with open(path, 'rb') as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()

def verify(path):
    path = Path(path)
    with tempfile.TemporaryDirectory(prefix='apo-backup-check-') as tmp, zipfile.ZipFile(path) as z:
        entries = z.infolist()
        names = [i.filename for i in entries]
        require(len(entries) <= MAX_ENTRIES, 'Too many entries')
        require(len(names) == len(set(names)), 'Duplicate ZIP entry')
        require(DB in names and 'manifest.json' in names, 'Missing database or manifest')
        total = 0
        for i in entries:
            require(not i.is_dir() and safe(i.filename), 'Unsafe archive entry')
            require(not i.flag_bits & 1, 'Encrypted ZIP entry is unsupported')
            require(i.file_size <= MAX_FILE, 'Oversized entry')
            total += i.file_size
            require(total <= MAX_TOTAL, 'Archive exceeds uncompressed size limit')
        require(z.getinfo('manifest.json').file_size < 250000, 'Oversized manifest')
        manifest = json.loads(z.read('manifest.json'), object_pairs_hook=no_duplicate_keys)
        require(isinstance(manifest, dict) and manifest.get('format') == FORMAT, 'Unsupported backup format')
        schema = manifest.get('sqliteSchema')
        require(type(schema) is int and 27 <= schema <= 29, 'Unsupported database schema')
        hashes = manifest.get('files')
        require(isinstance(hashes, dict) and set(hashes) == set(names)-{'manifest.json'}, 'Manifest file set mismatch')
        db_path = Path(tmp) / 'snapshot.sqlite'
        actual_total = 0
        for name, expected in hashes.items():
            require(isinstance(expected, str) and re.fullmatch(r'[a-fA-F0-9]{64}', expected), 'Invalid SHA-256 field')
            h = hashlib.sha256()
            count = 0
            out = db_path.open('wb') if name == DB else None
            try:
                with z.open(name) as f:
                    for chunk in iter(lambda: f.read(1024*1024), b''):
                        count += len(chunk)
                        actual_total += len(chunk)
                        require(count <= MAX_FILE and actual_total <= MAX_TOTAL, 'Expanded data exceeds safety limits')
                        h.update(chunk)
                        if out:
                            out.write(chunk)
            finally:
                if out:
                    out.close()
            require(count == z.getinfo(name).file_size and h.hexdigest() == expected.lower(), 'Checksum or size mismatch: '+name)
        with closing(sqlite3.connect(db_path.as_uri()+'?mode=ro&immutable=1', uri=True)) as con:
            con.execute('PRAGMA trusted_schema=OFF')
            require(con.execute('PRAGMA user_version').fetchone()[0] == schema, 'Database schema mismatch')
            require(con.execute('PRAGMA quick_check').fetchall() == [('ok',)], 'SQLite quick_check failed')
            tables = {r[0] for r in con.execute("SELECT name FROM sqlite_master WHERE type='table'")}
            require(set(CORE) <= tables, 'Required database tables missing')
            counts = {t: con.execute('SELECT COUNT(*) FROM "'+t+'"').fetchone()[0] for t in COUNTS if t in tables}
            active = con.execute('SELECT COUNT(*) FROM surveys WHERE ended IS NULL').fetchone()[0]
        require(active == 0, 'Active survey found; backup requires ended surveys')
        return {'format': FORMAT, 'schema': schema, 'zip_sha256': sha_file(path),
            'archive_bytes': path.stat().st_size, 'database_sha256': hashes[DB].lower(),
            'record_media_files': sum(n.startswith('media/record_media/') for n in hashes),
            'acoustic_files': sum(n.startswith('media/acoustic/') for n in hashes),
            'preferences_files': sum(n.startswith('prefs/') for n in hashes),
            'active_surveys': active, 'row_counts': counts,
            'scope': 'Archive integrity preflight only; does not prove full Android restore or semantic completeness.'}

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('archive', type=Path)
    args=parser.parse_args()
    try:
        print(json.dumps(verify(args.archive), indent=2, sort_keys=True))
    except (ValueError, OSError, sqlite3.Error, zipfile.BadZipFile, RuntimeError, TypeError) as exc:
        parser.exit(1, 'Backup verification failed: '+str(exc)+'\n')
if __name__ == '__main__':
    main()
