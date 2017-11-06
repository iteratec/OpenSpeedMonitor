#!/bin/bash
cd "`dirname $0`/../../"
rm -rf build/spock-reports/**
./gradlew -x jasmineRun test
xdg-open build/spock-reports/index.html
