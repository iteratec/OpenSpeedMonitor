#!/bin/bash
#################
# configure_deployment_*.sh-script must be run in front of this script
# else variables will be missing
#################

# make external config file available as env variable
if [ -e "./credentials/$bamboo_tomcat_EXTERNAL_OSM_CONFIG_FILE" ]
  then export bamboo_tomcat_EXTERNAL_OSM_CONFIG_FILE=$bamboo_tomcat_EXTERNAL_OSM_CONFIG_FILE
  else
    echo "External config file $bamboo_tomcat_EXTERNAL_OSM_CONFIG_FILE does not exist in osm_credentials repository!"
    exit 1;
fi

# detect port to undeploy (tomcat with actual osm version) and to deploy (tomcat without osm)
ports=()
if(curl -u "${bamboo_tomcat_TOMCAT_ADMIN_USERNAME}:${bamboo_tomcat_TOMCAT_ADMIN_PASSWORD}" "http://${bamboo_tomcat_HOST_TO_DEPLOY_TO}:${bamboo_tomcat_TOMCAT_PORT1}/manager/text/list" | grep OpenSpeedMonitor);
    then ports[${#ports[*]}]=$bamboo_tomcat_TOMCAT_PORT1
fi
if(curl -u "${bamboo_tomcat_TOMCAT_ADMIN_USERNAME}:${bamboo_tomcat_TOMCAT_ADMIN_PASSWORD}" "http://${bamboo_tomcat_HOST_TO_DEPLOY_TO}:${bamboo_tomcat_TOMCAT_PORT2}/manager/text/list" | grep OpenSpeedMonitor);
    then ports[${#ports[*]}]=$bamboo_tomcat_TOMCAT_PORT2
fi
if [ ${#ports[*]} -eq 0 ];
    then
      export PORT_TO_DEPLOY=$bamboo_tomcat_TOMCAT_PORT1
      export TOMCAT_SERVER_PORT_TO_WAIT_FOR=$bamboo_tomcat_TOMCAT_SERVER_PORT1
      export TOMCAT_AJP_PORT_TO_WAIT_FOR=$bamboo_tomcat_TOMCAT_AJP_PORT1
      export TOMCAT_TLS_PORT_TO_WAIT_FOR=$bamboo_tomcat_TOMCAT_TLS_PORT1
      export PORT_TO_UNDEPLOY=-1
  elif [ ${#ports[*]} -eq 1 ];
    then
      export PORT_TO_UNDEPLOY=${ports[0]}
      ((PORT_TO_DEPLOY=PORT_TO_UNDEPLOY==$bamboo_tomcat_TOMCAT_PORT1 ? $bamboo_tomcat_TOMCAT_PORT2 : $bamboo_tomcat_TOMCAT_PORT1 ))
      ((TOMCAT_SERVER_PORT_TO_WAIT_FOR=PORT_TO_UNDEPLOY==$bamboo_tomcat_TOMCAT_PORT1 ? $TOMCAT_SERVER_PORT2 : $bamboo_tomcat_TOMCAT_SERVER_PORT1 ))
      ((TOMCAT_AJP_PORT_TO_WAIT_FOR=PORT_TO_UNDEPLOY==$bamboo_tomcat_TOMCAT_PORT1 ? $TOMCAT_AJP_PORT2 : $bamboo_tomcat_TOMCAT_AJP_PORT1 ))
      ((TOMCAT_TLS_PORT_TO_WAIT_FOR=PORT_TO_UNDEPLOY==$bamboo_tomcat_TOMCAT_PORT1 ? $TOMCAT_TLS_PORT2 : $bamboo_tomcat_TOMCAT_TLS_PORT1 ))
      export PORT_TO_DEPLOY
      export TOMCAT_SERVER_PORT_TO_WAIT_FOR
      export TOMCAT_AJP_PORT_TO_WAIT_FOR
      export TOMCAT_TLS_PORT_TO_WAIT_FOR
  else
    echo "OpenSpeedMonitor is deployed on both tomcats"
    exit 1;
fi
