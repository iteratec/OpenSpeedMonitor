# OpenSpeedmonitor on Docker
Of course you can use the OSM as a docker-container. By this you don't have to care about any java-version or grails-version or building your war-file.

Grails supports an [embedded Tomcat](http://docs.grails.org/latest/guide/deployment.html) which is used in this docker-image. This container listens on port 8080, so you have to set docker-port-forwarding if you want to use another port on your host.

## Configure OSM-container
You can pass some environment-variables to your osm-container for configuring it:
* OSM_URL: full URL under which your OSM-instance will be reachable, f.e.: ``` OSM_URL="http://my-osm-instance.com" ```, default: ``` http://localhost:8080 ```
* OSM_ADMIN_USER: name of the admin-user which will be bootstrapped, f.e.: ``` OSM_ADMIN_USER="admin" ```, default: ``` admin ```
* OSM_ADMIN_PASSWORD: password of the admin-user which will be bootstrapped, f.e.: ``` OSM_ADMIN_PASSWORD="secret123" ```, default: ``` admin123 ```
* OSM_ROOT_USER: name of the root-user which will be bootstrapped, f.e.: ``` OSM_ROOT_USER="root" ```, default: ``` root ```
* OSM_ROOT_PASSWORD: password of the root-user which will be bootstrapped, f.e.: ``` OSM_ROOT_USER="muchMoreSecret!123" ```, default: ``` muchMoreSecret!123 ```
* MYSQL_HOST: hostname of your mysql-instance, f.e.: ``` MYSQL_HOST="localhost" ``` or ``` MYSQL_HOST="web.com:10000" ```, default: ``` osm-mysql ```
* MYSQL_DATABASE: name of database on mysql-host, f.e.: ``` MYSQL_DATABASE="osm-prod" ```, default: ``` osm ```
* MYSQL_USER: user-name for accessing mysql-database, f.e.: ``` MYSQL_USER="osm-user" ```, default: ``` osm ```
* MYSQL_PASSWORD: user-password for mysql-user, f.e.: ``` MYSQL_PASSWORD="DatSecret456" ```, default: ``` osm123 ```

## Run your own dockerized OSM
In this directory is a docker-compose-file for you. This file will ensure there is a mysql-db running which is linked to the OSM-container.

For testing purposes just start this OSM-environment by typing ``` docker-compose up ```.

If you want a long-term OSM-environment please adopt the given docker-compose-file to your infrastructure. Of course you can use this docker-compose-file to boot up your OSM, but you should (seriously) modify the given passwords for security-reasons.

After the bootup, you should reach your OSM-instance under this URL: ```http://localhost:8080```
