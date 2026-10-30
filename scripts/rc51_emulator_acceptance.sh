#!/usr/bin/env bash
set -euo pipefail
bash scripts/rc50_emulator_acceptance.sh
# The prior rollback test deliberately populates the disposable emulator.
# Clear ONLY this CI emulator workspace before the research export fixture.
adb shell pm clear org.phwildlife.survey.beta08 > emulator-qa/rc51-reset-disposable.txt
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc51-zip-instrumentation.txt 2>&1 || {
    tail -n 130 emulator-qa/rc51-zip-instrumentation.txt
    exit 1
  }
echo 'RC51 actual research ZIP fixture: PASS' | tee emulator-qa/rc51-zip-summary.txt
