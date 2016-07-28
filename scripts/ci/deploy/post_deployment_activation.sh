#!/bin/bash
# activate measurements and nightly cleanup job on new osm instance
if [ -n "$bamboo_tomcat_OSM_API_KEY" ];
then
    echo "activate measurements via rest api"
	curl -X PUT --data "apiKey=$bamboo_tomcat_OSM_API_KEY" $bamboo_tomcat_OSM_PROTOCOL$bamboo_tomcat_OSM_REVERSE_PROXY_URL/rest/config/activateMeasurementsGenerally
	echo "activate nightly cleanup via rest api"
    curl -X PUT --data "apiKey=$bamboo_tomcat_OSM_API_KEY" $bamboo_tomcat_OSM_PROTOCOL$bamboo_tomcat_OSM_REVERSE_PROXY_URL/rest/config/activateNightlyDatabaseCleanup
    echo "create event for deployment via rest api"
    curl -X POST -H "Cache-Control: no-cache" -H "Content-Type: application/x-www-form-urlencoded" -d "apiKey=$bamboo_tomcat_OSM_API_KEY&shortName=Deployment OSM&description=Deployment OSM version $bamboo_deploy_version&globallyVisible=true&system=undefined" "$bamboo_tomcat_OSM_PROTOCOL$bamboo_tomcat_OSM_REVERSE_PROXY_URL/rest/event/create"
    echo "start handling of pending job results via rest api"
    curl -X PUT --data "apiKey=$bamboo_tomcat_OSM_API_KEY" $bamboo_tomcat_OSM_PROTOCOL$bamboo_tomcat_OSM_REVERSE_PROXY_URL/rest/handleOldJobResults
else
  echo "No OSM_API_KEY configured so measurements and nightly cleanup can't be activated via rest calls!"
fi