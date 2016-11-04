#!/bin/bash
cd "`dirname $0`/../../"
./gradlew integrationTest --tests de.**.
firefox build/reports/tests/index.html
