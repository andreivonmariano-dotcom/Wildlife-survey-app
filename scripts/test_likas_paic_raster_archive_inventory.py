#!/usr/bin/env python3
"""Synthetic tests for the archive inventory. No private LIKAS source data."""
import io
import json
import tempfile
import tarfile
import unittest
import zipfile
from pathlib import Path

from likas_paic_raster_archive_inventory import (
    eligible_member, list_archive_members, copy_member, inspect
)

class ArchiveInventoryTests(unittest.TestCase):
    def test_safe_member_path(self):
        self.assertTrue(eligible_member("nested/cop30_tile.tif"))
        self.assertFalse(eligible_member("../outside.tif"))
        self.assertFalse(eligible_member("/absolute.tif"))
        self.assertFalse(eligible_member("folder/../../outside.tif"))
        self.assertFalse(eligible_member("..\\outside.tif"))

    def test_zip_index_and_copy(self):
        with tempfile.TemporaryDirectory() as root:
            archive = Path(root) / "landcover.zip"
            with zipfile.ZipFile(archive, "w") as dst:
                dst.writestr("cover/tile_2021.tif", b"dummy-tif-header")
                dst.writestr("cover/legend.csv", "code,meaning\n10,Tree cover\n")
                dst.writestr("cover/notes.xml", "<metadata/>")
            members = list_archive_members(archive)
            self.assertEqual(len(members), 3)
            self.assertEqual(len([m for m in members if m["kind"] == "raster"]), 1)
            target = io.BytesIO()
            copy_member(archive, "cover/tile_2021.tif", target, max_bytes=100)
            self.assertEqual(target.getvalue(), b"dummy-tif-header")
            with self.assertRaises(ValueError):
                copy_member(archive, "cover/tile_2021.tif", io.BytesIO(), max_bytes=1)
            manifest = inspect([archive], checksum=False)
            self.assertIsNone(manifest["archives"][0]["sha256"])
            self.assertEqual(manifest["analytical_status"], "archive_inventory_only")
            self.assertEqual(manifest["raster_members"][0]["header_status"], "not_inspected")

    def test_targz_index_and_copy(self):
        with tempfile.TemporaryDirectory() as root:
            archive = Path(root) / "cop30.tar.gz"
            with tarfile.open(archive, "w:gz") as dst:
                payload = b"synthetic-DSM"
                item = tarfile.TarInfo("COP30/tile.tif")
                item.size = len(payload)
                dst.addfile(item, io.BytesIO(payload))
            members = list_archive_members(archive)
            self.assertEqual(len(members), 1)
            self.assertEqual(members[0]["kind"], "raster")
            target = io.BytesIO()
            copy_member(archive, "COP30/tile.tif", target, max_bytes=100)
            self.assertEqual(target.getvalue(), b"synthetic-DSM")
            manifest = inspect([archive], checksum=True)
            self.assertEqual(len(manifest["archives"][0]["sha256"]), 64)

    def test_reject_non_archive(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / "wrong.zip"
            path.write_bytes(b"not an archive")
            with self.assertRaises(ValueError):
                list_archive_members(path)

if __name__ == "__main__":
    unittest.main()
