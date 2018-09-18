#!/bin/bash

# prepare osm config respective set env variables
OSM_CONFIG_TARGET_LOCATION="${OSM_CONFIG_HOME}/OpenSpeedMonitor-config.yml"

echo "complete osm config file gets prepared"
dockerize -template $OSM_CONFIG_TARGET_LOCATION.j2:$OSM_CONFIG_TARGET_LOCATION

# start tomcat
java -Dgrails.env=prod -Dosm_config_location=${OSM_CONFIG_TARGET_LOCATION} -jar $OSM_HOME/OpenSpeedMonitor*.war
