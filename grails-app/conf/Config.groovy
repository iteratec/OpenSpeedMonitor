import de.iteratec.osm.report.chart.ChartingLibrary

import org.apache.log4j.AsyncAppender
import org.apache.log4j.DailyRollingFileAppender
import org.apache.log4j.RollingFileAppender

/*
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* 	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


def appNameForLog4jConfig = appName

grails.databinding.dateFormats = [
        'dd.MM.yyyy', 'yyyy-MM-dd', 'yyyy/MM/dd', 'MMddyyyy', 'yyyy-MM-dd HH:mm:ss.S', 'yyyy-MM-dd HH:mm:ss', "yyyy-MM-dd'T'hh:mm:ss'Z'"]

/*
 * locations to search for config files that get merged into the main config:
 * 	config files can be ConfigSlurper scripts, Java properties files, or classes
 * 	in the classpath in ConfigSlurper format
 */
if (System.properties["osm_config_location"]) {
    log.info('sytem property for external configuration found')
    grails.config.locations = ["file:" + System.properties["osm_config_location"]]
} else {
    grails.config.locations = [
            "classpath:${appName}-config.properties",
            "classpath:${appName}-config.groovy",
            "file:${userHome}/.grails/${appName}-config.properties",
            "file:${userHome}/.grails/${appName}-config.groovy"]
}

// config for all environments //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
        all          : '*/*',
        atom         : 'application/atom+xml',
        css          : 'text/css',
        csv          : 'text/csv',
        form         : 'application/x-www-form-urlencoded',
        html         : ['text/html', 'application/xhtml+xml'],
        js           : 'text/javascript',
        json         : ['application/json', 'text/json'],
        multipartForm: 'multipart/form-data',
        rss          : 'application/rss+xml',
        text         : 'text/plain',
        xml          : ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
//"ISO-8859-1"
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "ISO-8859-1"
grails.converters.default.pretty.print = true

// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

beans {
    cacheManager {
        shared = true
    }
}
// so Tag and TagLink can be referenced in HQL queries. See http://grails.org/plugin/taggable
grails.taggable.tag.autoImport = true
grails.taggable.tagLink.autoImport = true

def logDirectory = '.'

grails.config.defaults.locations = [KickstartResources]

grails.plugin.springsecurity.password.algorithm = 'SHA-512'
grails.plugin.springsecurity.password.hash.iterations = 1
grails.plugin.springsecurity.userLookup.userDomainClassName = 'de.iteratec.osm.security.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'de.iteratec.osm.security.UserRole'
grails.plugin.springsecurity.authority.className = 'de.iteratec.osm.security.Role'
grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"

grails.plugin.springsecurity.interceptUrlMap = [
//////////////////////////////////////////////////////////////////
//free for all (even guests not logged in)
//////////////////////////////////////////////////////////////////
'/static/**'                                        : ["permitAll"],
'/static/*'                                         : ["permitAll"],
'/css/**'                                           : ["permitAll"],
'/js/**'                                            : ["permitAll"],
'/images/**'                                        : ["permitAll"],
'/less/**'                                          : ["permitAll"],
'/'                                                 : ["permitAll"],
'/proxy/**'                                         : ["permitAll"],
'/wptProxy/**'                                      : ["permitAll"],
'/csiDashboard/index'                               : ["permitAll"],
'/csiDashboard/showAll'                             : ["permitAll"],
'/csiDashboard/csiValuesCsv'                        : ["permitAll"],
'/csiDashboard/showDefault'                         : ["permitAll"],
'/csiDashboard/weights'                             : ["permitAll"],
'/csiConfigIO/downloadBrowserWeights'               : ["permitAll"],
'/csiConfigIO/downloadPageWeights'                  : ["permitAll"],
'/csiConfigIO/downloadHourOfDayWeights'             : ["permitAll"],
'/csiConfigIO/downloadBrowserConnectivityWeights'   : ["permitAll"],
'/csiConfigIO/downloadDefaultTimeToCsMappings'      : ["permitAll"],
'/eventResultDashboard/**'                          : ["permitAll"],
'/tabularResultPresentation/**'                     : ["permitAll"],
'/highchartPointDetails/**'                         : ["permitAll"],
'/rest/**'                                          : ["permitAll"],
'/login/**'                                         : ["permitAll"],
'/logout/**'                                        : ["permitAll"],
'/job/list'                                         : ["permitAll"],
'/job/saveJobSet'                                   : ["permitAll"],
'/job/getRunningAndRecentlyFinishedJobs'            : ["permitAll"],
'/job/nextExecution'                                : ["permitAll"],
'/job/getLastRun'                                   : ["permitAll"],
'/script/list'                                      : ["permitAll"],
'/queueStatus/list'                                 : ["permitAll"],
'/queueStatus/refresh'                              : ["permitAll"],
'/jobSchedule/schedules'                            : ["permitAll"],
'/connectivityProfile/list'                         : ["permitAll"],
'/about'                                            : ["permitAll"],
'/cookie/**'                                        : ["permitAll"],
'/csiDashboard/storeCustomDashboard'                : ["permitAll"],
'/csiDashboard/validateDashboardName'               : ["permitAll"],
'/csiDashboard/validateAndSaveDashboardValues'      : ["permitAll"],
'/i18n/getAllMessages'                              : ["permitAll"],
//////////////////////////////////////////////////////////////////
//SUPER_ADMIN only
//////////////////////////////////////////////////////////////////
'/console/**'                                     : ['ROLE_SUPER_ADMIN'],
'/apiKey/**'                                      : ['ROLE_SUPER_ADMIN'],
//////////////////////////////////////////////////////////////////
//ADMIN or SUPER_ADMIN log in
//////////////////////////////////////////////////////////////////
'/**'                                             : ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']
]

/*
 *	Configure charting libraries available in OpenSpeedMonitor.
 * 	Default is rickshaw, see http://code.shutterstock.com/rickshaw/
 * 	Highcharts (http://www.highcharts.com/) is possible, too, but licensed proprietary.
 */
/** default charting lib */
grails.de.iteratec.osm.report.chart.chartTagLib = ChartingLibrary.RICKSHAW
/** all available charting libs */
grails.de.iteratec.osm.report.chart.availableChartTagLibs = [ChartingLibrary.RICKSHAW]

// if not specified default in code is 30 days
// unit: seconds
grails.plugins.cookie.cookieage.default = 60 * 60 * 24 * 36

//Exclude all less files, but not the main less files. This.will solv.dependency errors and will increase the performance.
grails.assets.less.compile = 'less4j'
grails.assets.plugin."twitter-bootstrap".excludes = ["**/*.less"]
grails.assets.plugin."font-awesome-resources".excludes = ["**/*.less"]
grails.assets.excludes = ["openspeedmonitor.less"]

grails.assets.minifyJs = true
grails.assets.minifyCss = true

grails.i18n.locales = ['en', 'de']

grails.plugin.databasemigration.updateOnStart = true
grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']

// environment-specific config //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

environments {
    development {

        grails.logging.jul.usebridge = true

        grails.dbconsole.enabled = true
        grails.dbconsole.urlRoot = '/admin/dbconsole'

        // grails console-plugin, see https://github.com/sheehan/grails-console
        grails.plugin.console.enabled = true
        grails.plugin.console.fileStore.remote.enabled = false
        // Whether to include the remote file store functionality. Default is true.

        grails.assets.bundle = true

        log4j = {

            def catalinaBase = System.properties.getProperty('catalina.base')
            if (!catalinaBase) catalinaBase = '.'   // just in case
            def logFolder = "${catalinaBase}/logs/"

//            doesn't work cause we can't access grailsApplictaion or there are no serviceClasses/controllerClasses:
//            List<String> identifiersToLogExplicitlyFor = []
//            identifiersToLogExplicitlyFor << 'grails.app.conf'
//            identifiersToLogExplicitlyFor << 'grails.app.filters'
//            identifiersToLogExplicitlyFor << 'grails.app.taglib'
//            identifiersToLogExplicitlyFor << 'grails.app.domain'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.isj'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.osm'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.isocsi'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.ispc'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.isr'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.iss'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.issc'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.chart'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.isj'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.osm'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.isocsi'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.ispc'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.isr'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.issc'
//			identifiersToLogExplicitlyFor.addAll(grailsApplication.serviceClasses.collect {"grails.app.services.${it.fullName}"})
//			identifiersToLogExplicitlyFor.addAll(grailsApplication.controllerClasses.collect {"grails.app.controllers.${it.fullName}"})

            appenders {

                console(
                        name: 'stdout',
                        layout: pattern(conversionPattern: '%c{2} %m%n'),
                        threshold: org.apache.log4j.Level.ERROR
                )

                /**
                 * Standard-appender for openSpeedMonitor-app. One Logging-level for the whole application.
                 * 	Nothing else is logged.
                 */
                appender new DailyRollingFileAppender(
                        name: 'osmAppender',
                        datePattern: "'.'yyyy-MM-dd",  // See the API for all patterns.
                        fileName: "logs/${appNameForLog4jConfig}.log",
                        layout: pattern(conversionPattern: "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %c{2} (line %L): %m%n"),
                        threshold: org.apache.log4j.Level.ERROR
                )
                /**
                 * Detail-appender for openSpeedMonitor-app. Logging-level can be set for every package separately at runtime.
                 * Grails-core packages get logged, too.
                 */
                RollingFileAppender rollingFileAppender = new RollingFileAppender(
                        name: 'osmAppenderDetails',
                        fileName: "logs/${appNameForLog4jConfig}Details.log",
                        layout: pattern(conversionPattern: "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %c{2} (line %L): %m%n"),
                        maxFileSize: '20MB',
                        maxBackupIndex: 10,
                        threshold: org.apache.log4j.Level.DEBUG
                )
                appender rollingFileAppender
                AsyncAppender asyncAppender = new AsyncAppender(
                        name: 'asyncOsmAppenderDetails',
                )
                asyncAppender.addAppender(rollingFileAppender)
                appender asyncAppender
            }
            // Per default all is logged for application artefacts.
            // Appenders apply their own threshold level to limit logs.
            all(
                    osmAppender: [
                            'grails.app'
                    ],
                    asyncOsmAppenderDetails: [
                            'grails.app'
                    ]
            )
            error(
                    osmAppender: [
                            'org.codehaus.groovy.grails.commons',            // core / classloading
                            'org.codehaus.groovy.grails.web.mapping',        // URL mapping
                            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
                            'org.codehaus.groovy.grails.web.pages',          // GSP
                            'org.codehaus.groovy.grails.web.servlet',        // controllers
                            'org.codehaus.groovy.grails.web.sitemesh',       // layouts
                            'org.codehaus.groovy.grails.plugins',            // plugins
                            'org.springframework',
                            'net.sf.ehcache.hibernate',
                            'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
                            'org.hibernate.SQL', 'org.hibernate.transaction'    //hibernate],
                    ],
                    asyncOsmAppenderDetails: [
                            'org.codehaus.groovy.grails.commons',            // core / classloading
                            'org.codehaus.groovy.grails.web.mapping',        // URL mapping
                            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
                            'org.codehaus.groovy.grails.web.pages',          // GSP
                            'org.codehaus.groovy.grails.web.servlet',        // controllers
                            'org.codehaus.groovy.grails.web.sitemesh',       // layouts
                            'org.codehaus.groovy.grails.plugins',            // plugins
                            'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
                            'org.springframework',
                            'net.sf.ehcache.hibernate',
                            'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
                            'org.hibernate.SQL', 'org.hibernate.transaction' //hibernate
                    ]
            )
        }

    }
    production {

        //base url of osm instance can be configured here or in external configuration file (see grails-app/conf/OpenSpeedMonitor-config.groovy.sample)
        //grails.serverURL = "https://[base-url-of-your-prod-osm-instance]"

        grails.logging.jul.usebridge = false

        grails.dbconsole.enabled = true
        grails.dbconsole.urlRoot = '/admin/dbconsole'

        // grails console-plugin, see https://github.com/sheehan/grails-console
        // Whether to enable the plugin. Default is true for the development environment, false otherwise.
        grails.plugin.console.enabled = true
        // Whether to include the remote file store functionality. Default is true.
        // Should never be set to true in production. Otherwise everybody with an account in group root has access for
        // all files of unix user the servlet container is running as!!!
        grails.plugin.console.fileStore.remote.enabled = false

        log4j = {

            def catalinaBase = System.properties.getProperty('catalina.base')
            if (!catalinaBase) catalinaBase = '.'   // just in case
            def logFolder = "${catalinaBase}/logs/"

//            doesn't work cause we can't access grailsApplictaion or there are no serviceClasses/controllerClasses:
//            List<String> identifiersToLogExplicitlyFor = []
//            identifiersToLogExplicitlyFor << 'grails.app.conf'
//            identifiersToLogExplicitlyFor << 'grails.app.filters'
//            identifiersToLogExplicitlyFor << 'grails.app.taglib'
//            identifiersToLogExplicitlyFor << 'grails.app.domain'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.isj'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.osm'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.isocsi'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.ispc'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.isr'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.iss'
//            identifiersToLogExplicitlyFor << 'grails.app.controllers.de.iteratec.issc'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.chart'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.isj'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.osm'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.isocsi'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.ispc'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.isr'
//            identifiersToLogExplicitlyFor << 'grails.app.services.de.iteratec.issc'
            //			identifiersToLogExplicitlyFor.addAll(grailsApplication.serviceClasses.collect {"grails.app.services.${it.fullName}"})
            //			identifiersToLogExplicitlyFor.addAll(grailsApplication.controllerClasses.collect {"grails.app.controllers.${it.fullName}"})

            appenders {

                console(
                        name: 'stdout',
                        layout: pattern(conversionPattern: '%c{2} %m%n'),
                        threshold: org.apache.log4j.Level.ERROR
                )

                /**
                 * Standard-appender for openSpeedMonitor-app.
                 * Log-level ERROR as threshold.
                 * Per log4j configuration all would be logged.
                 */
                appender new DailyRollingFileAppender(
                        name: 'osmAppender',
                        datePattern: "'.'yyyy-MM-dd",  // See the API for all patterns.
                        fileName: "${logFolder}${appNameForLog4jConfig}.log",
                        layout: pattern(conversionPattern: "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %c{2} (line %L): %m%n"),
                        threshold: org.apache.log4j.Level.ERROR
                )
                /**
                 * Detail-appender for OpenSpeedMonitor-app.
                 * Log-level DEBUG as threshold.
                 * Per log4j configuration all would be logged.
                 */
                RollingFileAppender rollingFileAppender = new RollingFileAppender(
                        name: 'osmAppenderDetails',
                        fileName: "${logFolder}${appNameForLog4jConfig}Details.log",
                        layout: pattern(conversionPattern: "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %c{2} (line %L): %m%n"),
                        maxFileSize: '20MB',
                        maxBackupIndex: 20,
                        threshold: org.apache.log4j.Level.DEBUG
                )
                appender rollingFileAppender
                AsyncAppender asyncAppender = new AsyncAppender(
                        name: 'asyncOsmAppenderDetails',
                )
                asyncAppender.addAppender(rollingFileAppender)
                appender asyncAppender
            }
            // Per default all is logged for application artefacts.
            // Appenders apply their own threshold level to limit logs.
            all(
                    osmAppender: [
                            'grails.app'
                    ],
                    asyncOsmAppenderDetails: [
                            'grails.app'
                    ]
            )
            error(
                    osmAppender: [
                            'org.codehaus.groovy.grails.commons',            // core / classloading
                            'org.codehaus.groovy.grails.web.mapping',        // URL mapping
                            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
                            'org.codehaus.groovy.grails.web.pages',          // GSP
                            'org.codehaus.groovy.grails.web.servlet',        // controllers
                            'org.codehaus.groovy.grails.web.sitemesh',       // layouts
                            'org.codehaus.groovy.grails.plugins',            // plugins
                            // GSP
                            'org.codehaus.groovy.grails.web.servlet',
                            // controllers
                            'org.codehaus.groovy.grails.web.sitemesh',
                            // layouts
                            'org.codehaus.groovy.grails.plugins',
                            // plugins
                            'org.springframework',
                            'net.sf.ehcache.hibernate',
                            'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
                            'org.hibernate.SQL', 'org.hibernate.transaction'    //hibernate],
                    ],
                    asyncOsmAppenderDetails: [
                            'org.codehaus.groovy.grails.commons',            // core / classloading
                            'org.codehaus.groovy.grails.web.mapping',        // URL mapping
                            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
                            'org.codehaus.groovy.grails.web.pages',          // GSP
                            'org.codehaus.groovy.grails.web.servlet',        // controllers
                            'org.codehaus.groovy.grails.web.sitemesh',       // layouts
                            'org.codehaus.groovy.grails.plugins',            // plugins
                            'org.springframework',
                            'net.sf.ehcache.hibernate',
                            'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
                            'org.hibernate.SQL', 'org.hibernate.transaction'    //hibernate],
                    ]
            )
        }
    }
    test {
        grails.logging.jul.usebridge = true

        grails.dbconsole.enabled = true
        grails.dbconsole.urlRoot = '/admin/dbconsole'

        // grails console-plugin, see https://github.com/sheehan/grails-console
        grails.plugin.console.enabled = true
        // Whether to enable the plugin. Default is true for the development environment, false otherwise.
        grails.plugin.console.fileStore.remote.enabled = true
        // Whether to include the remote file store functionality. Default is true.

        log4j = {
            appenders {
                console name: 'stdout', layout: pattern(conversionPattern: '%c{2} %m%n')
            }

            info(stdout: ['grails.app', 'co.freeside.betamax'])

            info(stdout: [
                    'org.codehaus.groovy.grails.web.servlet',        // controllers
                    'org.codehaus.groovy.grails.web.pages',          // GSP
                    'org.codehaus.groovy.grails.web.sitemesh',       // layouts
                    'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
                    'org.codehaus.groovy.grails.web.mapping',        // URL mapping
                    'org.codehaus.groovy.grails.commons',            // core / classloading
                    'org.codehaus.groovy.grails.plugins',            // plugins
                    'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
                    'org.springframework',
                    'org.hibernate',
                    'net.sf.ehcache.hibernate'])

        }
    }
}