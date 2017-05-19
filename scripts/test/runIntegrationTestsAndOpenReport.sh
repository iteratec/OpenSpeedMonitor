#!/bin/bash
cd "`dirname $0`/../../"
./gradlew integrationTest --tests de.**.
xdg-open build/spock-reports/index.html
