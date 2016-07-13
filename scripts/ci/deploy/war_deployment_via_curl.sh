#!/bin/bash

echo "ping $bamboo_tomcat_HOST_TO_DEPLOY_TO"
ping -c 3 $bamboo_tomcat_HOST_TO_DEPLOY_TO

echo "deploy osm war to: http://$bamboo_tomcat_HOST_TO_DEPLOY_TO:$bamboo_tomcat_PORT_TO_DEPLOY/manager/text/deploy\?update\=true\&path\=/OpenSpeedMonitor"
curl -T ./osm_war_to_deploy.war -u $bamboo_tomcat_TOMCAT_ADMIN_USERNAME:$bamboo_tomcat_TOMCAT_ADMIN_PASSWORD \
	http://$bamboo_tomcat_HOST_TO_DEPLOY_TO:$bamboo_tomcat_PORT_TO_DEPLOY/manager/text/deploy\?update\=true\&path\=/OpenSpeedMonitor \
	> ./deploy_osm_war_output.txt

curl_output=$(cat ./deploy_osm_war_output.txt)
cat curl_output
if [[ $curl_output == *"Failed to deploy application"* ]]
then
  exit 1;
else
  exit 0;
fi