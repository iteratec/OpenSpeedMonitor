#!/bin/bash
cd "`dirname $0`/../../"
./gradlew jasmineRun
xdg-open build/reports/tests-jasmine/units.html
