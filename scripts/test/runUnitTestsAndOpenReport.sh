#!/bin/bash
cd "`dirname $0`/../../"
./gradlew -x jasmineRun test
firefox build/reports/tests/index.html
