#!/bin/bash

export BAMBOO_USER=$BAMBOO_TU_USERNAME
export BAMBOO_PASS=$BAMBOO_TU_PASSWORD
export PROJECT_NAME=OSM
export PLAN_NAME=DOD
export STAGE_NAME=JOB1

curl --user $BAMBOO_USER:$BAMBOO_PASS -X POST -d "$STAGE_NAME&ExecuteAllStages" https://bamboo.iteratec.io/rest/api/latest/queue/$PROJECT_NAME-$PLAN_NAME