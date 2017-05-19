#!/bin/bash
cd "`dirname $0`/../../"
./gradlew --stacktrace integrationTest --tests geb.**.*
xdg-open build/spock-reports/index.html
