#!/bin/bash
cd "`dirname $0`/../../"
./gradlew jasmineRun
firefox build/reports/tests-jasmine/units.html
