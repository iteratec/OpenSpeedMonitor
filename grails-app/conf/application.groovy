import de.iteratec.osm.report.chart.ChartingLibrary
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.grails.config.yaml.YamlPropertySourceLoader
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.AbstractEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader


/*
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

grails.databinding.dateFormats = [
        'dd.MM.yyyy', 'yyyy-MM-dd', 'yyyy/MM/dd', 'MMddyyyy', 'yyyy-MM-dd HH:mm:ss.S', 'yyyy-MM-dd HH:mm:ss', "yyyy-MM-dd'T'hh:mm:ss'Z'"]



// config for all environments //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

info{
    app{
        name = '@info.app.name@'
        version = '@info.app.version@'
        grailsVersion = '@info.app.grailsVersion@'
    }
}

grails{

    profile = "web"
    codegen.defaultPackage = "de.iteratec.osm"
    spring.transactionManagement.proxies = false

    mime{
        disable.accept.header.userAgents = [
            'Gecko',
            'WebKit',
            'Presto',
            'Trident'
        ]
        file.extensions = true // enables the parsing of file extensions from URLs into the request format
        use.accept.header = false
        types = [
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
                xml          : ['text/xml', 'application/xml'],
                pdf          : 'application/pdf',
                hal          : ['application/hal+json', 'application/hal+xml']
        ]
    }
    urlmapping.cache.maxsize = 1000
    controllers.defaultScope = 'singleton'

}

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.views.gsp.htmlcodec = 'xml'
grails.views.gsp.codecs.expression = 'html'
grails.views.gsp.codecs.scriptlets = 'html'
grails.views.gsp.codecs.taglib = 'none'
grails.views.gsp.codecs.staticparts = 'none'
grails.converters.encoding = 'UTF-8'//"ISO-8859-1"
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
//TODO: Where does this come from !?
//spring.groovy.template.check-template-location = false

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
////////////////////////////////////////////////////////////////
//free for all (even guests not logged in)
////////////////////////////////////////////////////////////////
[pattern: '/static/**',                                     access: ["permitAll"]],
[pattern: '/static/*',                                      access: ["permitAll"]],
[pattern: '/assets/**',                                     access: ["permitAll"]],
[pattern: '/assets/*',                                      access: ["permitAll"]],
[pattern: '/css/**',                                        access: ["permitAll"]],
[pattern: '/js/**',                                         access: ["permitAll"]],
[pattern: '/images/**',                                     access: ["permitAll"]],
[pattern: '/less/**',                                       access: ["permitAll"]],
[pattern: '/',                                              access: ["permitAll"]],
[pattern: '/proxy/**',                                      access: ["permitAll"]],
[pattern: '/wptProxy/**',                                   access: ["permitAll"]],
[pattern: '/csiDashboard/index',                            access: ["permitAll"]],
[pattern: '/csiDashboard/showAll',                          access: ["permitAll"]],
[pattern: '/csiDashboard/csiValuesCsv',                     access: ["permitAll"]],
[pattern: '/csiDashboard/showDefault',                      access: ["permitAll"]],
[pattern: '/csiConfiguration/configurations/**',            access: ["permitAll"]],
[pattern: '/csiConfigIO/downloadBrowserWeights',            access: ["permitAll"]],
[pattern: '/csiConfigIO/downloadPageWeights',               access: ["permitAll"]],
[pattern: '/csiConfigIO/downloadHourOfDayWeights',          access: ["permitAll"]],
[pattern: '/csiConfigIO/downloadBrowserConnectivityWeights',access: ["permitAll"]],
[pattern: '/csiConfigIO/downloadDefaultTimeToCsMappings',   access: ["permitAll"]],
[pattern: '/eventResultDashboard/**',                       access: ["permitAll"]],
[pattern: '/tabularResultPresentation/**',                  access: ["permitAll"]],
[pattern: '/highchartPointDetails/**',                      access: ["permitAll"]],
[pattern: '/rest/**',                                       access: ["permitAll"]],
[pattern: '/login/**',                                      access: ["permitAll"]],
[pattern: '/logout/**',                                     access: ["permitAll"]],
[pattern: '/job/list',                                      access: ["permitAll"]],
[pattern: '/job/saveJobSet',                                access: ["permitAll"]],
[pattern: '/job/getRunningAndRecentlyFinishedJobs',         access: ["permitAll"]],
[pattern: '/job/nextExecution',                             access: ["permitAll"]],
[pattern: '/job/getLastRun',                                access: ["permitAll"]],
[pattern: '/script/list',                                   access: ["permitAll"]],
[pattern: '/queueStatus/list',                              access: ["permitAll"]],
[pattern: '/queueStatus/refresh',                           access: ["permitAll"]],
[pattern: '/jobSchedule/schedules',                         access: ["permitAll"]],
[pattern: '/connectivityProfile/list',                      access: ["permitAll"]],
[pattern: '/about',                                         access: ["permitAll"]],
[pattern: '/cookie/**',                                     access: ["permitAll"]],
[pattern: '/csiDashboard/storeCustomDashboard',             access: ["permitAll"]],
[pattern: '/csiDashboard/validateDashboardName',            access: ["permitAll"]],
[pattern: '/csiDashboard/validateAndSaveDashboardValues',   access: ["permitAll"]],
[pattern: '/i18n/getAllMessages',                           access: ["permitAll"]],
//////////////////////////////////////////////////////////////////
//SUPER_ADMIN only
//////////////////////////////////////////////////////////////////
[pattern: '/console/**',                                    access: ['ROLE_SUPER_ADMIN']],
[pattern: '/apiKey/**',                                     access: ['ROLE_SUPER_ADMIN']],
//////////////////////////////////////////////////////////////////
//ADMIN or SUPER_ADMIN log in
//////////////////////////////////////////////////////////////////
[pattern: '/**',                                            access: ['ROLE_SUPER_ADMIN', 'ROLE_SUPER_ADMIN']]
]

/*
 *  Configure charting libraries available in OpenSpeedMonitor.
 *  Default is rickshaw, see http://code.shutterstock.com/rickshaw/
 *  Highcharts (http://www.highcharts.com/) is possible, too, but licensed proprietary.
 */
/** default charting lib */
grails.de.iteratec.osm.report.chart.chartTagLib = ChartingLibrary.RICKSHAW
/** all available charting libs */
grails.de.iteratec.osm.report.chart.availableChartTagLibs = [ChartingLibrary.RICKSHAW]

// if not specified default in code is 30 days
// unit: seconds
grails.plugins.cookie.cookieage.default = 60 * 60 * 24 * 36

//Exclude all less files, but not the main less files. This.will solv.dependency errors and will increase the performance.
grails.assets.less.compile = 'logback'
grails.assets.plugin."twitter-bootstrap".excludes = ["**/*.less"]
grails.assets.plugin."font-awesome-resources".excludes = ["**/*.less"]
grails.assets.excludes = ["openspeedmonitor.less"]

grails.assets.minifyJs = true
grails.assets.minifyCss = true

grails.i18n.locales = ['en', 'de']

grails.plugin.databasemigration.updateOnStart = true
grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']

//TODO: Where did this come from?!?
//endpoints.jmx.unique-names = true


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

        grails.plugin.databasemigration.dropOnStart = true
        grails.plugin.databasemigration.autoMigrateScripts = 'TestApp'
        grails.plugin.databasemigration.forceAutoMigrate = true


    }
}
/**
 * The datasources defined in the following bock are default datasources to be used only when running the app out of the box via run-app.
 * Datasources different from default can and should be defined in separate external config files. 
 * Config param grails.config.locations in this file contains a list of possible locations for such additional config files.
 * In addition you can add an own location for an external config file via system property "osm_config_location"
 * 
 * @author nkuhn
 * @see OpenSpeedMonitor-config.groovy.sample
 * @see http://mrhaki.blogspot.de/2015/09/grails-goodness-using-external.html
 *
 */
// general settings
dataSource {
    pooled = true
    jmxExport = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    cache.queries = false
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    test {
        dataSource {
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    production {
            dataSource {
            url = "jdbc:h2:mem:prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
}

codenarc {
    processTestUnit = false
    processTestIntegration = false
    propertiesFile = 'codenarc.properties'
    ruleSetFiles = "file:grails-app/conf/CodeNarcRules.groovy"
    reports = {
        RedisReport('xml') {
            // The report name "MyXmlReport" is user-defined; Report type is 'xml'
            outputFile = 'target/codenarc.xml'  // Set the 'outputFile' property of the (XML) Report
            title = 'Grails Redis Plugin'             // Set the 'title' property of the (XML) Report
        }
    }
}