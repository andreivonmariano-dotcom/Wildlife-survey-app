# v0.8 Reliability, User Experience and Analysis QA

## Lifecycle and memory
- Start a Bird Survey, type only half a species name and a distance. Press Android Home, switch to another app, return. Verify the original screen and fields.
- Force stop or swipe from Recent Apps, relaunch v0.8. If a survey is active, it should restore the survey. Verify the draft values, count, notes, chosen taxon and captured magnetic bearing.
- Open camera, capture bearing, return. Species and detection timestamp should remain.
- Tap the in-survey **Home** button, then reopen from **Resume active survey**. Unfinished draft should remain.
- Tap Android Back inside a running survey. Confirm that the exit warning appears and the data remains.
- Switch away mid-setup (before starting survey). Return and confirm method, station and typed parameters remain.
- Complete a survey. Verify the previous draft is cleared and no old entry appears in new surveys.

## Dashboard and data checks
- Dashboard survey total equals the survey-history count.
- Dashboard observation total excludes soft-deleted records.
- Total taxon groups and counts reflect real records. A species label is not certified taxonomy.
- Run SQLite quick_check. Confirm result OK on an intact database.
- Edit a record, check audit before/after. Soft delete a record. Confirm normal export excludes it, audit persists.

## Distance sampling
- Point count with a 30 m fixed radius: bird at 25 m may be a candidate if first-position/measurement flags pass. Bird at 40 m is preserved as an observation but is not an eligible distance candidate.
- Line-transect distance: with line 90° and bird 0° and r=20 m, perpendicular distance=20 m. Line and camera bearings both magnetic.
- Do not complete line surveys with missing or zero route length.
- End point count before planned duration and confirm incomplete status.
- Export samples.csv, distance_flatfile_draft.csv, distance_review.csv. A completed sample with no eligible detection for a target species appears as a sample row with blank distance.
- Filter flatfile by species and method before R. Check Area, Effort, Region.Label and Sample.Label.
- Do not interpret metadata_ready as density-estimation acceptance.

## Migration and installation safety
- v0.8 installs as a separate app. Leave v0.7 installed.
- Export ZIP from v0.7. Import it through v0.8's Import v0.7 backup.
- Verify survey count and raw observation IDs after import.
- Verify repeat import into nonempty database is rejected. Restore failure should roll back the whole transaction.
- Keep backup ZIP even if SQLite quick_check reports OK.
- Do not submit v0.8 data for research use without comparing exported measurements and effort to original field records.
