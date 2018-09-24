FROM java:openjdk-8

ENV OSM_HOME /osm
ENV OSM_CONFIG_HOME /osm/config
ENV JAVA_OPTS "-server -Dgrails.env=prod -Dfile.encoding=UTF-8"
ENV DOCKERIZE_VERSION v0.3.0

# add osm-user
RUN groupadd -r osm && useradd -ms /bin/bash -r -g osm osm

# install dockerize
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

# get osm-sources and build war-file
RUN mkdir -p $OSM_HOME $OSM_HOME/logs $OSM_CONFIG_HOME && \
    chmod -R 775 $OSM_HOME $OSM_CONFIG_HOME && \
    chown -R osm:100 $OSM_HOME $OSM_CONFIG_HOME

WORKDIR $OSM_HOME
ADD ./build/libs/OpenSpeedMonitor*.war $OSM_HOME/

# add osm config file
ADD docker/templates/osm-config.yml.j2 $OSM_CONFIG_HOME/OpenSpeedMonitor-config.yml.j2

# add entrypoint script
ADD docker/entrypoint.sh /entrypoint.sh
RUN chmod 775 /entrypoint.sh

USER osm
EXPOSE 8080
ENTRYPOINT /entrypoint.sh
