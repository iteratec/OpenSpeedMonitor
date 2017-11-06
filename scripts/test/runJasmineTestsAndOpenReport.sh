#!/bin/bash
cd "`dirname $0`/../../"
rm -rf build/spock-reports/**
./gradlew jasmineRun
xdg-open build/reports/tests-jasmine/units.html
