#!/bin/bash
cd "`dirname $0`/../../"
rm -rf build/spock-reports/**
./node_modules/karma/bin/karma start ./src/test/js/karma.conf.js
