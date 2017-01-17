Summary
=====================
OpenSpeedMonitor automates web performance measurements based on [WebPagetest][1]. It also processes, aggregates and visualizes the results. So it perfectly fits the need for a continuous monitoring of your most important pages.

_The timings you have to keep track of - always on expressive, well arranged dashboards. Without loosing all the details of the single WebPagetests if needed!_

You want to see it running? No problem, there is a demo up and running at [demo.openspeedmonitor.org][17].

![Einstieg OSM](https://github.com/IteraSpeed/OpenSpeedMonitor/wiki/images/osm_v4_entry.jpg)

OpenSpeedMonitor is published under [Apache License 2.0][12], so feel free to use, adapt and do whatever you want with the code. If you extend functionality we would appreciate contributions. Technically, OpenSpeedMonitor is based on [grails][3], a jvm based web framework leveraged by the programming language [groovy][2].

Usage
=====================
In the following explanations all references to gui elements of OpenSpeedMonitor are given in english, also gui is internationalized and comes with german as additional language.

Construct measurement environment
---------------------
OpenSpeedMonitor is great for automation of web performance measurements as well as for processing, analyzing and visualizing the measurement results. But it doesn't provide anything for actually running the performance measurements. Therefore it is based on [WebPagetest][1] which does the measurement job. So to run OpenSpeedMonitor we assume you have installed and set up a private instance of WebPagetest. This instance should contain at least one wpt server with configured locations and measuring agent(s) behind the locations. An installation guide for private instances of WebPagetest can be found [here][4].

OpenSpeedMonitor uses the [RESTful API][6] of WebPagetest for automation of performance measurements and getting the result's raw data. If you look at the [system overview][5] of webpagetest OpenSpeedMonitor uses Automation API to run scheduled tests and get back results from wpt server like other external tools.

First thing you have to do after installation is to configure the components of your private WebPagetest instance. All you need is a running WebPagetest server which is available via HTTP(S) from your OpenSpeedMonitor instance. After a log in you can reach the gui to create a new wpt server object via menu entry `Administration >> All Controllers >> WebPageTestServerController`. You have to set correct _Base Url_ of the wpt server in order to automate tests on that server. After creation of the server object you can use the _Fetch locations_ button in order to get locations configured on that wpt server into OpenSpeedMonitor. That function uses `[wpt server BaseUrl]/getLocations.php` api function of wpt server to request its locations.

For the following environment components objects get created in database of OpenSpeedMonitor (if they don't already exist) while start of OpenSpeedMonitor:

* Browsers
    * Internet Explorer
    * Firefox
    * Chrome
* Connectivity Profiles
    * DSL 6.000 (6.000 mbit/s bandwidth down, 512 kbit/s bandwidth up, 50 ms additional RTT, 0 packet loss rate)
    * UMTS (384 kbit/s bandwidth down, 384 kbit/s bandwidth up, 140 ms additional RTT, 0 packet loss rate)
    * UMTS - HSDPA (3.600 kbit/s bandwidth down, 1.500 kbit/s bandwidth up, 40 ms additional RTT, 0 packet loss rate)

Additional objects like further browsers and/or connectivity profiles can be created via web gui of OpenSpeedMonitor after start.

Set up measurements
---------------------
After wpt server(s), locations and browsers of your measurement environment are set up you can start to automate web performance measurements for your private WebPagetest instance. To run your first measurement automatically, you will need:

* A wpt script

    > Describes what the browser should do while test execution. That can be a sequence of different pages illustrating a whole use case of the web application under test. For each of the steps in the sequence a single result will be available after test execution. Single steps should be named via `setEventName` command.
    >
    > Scripts has to be written in WebPagetest DSL. See [scripting documentation][7] for details.
    >
    > Example measuring a single page:
    > ![Script of a single step](https://github.com/IteraSpeed/OpenSpeedMonitor/wiki/images/osm_v4_script_singleStep.png)
    >
    > Example surfing through an online shop to a product page:
    > ![Script of multiple steps](https://github.com/IteraSpeed/OpenSpeedMonitor/wiki/images/osm_v4_scriptMultistep.png)
    >
    > If you have any further questions on that feel free to [contact us][15]

* A measurement job

    > Within a job you configure when to run the measurement and on which wpt location and browser to run it. Also you can set all the advanced settings you can set for an ad hoc measurement via web gui of wpt server (like internet connectivity, whether or not to create a video, whether or not to create a tcp dump of the measurement, et cetera).

Views for managing scripts and jobs are available in _Measurement_ section of OpenSpeedMonitor gui. The list views can be filtered. Filters get persisted in local storage of your browser so you don't loose your settings while navigating through application.

After each start of OpenSpeedMonitor application, measurements are disabled generally. So even if you set up measurements as described in previous section none of your measurement jobs will start any measurements at all. You will have to activate measurements once, initially on job list view (requires login with admin rights).

Visualize results
---------------------

**Raw test result data**

Trends in time of wpt results are available in _Measurement results_ section.

![resultchart](https://github.com/IteraSpeed/OpenSpeedMonitor/wiki/images/osm_v4_resultchart.jpg)

Measurement data to show in diagrams can be chosen by three categories which are located in three different areas within the gui.

* _Aggregation and timeframe_

    One can select the timeframe to be shown. Averages per timeframe can be build if necessary.

* _Filter jobs_

    One can filter the results of which jobs to show. Measurement jobs can be associated to groups. Webpage types (like _homepage_ or _product page_ of online shops) can be configured and associated to measured steps. Browser and/or location tests run on can be used as filter criteria.

* _Choose measured variables_

    All the data WebPagetest provides on page level (first and repeated view) can be shown in diagrams of OpenSpeedMonitor. The measurands are categorized as follows for convenient selection and for association to different y-axes of diagram:

    * Load times
        * load time
        * time to first byte
        * start render time
        * doc complete time
        * visually complete time
        * dom time
        * fully loaded time
    * Count of requests
        * to doc complete
        * to fully loaded
    * Size of requests
        * until doc complete
        * until fully loaded
    * Percentages
        * customer satisfaction for doc complete
        * customer satisfaction for visually complete
    * Others
        * speed index

Single data points are clickable and lead to underlying WebPagetest result with all its details.

**Results aggregated by measured pages**

All the raw data measured with WebPagetest can be aggregated by page. These aggregations get visualized as barcharts on a dashboard.

![resultchart](https://github.com/IteraSpeed/OpenSpeedMonitor/wiki/images/osm_v4_page_aggregation.jpg)

Detail Analysis
---------------------

[OsmDetailAnalysis][18] is a microservice that provides functionality to persist and analyze web performance waterfall data. It can be integrated with OpenSpeedMonitor in that it collects and stores the detailed waterfall data of all the measured pages within OpenSpeedMonitor jobs. The timings and sizes of all the single waterfall resources can be filtered and analyzed interactively in a dashboard based on [dc.js][19].

![OsmDetailAnalysis](https://github.com/IteraSpeed/OpenSpeedMonitor/wiki/images/osm_v4_detailanalysis.jpg)

These sort of analysis can be of great benefit detecting thirdparty libraries that slow down your site!

OsmDetailAnalysis is open source and licensed under same licence as OpenSpeedMonitor.

Customer satisfaction index (CSI)
---------------------

Usually different pages of your application are of greatest interest regarding performance. To compare these different pages raw pageload times aren't always best measurand quantity. Your users may expect and accept different load times for different types of pages. For example, 2.8 secs may be acceptable for the basket of your online shop, while it may absolutely not be acceptable for the homepage.

To take that fact into account OpenSpeedMonitor can translate load times of every test result into a percentage of customers who would be satisfied with that load time. OpenSpeedMonitor comes with five predefined mappings.

![CSI mappings](https://github.com/IteraSpeed/OpenSpeedMonitor/wiki/images/osm_v4_csiMappings.png)

To translate load times into percentages of satisfaction one of these mappings can be applied to all your measured page types.

Further, OpenSpeedMonitor provides possibility to calculate aggregated customer satisfaction indices for all the pages of your application and for the whole application altogether. Results factored in the calculation of the pages csi get weighted by browser, connectivity and hour of day regarding their importance for your application. Results factored in the calculation of applications csi get weighted by pages regarding their importance for your application.

![page weights](https://github.com/IteraSpeed/OpenSpeedMonitor/wiki/images/osm_v4_pageweights.png)

The CSI is a KPI very suitable for getting a quick overview of the overall performance of your whole application. It can be used to setup a competition benchmark in your branch of business.

![csi competition benchmark](https://github.com/IteraSpeed/OpenSpeedMonitor/wiki/images/osm_v4_csi_chart.jpg)

Installation
=====================

Prerequisites
---------------------
* Java runtime environment (JRE), version 8.

* [optional] Dedicated servlet container.

    Mainly tested with Apache Tomcat. Should run on most servlet containers. See [full list of supported Java EE containers][11] in grails documentation for further details.

* [optional, but recommended] Relational data base managing system (DBMS).

    Tested with MySQL. Should run with Oracle out of the box. OpenSpeedMonitor uses regular expressions via `rlike` command in grails object relational mapper gorm. `rlike` is supported for MySQL and Oracle. Other DBMS supporting regular expressions in sql queries would necessitate slight modifications to run performant.

* Operating system (OS) to run OpenSpeedMonitor can be any server OS for which JRE 8 and the servlet container and dbms of choice are available.

    Mainly tested on [debian][13].

* Alternatively to run OpenSpeedMonitor directly on a server one can use docker-compose file in _docker/_ folder to run OpenSpeedMonitor in docker containers. All you need is the docker daemon on the target host in order to run OpenSpeedMonitor in docker.  All the other prerequisites defined in this section aren't necessary because these already exist inside the containers.

Installation OpenSpeedMonitor
---------------------
As a [grails][3] application OpenSpeedMonitor is jvm and servlet API based. So its runtime environment is a servlet container. The deployment artefact of servlet API based web applications is a war file (web application archive).

One way to install OpenSpeedMonitor is to create a war file through running _gradle assemble_.

From project root on Linux/MAC:

    ./gradlew assemble

or on Windows:

    ./gradlew.bat assemble

Created war file can be deployed to servlet container of choice.
See [grails build documentation][10] for details.

Since version 3 Grails is based on Spring Boot and so can be run easily **in embedded tomcat container**.

From project root on Linux/MAC:

    ./gradlew prod bootRun

or on Windows:

    ./gradlew.bat prod bootRun

Created war file can be deployed to servlet container of choice.
See [grails build documentation][10] for details.

Like described in previous prerequisites section OpenSpeedMonitor can be deployed and run in docker containers. One can use docker-compose file _docker/docker-compose.yml_  to start multiple containers via this simple call:

    cd docker
    docker-compose up

Configuration
=====================
Grails framework implements convention over configuration pattern. So a lot of configuration settings are preset with sensible defaults and you won't have to touch them ever. Default configuration files are located in `./grails-app/conf/`.

Configuration settings you have to change from defaults should be located in an external configuration file. A sample for such an external config file can be found in `./grails-app/config/OpenSpeedMonitor-config.yml.sample`. A copy of that file can be located in

    ~/.grails/OpenSpeedMonitor-config.yml

Settings in that external config file will override defaults configured in `./grails-app/conf/` on startup.

In develop mode there are two users created for testing purposes:

* admin with password admin
* root with password root

If you're going to run OpenSpeedMonitor in prod-mode, you have to declare your admin and root in the external configuration file.

License
=====================
Code of OpenSpeedMonitor is licensed under [Apache License 2.0][12].

Development
=====================
To contribute to the code base or develop your own features you should be familiar with [grails][3]. Groovy and Javascript skills would be an asset, too.

OpenSpeedMonitor is a monolithic web application. Respective grails plugin structure several external plugins are integrated. Before publishing under [Apache License][12] we developed the tool under version control in an own git server. So older history can't be found on github.

As IDE you can use [Groovy/Grails Tool Suite][16] from SpringSource, which is an eclipse bundle optimized for groovy and grails development. IntelliJIdea has excellent groovy/grails support within its ultimate edition.

Although it works with an H2 in memory database out of the box it's highly recommended to develop and test with a relational database server like MySQL.

[1]: http://webpagetest.org/    "WebPagetest"
[2]: http://groovy.codehaus.org/    "groovy programming language"
[3]: http://grails.org/ "grails"
[4]: https://sites.google.com/a/webpagetest.org/docs/private-instances "WebPagetest's installation guide for private instances"
[5]: https://sites.google.com/a/webpagetest.org/docs/system-design/overview "system overview WebPagetest"
[6]: https://sites.google.com/a/webpagetest.org/docs/advanced-features/webpagetest-restful-apis "rest api WebPagetest"
[7]: https://sites.google.com/a/webpagetest.org/docs/using-webpagetest/scripting "Scripting DSL WebPagetest"
[8]: http://grails.github.io/grails-doc/2.5.0/ref/Command%20Line/run-app.html "grails documentation: run-app"
[9]: http://grails.github.io/grails-doc/2.5.0/ref/Command%20Line/run-war.html "grails documentation: run-war"
[10]: http://docs.grails.org/3.1.5/guide/commandLine.html#gradleBuild "grails buld documentation"
[11]: http://grails.org/doc/2.5.0/guide/gettingStarted.html#supportedJavaEEContainers "grails: supported servlet containers"
[12]: http://www.apache.org/licenses/LICENSE-2.0 "Apache License 2.0"
[13]: https://www.debian.org/ "debian"
[14]: https://github.com/WPO-Foundation/webpagetest/pull/151 "pull request multistep"
[15]: mailto:wpt@iteratec.de "contact us"
[16]: http://spring.io/tools/ggts "GGTS"
[17]: http://demo.openspeedmonitor.org "demo.openspeedmonitor.org"
[18]: https://github.com/iteratec/OsmDetailAnalysis
[19]: https://dc-js.github.io/dc.js/
