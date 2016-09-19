#!/bin/bash

set -e

########################################################################################
PORT_TO_SWITCH_FROM=$bamboo_tomcat_PORT_TO_UNDEPLOY
PORT_TO_SWITCH_TO=$bamboo_tomcat_PORT_TO_DEPLOY
########################################################################################

# echo variables of this job
echo "PORT_TO_SWITCH_FROM=$PORT_TO_SWITCH_FROM"
echo "PORT_TO_SWITCH_TO=$PORT_TO_SWITCH_TO"
# echo variables of previous (deploy) job
echo "PORT_TO_DEPLOY=$bamboo_tomcat_PORT_TO_DEPLOY"
echo "PORT_TO_UNDEPLOY=$bamboo_tomcat_PORT_TO_UNDEPLOY"
stdout_file="osm_${bamboo_tomcat_HOST_TO_REVERSE_PROXY}_${PORT_TO_SWITCH_TO}_blue-green-switch-stdout.txt"

# run blue/green switch on target machine

ssh -p $bamboo_tomcat_SSH_PORT_TO_REVERSE_PROXY -o "StrictHostKeyChecking no" $bamboo_tomcat_JENKINS_USER_NAME@$bamboo_tomcat_HOST_TO_REVERSE_PROXY \
	"sudo /opt/osm_blue_green_switch.sh $PORT_TO_SWITCH_FROM $PORT_TO_SWITCH_TO $bamboo_tomcat_OSM_REVERSE_PROXY_URL $bamboo_tomcat_OSM_REVERSE_PROXY_IP OpenSpeedMonitor 2> /tmp/${stdout_file}" \
   && scp -P $bamboo_tomcat_SSH_PORT_TO_REVERSE_PROXY -o "StrictHostKeyChecking no" $bamboo_tomcat_JENKINS_USER_NAME@$bamboo_tomcat_HOST_TO_REVERSE_PROXY:"/tmp/$stdout_file" .

echo "stdout of blue-green-switch remote execution:"
cat ./$stdout_file
echo "END of stdout of blue-green-switch remote execution"

# let this script fail if osm_blue_green_switch.sh failed to run
cat ./$stdout_file | egrep ".*"
if [ $? -eq 0 ]; then
  exit 1
fi

# undeploy old version
if [ $PORT_TO_SWITCH_FROM -ne -1 ];
	then
		curl -u $bamboo_tomcat_TOMCAT_ADMIN_USERNAME:$bamboo_tomcat_TOMCAT_ADMIN_PASSWORD http://$bamboo_tomcat_HOST_TO_DEPLOY_TO:$PORT_TO_SWITCH_FROM/manager/text/undeploy\?path\=/OpenSpeedMonitor
fi