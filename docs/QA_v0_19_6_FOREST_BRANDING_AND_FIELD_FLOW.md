# LIKAS v0.19.6: forest branding, performance audit and survey journey

Date: 2026-10-27
Source branch: `feature/v0.19.5-ui-audit-cleanup`
New branch: `feature/v0.19.6-forest-branding-field-flow`
Scope: source inspection, UI and native drawable updates, policy tests. Not a substitute for a device run.

## Asset provenance

The repository contained `LikasForestView.java`, `LikasLaunchActivity.java`, and `res/drawable/likas_mark.xml`. No standalone GIF, WebP, PNG animation sheet, exported sprite atlas, video, or generated icon bitmap was present on the inspected source and historical LIKAS branches.

The new result builds on these existing native assets. It does not claim to import unretrieved images from another chat.

## Forest splash

- **Pixel art:** one reusable offscreen 216×384 ARGB_8888 bitmap. Approximate pixel storage is 331,776 bytes (324 KiB). Android view/surface and internal drawing memory are extra.
- **Motion:** nearest-neighbor upscaling, repeating parallax tree layers, drifting block clouds, moving waterfall highlights and forest-floor plants. Fireflies pulse at night.
- **Day cycle:** handset local time. Dawn 05:00–07:00 and dusk 17:30–19:00 use continuous color blending, rather than an abrupt day/night toggle.
- **Frame budget:** animation redraw delay 100 ms (up to 10 FPS). Reduced/disabled system animator setting prevents the repeating redraw loop.
- **Lifecycle:** animation stops redrawing when its window is no longer visible. No additional GPS listeners, audio, remote requests, animation threads, or decoded GIF frames.
- **Branding:** replaced the circle icon with a consistent leaf/compass mark and a separate adaptive-icon vector foreground. The launcher mark is not an upscaled raster image.
- **Resume:** an unfinished survey shows Resume Survey as the primary control. New Survey changes to Home & Tools to avoid promising a second concurrent survey. Without an unfinished survey, New Survey is the primary control.

## Static performance audit

| Area | Code observation | Risk / next test |
|---|---|---|
| Forest | One small bitmap and one Canvas; 100 ms repeat redraw | Compare CPU/GPU and battery use of a stationary 5-minute splash at day and night |
| Track GNSS | `TrackService.onLocationChanged` calls `record()`, which begins a SQLite transaction on the listener thread | At a 1 Hz fix cadence, up to about 3,600 write events per hour; benchmark callback delay, durable order, and battery before introducing a serial I/O writer |
| Recording timer | `MainActivity.visibleTimer` runs at 1 Hz, station/progress queries throttled to about every 4 seconds | Observe UI frame time and SQLite read time over 30 minutes; refresh progress only on transitions if supported by evidence |
| Picker | Background catalog query and in-memory sorting, up to 5,000 candidates; paged view output | Profile peak allocations and scroll time for birds, herpetofauna and mammals |
| ZIP | Research ZIP export already writes from a worker thread | Verify output data parity on large survey and cancellation, and test interruption handling |
| Station elevation | MSL conversion worker does not run on recorder main UI thread | Compare first-use conversion latency and battery across Android versions |
| Dashboard | Duplicate first render removed in v0.19.5 | Check cold navigation and history page memory on low-cost handsets |
| Launch | Existing activity checks active survey via read-only SQLite | Measure wake and Home/Resume latency with a large migrated survey database |

Numbers above are code-derived budgets or illustrative write counts, **not device benchmarks**.

## Automated survey-flow scenarios

JVM `FieldWorkflowScenarioTest` runs these paths using the app's real GPS and survey-close policies:

1. No active survey -> startup permits New Survey -> fresh non-mock GNSS fix -> active bird point count -> partially visited stations classify Incomplete -> all completed classify Complete.
2. Expired, mocked, inaccurate, or nonfinite GPS values cannot pass the 10 m/10 second survey-start gate.
3. Mammal station sampling requires a finished visit for Complete. Early termination stays Incomplete.
4. Canceled herpetofauna visit returns Not Counted, regardless of progress.
5. Line transect requires active effort, a measured route length, and route completion confirmation.
6. Area search requires an area measurement before Complete.

`LikasUiRulesTest` covers day/night blend clock boundaries. `scripts/test_likas_branding.py` validates Android resource connections, launcher behavior, forest budget and an always-present End Survey action.

These **simulate domain states**, not Android taps, sensor delivery, visual layout, actual GPS motion, or exported ZIP bytes. A full end-to-end device pass is still required.

## Physical handset acceptance matrix

### Startup and branding
- Start on a phone around 360 dp wide, a 320 dp wide phone, and a tablet. Verify pixel edges are crisp, content readable over all backgrounds, no clipping at 200% font, and the icon looks coherent in circular and squircle adaptive masks.
- In device timezone Asia/Manila, test at 04:59, 06:00, 07:00, 17:30, 18:15 and 19:00. Check the intended dawn/dusk palette and smooth transitions. Avoid changing the phone time mid-survey on a production dataset.
- Return to Home, lock the screen, reopen LIKAS. Confirm animation is not actively redrawing in the background and the last in-progress survey remains intact.
- Disable system animations and check that forest motion stops without hiding the content.

### Survey interactions
- Start Birds > point count > acquire fresh GPS. Create 3 planned stations, start count at each real station, use Identify Species, choose an unknown label, save observation and photo.
- Pause and resume during a count. Confirm active effort excludes pauses and the total wall clock continues. Complete all points and select End Survey > Finish Complete. Verify station IDs, GPS time and survey outcome.
- Repeat and end early. Verify Incomplete status and preserved detections.
- Open Herpetofauna > visual encounter, select Amphibians then Reptiles, verify method-specific labels and evidence. Cancel with a valid reason. Verify Not Counted and the audit entry.
- Open Mammals > track/sign or camera-trap check, complete a station and capture evidence. Confirm no bird distance-only fields appear.
- Walk a bird line transect with two saved detections, verify route path and measured length, then confirm complete. Missing measured effort must force Incomplete.
- Check Review/History, edit log and ZIP exports. Confirm all previous fields, scientific name provenance, source version, elevation datum, vertical accuracy and outcome remain unchanged.
- During a paused survey, background the app and return through Resume Survey. Verify GPS notification/manual stop behavior and retained unsubmitted draft.

### Speed, RAM and battery
- Time cold start, survey setup, Resume, observation Save, species search and History transitions (median and P95 from at least 20 repetitions). Compare with v0.19.5 on the same handset.
- Run a 30–60 minute outdoor survey with GPS logging and a 100-observation history. Collect dropped frames, main-thread database stalls, memory peak, battery percent/energy use, temperature and GPS service uptime.
- Compare idle 5-minute launch screen with a static version. No claim of battery savings is made without this comparison.
- Test phone heat and very poor GNSS visibility, plus unplanned navigation out of the Activity.

## Risk boundaries

The PR does not change SQLite schema, survey completion policy, track-point storage, scientific exports, species checklists, taxonomic verification or the automated altitude conversion. Improving GNSS write batching is a separate research integrity change. Never discard rejected points, duplicate station GPS snapshots or change a canceled survey into a complete one for UI convenience.
