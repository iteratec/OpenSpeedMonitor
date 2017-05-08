#!/bin/bash
cd "`dirname $0`/../../"
./gradlew integrationTest --tests geb.**.*
xdg-open build/spock-reports/index.html
