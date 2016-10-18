#!/bin/bash

echo "ping $bamboo_tomcat_HOST_TO_DEPLOY_TO"
ping -c 3 $bamboo_tomcat_HOST_TO_DEPLOY_TO
mv *.war ROOT.war
echo "deploy osm war to:"
echo "  http://$bamboo_tomcat_HOST_TO_DEPLOY_TO:$bamboo_tomcat_PORT_TO_DEPLOY/manager/text/deploy\?update\=true\&path\=/"
echo "  war file:"
ls -la ./*.war
curl -T ./*.war -u $bamboo_tomcat_TOMCAT_ADMIN_USERNAME:$bamboo_tomcat_TOMCAT_ADMIN_PASSWORD \
	http://$bamboo_tomcat_HOST_TO_DEPLOY_TO:$bamboo_tomcat_PORT_TO_DEPLOY/manager/text/deploy\?update\=true\&path\=/ \
	> ./deploy_osm_war_output.txt

curl_output=$(cat ./deploy_osm_war_output.txt)
echo "$curl_output"
if [[ $curl_output == *"Failed to deploy application"* ]]
then
  exit 1;
else
  exit 0;
fi