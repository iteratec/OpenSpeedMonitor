#!/bin/bash

########################################################################################
PORT_TO_SWITCH_FROM=$bamboo_tomcat_PORT_TO_UNDEPLOY
PORT_TO_SWITCH_TO=$bamboo_tomcat_PORT_TO_DEPLOY
########################################################################################

echo "echoing variables of this job"
echo "#######################################"
echo "PORT_TO_SWITCH_FROM=$PORT_TO_SWITCH_FROM"
echo "PORT_TO_SWITCH_TO=$PORT_TO_SWITCH_TO"
# echo variables of previous (deploy) job
echo "PORT_TO_DEPLOY=$bamboo_tomcat_PORT_TO_DEPLOY"
echo "PORT_TO_UNDEPLOY=$bamboo_tomcat_PORT_TO_UNDEPLOY"
stderr_file="osm_${bamboo_tomcat_HOST_TO_REVERSE_PROXY}_${PORT_TO_SWITCH_TO}_blue-green-switch-stderr.txt"
echo "stderr_file=${stderr_file}"

echo "run blue/green switch on target machine"
echo "#######################################"
ssh -p $bamboo_tomcat_SSH_PORT_TO_REVERSE_PROXY -o "StrictHostKeyChecking no" $bamboo_tomcat_JENKINS_USER_NAME@$bamboo_tomcat_HOST_TO_REVERSE_PROXY \
	"sudo /opt/osm_blue_green_switch.sh $PORT_TO_SWITCH_FROM $PORT_TO_SWITCH_TO $bamboo_tomcat_OSM_REVERSE_PROXY_URL $bamboo_tomcat_OSM_REVERSE_PROXY_IP OpenSpeedMonitor 2> /tmp/${stderr_file}" \
   && scp -P $bamboo_tomcat_SSH_PORT_TO_REVERSE_PROXY -o "StrictHostKeyChecking no" $bamboo_tomcat_JENKINS_USER_NAME@$bamboo_tomcat_HOST_TO_REVERSE_PROXY:"/tmp/$stderr_file" .

echo "stderr of blue-green-switch remote execution:"
cat ./$stderr_file
echo "END of stderr of blue-green-switch remote execution"
cat ./$stderr_file | egrep ".*"
exit_code_greping_stderr=$?
echo "exit_code_greping_stderr=${exit_code_greping_stderr}"
if [ $exit_code_greping_stderr -eq 0 ]; then
    exit 1
fi

echo "undeploy old version"
echo "#######################################"
if [ $PORT_TO_SWITCH_FROM -ne -1 ]; then
    curl -u $bamboo_tomcat_TOMCAT_ADMIN_USERNAME:$bamboo_tomcat_TOMCAT_ADMIN_PASSWORD http://$bamboo_tomcat_HOST_TO_DEPLOY_TO:$PORT_TO_SWITCH_FROM/manager/text/undeploy\?path\=
fi
exit_code_undeploying_old_osm=$?
echo "exit_code_undeploying_old_osm=${exit_code_undeploying_old_osm}"
if [ $exit_code_undeploying_old_osm -ne 0 ]; then
    exit 1
fi

echo "success if previous steps didn't fail"
echo "#######################################"
exit 0