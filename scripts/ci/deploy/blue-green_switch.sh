#!/bin/bash

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


# run blue/green switch on target machine
ssh -p $bamboo_tomcat_SSH_PORT_TO_HOST_TO_DEPLOY_TO -o "StrictHostKeyChecking no" $bamboo_tomcat_TOMCAT_ADMIN_USERNAME@$bamboo_tomcat_HOST_TO_DEPLOY_TO \
	"sudo /opt/osm_blue_green_switch.sh $PORT_TO_SWITCH_FROM $PORT_TO_SWITCH_TO $bamboo_tomcat_OSM_REVERSE_PROXY_URL $bamboo_tomcat_OSM_REVERSE_PROXY_IP OpenSpeedMonitor 2>&1" \
    > .\combined_output_on_local_host.txt
cat .\combined_output_on_local_host.txt

# undeploy old version
curl -u $bamboo_tomcat_TOMCAT_ADMIN_USERNAME:$bamboo_tomcat_TOMCAT_ADMIN_PASSWORD http://$bamboo_tomcat_HOST_TO_DEPLOY_TO:$PORT_TO_SWITCH_FROM/manager/text/undeploy\?path\=/OpenSpeedMonitor
