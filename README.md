# Wildlife Survey Beta v0.6: Welcome and protocol-specific survey setup

Standalone offline Android prototype focused on Philippine vertebrate taxa. No SMART, KALUMBATA, or Bluetooth integration.

## Welcome screen
The launcher opens **WelcomeActivity** instead of the field entry form.
- **Bird survey**: point count, line transect, area search, opportunistic.
- **Herpetofauna survey**: visual encounter survey (VES), transect VES, amphibian call survey, cover-board inspection, pitfall/drift fence check, opportunistic. Reptiles and amphibians can both be recorded during VES surveys. Calling surveys are restricted to Amphibians.
- **Mammal survey**: line transect, track/sign, spotlight, camera-trap checks, bat acoustic, live-trap checks, opportunistic.
- **Custom survey**: select ONE of these three groups and then set the relevant method, station, observer and bird point-count duration. No All Taxa mode.

The method setup screen records site/station identifiers and makes method-specific controls visible. Bird line transects require a magnetic line bearing. A point count uses a configurable timer.

## Survey screen
Only relevant taxon-group controls appear. A herpetofauna VES survey lets observers switch between **Reptiles** and **Amphibians**, with both groups stored in one linked survey event. A completed event returns to the welcome screen. Active sessions can be resumed through the welcome screen. The camera remains hidden unless explicitly opened.

## Research readiness
Method choices are drawn from published bird monitoring, USGS amphibian inventories and published mammal monitoring literature, but **basic log** options are NOT complete scientific protocols in the current Beta. Acoustic/camera/trap deployments need protocol-specific sensor, number-of-traps, duration, uptime and effort records. The basic-log flag is present in the survey export. Point counts, line transects and VES also require appropriate sampling designs, calibration and effort to support inferential analyses.

## Storage and installation
App ID `org.phwildlife.survey.beta` is unchanged. This build upgrades the offline database to schema version 3, adding profile, station ID, observer name and protocol status while retaining existing Beta records. Export from the old Beta before installing this update. GitHub's cached debug-signing key should permit updating without uninstalling the prior Beta.

## GitHub APK
See Actions → Build Android Debug APK → latest successful build → artifact `wildlife-survey-debug-apk`, extract `app-debug.apk`.

## References for method choices
- Cornell Lab of Ornithology Bird Monitoring Methods: https://www.birds.cornell.edu/landtrust/bird-monitoring-methods/
- US Forest Service multiple species inventory guide: https://www.fs.usda.gov/psw/publications/wo/wo_gtr073.pdf
- USGS amphibian calling monitoring: https://www.usgs.gov/centers/eesc/science/north-american-amphibian-monitoring-program
- USGS pitfall/drift-fence surveys: https://www.usgs.gov/publications/straight-line-drift-fences-and-pitfall-traps
- USGS NABat acoustic transect SOP: https://pubs.usgs.gov/publication/tm2C3/full

## Test priorities
- Launch → Welcome screen (not a survey).
- Start a bird point count and verify timer is configurable.
- Complete survey → returns to Welcome.
- Start herpetofauna VES → switch reptiles / amphibians in same session.
- Attempt amphibian calls → only amphibians appear.
- Start custom mammal sign survey → survey event captures mammal profile and method.
- Confirm existing Beta records persist, with correct data exports.
