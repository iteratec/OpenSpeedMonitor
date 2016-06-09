#!/bin/bash
cd "`dirname $0`/../../"
./gradlew integrationTest
firefox build/reports/tests/index.html  
