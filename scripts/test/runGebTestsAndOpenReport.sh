#!/bin/bash
cd "`dirname $0`/../../"
./gradlew integrationTest --tests geb.**.*
firefox build/reports/tests/index.html
