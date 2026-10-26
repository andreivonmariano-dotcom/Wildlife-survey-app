# Philippine Wildlife Survey (Android prototype)
Standalone offline Android field application. No SMART, KALUMBATA or Bluetooth rangefinder integration.

The **v0.3 alpha** supports: live back-camera aiming and magnetic azimuth/inclination from phone rotation sensors, optional bird-bearing capture, point or straight-line transect survey events, species/count/manual horizontal-distance entry, GPS fixes when permission and reception permit, offline SQLite records, and export of `surveys.csv` and `observations.csv` together in one ZIP. No scientific taxonomic checklist is bundled yet.

## APK
Go to GitHub Actions → **Build Android Debug APK** → successful run → download the **wildlife-survey-debug-apk** artifact and extract `app-debug.apk` from that artifact ZIP.

## Limitations
Experimental debug build only. Phone orientation and camera axis need calibration under real field conditions. Sensor azimuth is magnetic, **not true north**; do not mix it with true-north transect bearings. The first build does not verify detections or produce publication-ready distance-sampling analyses. Nondetection survey events are retained. Line calculations assume a straight segment and a manually provided magnetic survey bearing. GPS can be absent or stale until a new fix is obtained. Camera preview uses Android's legacy camera API for a dependency-free prototype, which may have compatibility limitations on some Android devices. No photograph is saved unless a future feature adds this.
