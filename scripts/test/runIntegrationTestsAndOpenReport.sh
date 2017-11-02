#!/bin/bash
cd "`dirname $0`/../../"
rm -rf build/spock-reports/**
./gradlew integrationTest --tests de.**.
xdg-open build/spock-reports/index.html
