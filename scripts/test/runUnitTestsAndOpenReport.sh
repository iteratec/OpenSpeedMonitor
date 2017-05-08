#!/bin/bash
cd "`dirname $0`/../../"
./gradlew -x jasmineRun test
xdg-open build/spock-reports/index.html
