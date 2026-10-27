# LIKAS v0.18.5: permanent End Survey and analysis disposition

Source regression: beta v0.17 moved the Finish Survey control inside a
collapsed "Session options" container. Field researchers could not find a
survey-end action on the normal recording screen.

## Permanent screen action
- The recording screen has a fixed, always-visible **End Survey** action
  directly below the Save Observation / Review footer.
- This action is outside the scrolling form and outside collapsed settings.
- Opening it offers **Finish Complete**, **End Incomplete**, **Cancel Survey ·
  Not Counted**, and **Keep Recording**.
- The button works from any active station, between stations, after a
  planned transect, or during a paused survey.

## Outcome meaning
| Selected action | Database outcome | Data handling |
| --- | --- | --- |
| Finish Complete | complete, only if requirements met | eligible analytical export |
| End Incomplete | incomplete | retains survey/visits/detections, excluded from complete-only summary |
| Cancel Survey | not_counted | retains audit and reason, excluded from all analyses |
| Back / Keep Recording | still active | no change |

An unsaved observation causes a warning. The observer must save it or
explicitly discard that unfinished form entry, not a stored record.

## Evidence required to classify Complete
- Timed unsegmented survey: active survey effort at least planned duration,
  excluding documented pauses.
- Planned bird point route: at least one recorded point count, no incomplete
  visited counts and no missed/incomplete planned stations.
- Unplanned point count: at least one completed count.
- Structured herpetofauna and mammal methods: at least one sampling visit
  and no incomplete visit.
- Line and VES transects: positive measured route length **and** the
  researcher's explicit checkbox that the planned route was completed.
- Area search: positive measured searched area.
- Missing any required condition automatically classifies an attempted
  Finish Complete as **Incomplete**. It cannot silently become Complete.
- Complete does not certify field identification, true GPS uncertainty or
  independent scientific acceptance. Subsequent data audit still applies.

## Data retention
- Existing `surveys.outcome`, `complete`, `exclusion_reason`,
  `outcome_changed_at` and `survey_outcome_audit` columns are used.
  **No database schema migration** is needed.
- On Cancel Survey, the observer supplies a reason at least five characters
  long. Saved observations are archived, not deleted. General ZIP exports
  keep their outcome and audit records.
- Dashboard and research-only extracts use `outcome='complete'` for
  eligible records. `not_counted` remains an audited, separately excluded
  archive. Incomplete visits are preserved for follow-up.

## Regression tests
- Verify survey end button at screen bottom with a long form, keyboard open,
  during a point count, and in between stations.
- End before visiting any point. Result must be Incomplete.
- End after some but not all planned point counts. Result Incomplete.
- End with fully timed counts and no planned stations left. Result Complete.
- Finish transect with no measured route length: Incomplete.
- Finish transect with length but unticked confirmation: Incomplete.
- Finish completed transect with measured length and confirmation: Complete
  after planned active effort requirement.
- Finish area search with unknown searched area: Incomplete.
- Cancel with fewer than five characters reason: remain active.
- Cancel with valid reason: `outcome='not_counted'`, no deletion.
- Leave recorder with Home and resume it. Survey remains active; no
  accidental outcome is saved.
- Export ZIP after each outcome. Check `surveys.csv`,
  `survey_outcome_audit.csv` and saved observation provenance.
- Run `testDebugUnitTest` and `assembleDebug`. Then check on Android
  phones and compare signing certificate with currently installed release.

This work intentionally preserves the WBCP 2025 checklist research from the
parent branch without rebundling copyrighted data in the APK.
