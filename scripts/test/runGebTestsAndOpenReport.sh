#!/bin/bash
cd "`dirname $0`/../../"
rm -rf build/spock-reports/**
./gradlew --stacktrace integrationTest --tests geb.**.*
xdg-open build/spock-reports/index.html
