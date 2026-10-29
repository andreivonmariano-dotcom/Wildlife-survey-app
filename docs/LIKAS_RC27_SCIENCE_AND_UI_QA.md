# LIKAS RC27 | Primary Philippine herpetofauna observations and large-font header repair

Date: 29 October 2026, Asia/Manila. Working branch: `feature/likas-core-fieldwork-post-rc4`, draft PR #38. This is a source and CI candidate, not field-certified software.

## Biological evidence (Q1)

The existing corpus contains 96 species. RC27 contributes four narrow, source-attributed facts for three existing Philippine reptiles and three distinct publication links. It does **not** alter checklist identities, historical observations, local occurrence assumptions or conservation categories.

- `Hydrosaurus pustulatus`: one Natural history observation from Sanguila et al. 2016, *ZooKeys* 624:1–132, DOI https://doi.org/10.3897/zookeys.624.9814. Northeast Mindanao individuals active in daytime and found at night in river-site rock crevices, reported 170–420 m elevation. These are local observations, not nationwide behavioral/elevational bounds.
- `Varanus marmoratus`: one Habitat observation from Brown et al. 2013, *ZooKeys* 266:1–120, DOI https://doi.org/10.3897/zookeys.266.3982. Coastal, agricultural, scrub, forest-edge and secondary/primary forest contexts reported around the northern Sierra Madre. A study-site sample is not a predictive national habitat model.
- `Varanus dalubhasa`: Habitat and Distribution snapshots from Clores et al. 2021, *Journal of Asia-Pacific Biodiversity* 14:1–14, DOI https://doi.org/10.1016/j.japb.2020.11.005. A Caramoan water monitor documented near mangrove habitat on Lahuy Island, plus reported coastal/riparian context. No inferred current location or broad range.
- Each fact stores source year, DOI, author/source title, study part and an explicit sampling limit. The unchanged `Dicaeum kampalili` 1952 Identification gap stays blank.

New totals: 96 accounts, 456 cited fact records, 306 species/publication references. Empty sections: Identification 1, Habitat 38, Distribution 22, Natural history 64, Conservation 69.

## UI and failure investigation (Q2)

Actual RC26C API34 emulator run `40826262462` completed **failure**: 15 of 16 Android instrumentation tests succeeded. The Habitat screen's My records tab reported a 144px measured button, but only 121px appeared in the global visible rectangle at 200% font on a 320dp screen. The failed evidence contains old/new SQLite fixtures, startup capture and instrumentation, but not a complete successful 96-shot and page-audit archive. Do not claim RC26C emulator acceptance.

Source inspection suggests the shared navigation header keeps an arrow-only back button with the `UiKit.button` large-font minimum of 96dp even when its fixed header slot is 48dp. RC27 explicitly caps only this compact 200%-font **navigation back arrow** to a 48dp touch target, reducing vertical crowding without reducing main action sizes. Existing 200%-font Habitat tab, Home, Survey Setup, History, Species Library, Recorder, Comparison and Conservation test assertions remain. No survey-ending logic, SQLite code, user photos, recordings, GPS or startup artwork changed.

This is a hypothesized layout fix until a new full 16-test emulator run confirms the global visible geometry and screenshots. Larger tap controls elsewhere keep 96dp minimum at 200%.

## Physical Android testing (Q3)

`docs/LIKAS_RC27_PHONE_FIELD_QA.md` is a separate on-phone acceptance register. It defines installation without data loss, full three-state End Survey workflow, zero-detection effort, GPS/elevation provenance, photo and WAV linkage, export and restore, 200%/TalkBack, offline/battery/power, and signed backup comparisons. **NO PHYSICAL HANDSET TEST HAS BEEN PERFORMED.** Automated emulator/device fixture tests do not replace hardware results.

## Candidate identity and gates

`0.27.3-rc27-herpetofauna-field-preview`, versionCode 85, same package `org.phwildlife.survey.beta08`, same pinned signer, SQLite27. Required before shipping even a preview: source tests, JVM and Android compilation, signed binary inspection, Android 16/16, first-viewport large-text screenshot review, 96 rendered species, old/new disposable DB equality, verified direct APK file attachment. Field certification additionally requires an owner's actual hardware and backed-up historical records.
