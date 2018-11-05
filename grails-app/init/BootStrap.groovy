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


import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.Status
import de.iteratec.osm.csi.*
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.wptserver.DetailAnalysisPersisterService
import de.iteratec.osm.measurement.environment.wptserver.LocationPersisterService
import de.iteratec.osm.measurement.environment.wptserver.ResultPersisterService
import de.iteratec.osm.measurement.environment.wptserver.WptInstructionService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobSchedulingService
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.report.external.GraphiteServer
import de.iteratec.osm.report.external.HealthReportService
import de.iteratec.osm.security.Role
import de.iteratec.osm.security.User
import de.iteratec.osm.security.UserRole
import de.iteratec.osm.util.I18nService
import grails.util.Environment
import org.apache.commons.validator.routines.UrlValidator
import org.joda.time.DateTime

import static de.iteratec.osm.OsmConfiguration.DEFAULT_MAX_VALID_LOADTIME
import static de.iteratec.osm.OsmConfiguration.DEFAULT_MIN_VALID_LOADTIME

class BootStrap {

    EventCsiAggregationService eventCsiAggregationService
    CsiAggregationUtilService csiAggregationUtilService
    JobSchedulingService jobSchedulingService
    JobResultPersisterService jobResultPersisterService
    I18nService i18nService
    ResultPersisterService resultPersisterService
    LocationPersisterService locationPersisterService
    DetailAnalysisPersisterService detailAnalysisPersisterService
    WptInstructionService wptInstructionService
    HealthReportService healthReportService
    InMemoryConfigService inMemoryConfigService
    def grailsApplication

    /**
     * Will be created on first start of application without any WebPagetest servers.
     */
    WebPageTestServer initiallyCreatedWptServer

    def init = { servletContext ->

        log.info("Environment in Bootstrap: ${Environment.current}")

        switch (Environment.getCurrent()) {
            case Environment.DEVELOPMENT:
                initApplicationData(true)
                registerProxyListener()
                fetchLocationsOfWebpagetestOnFirstStart()
                break
            case Environment.TEST:
                // no creation of test-data, cause each test will create its own data
                registerProxyListener()
                break;
            case Environment.PRODUCTION:
                initApplicationData(false)
                registerProxyListener()
                fetchLocationsOfWebpagetestOnFirstStart()
                inMemoryConfigService.activateMeasurementsGenerally()
                break
        }

    }

    def destroy = {
    }

    def initApplicationData = { boolean createDefaultUsers ->

        log.info "initApplicationData() OSM starts with createDefaultUsers=${createDefaultUsers}"

        initConfig()
        initUserData(createDefaultUsers)
        initCsiData()
        initMeasurementInfrastructure()
        initJobScheduling()
        cancelActiveBatchActivity()
        excludePropertiesInJsonRepresentationsofDomainObjects()
        initHealthReporting()
        updateScripts()

        log.info "initApplicationData() OSM ends"

    }


    void initHealthReporting() {
        log.info("initHealthReporting() OSM starts")
        GraphiteServer.findAllByReportHealthMetrics(true).each {
            healthReportService.handleGraphiteServer(it)
        }
        log.info("initHealthReporting() OSM ends")
    }

    void initConfig() {
        log.info "initConfig() OSM starts"

        List<OsmConfiguration> configs = OsmConfiguration.list()

        if (configs.size() != 1) {
            deleteAllInvalidAndCreateNewOsmConfig(configs)
        }

        log.info "initConfig() OSM ends"
    }

    void deleteAllInvalidAndCreateNewOsmConfig(List<OsmConfiguration> configs) {
        configs.each {
            it.delete()
        }
        new OsmConfiguration(
                detailDataStorageTimeInWeeks: 12,
                defaultMaxDownloadTimeInMinutes: 60,
                minValidLoadtime: DEFAULT_MIN_VALID_LOADTIME,
                maxValidLoadtime: DEFAULT_MAX_VALID_LOADTIME,
                maxDataStorageTimeInMonths: 13,
        ).save(failOnError: true)
    }

    void initJobScheduling() {
        log.info "initJobScheduling() OSM starts"

        createConnectivityProfileIfMissing(6000, 512, 50, 'DSL 6.000', 0)
        createConnectivityProfileIfMissing(384, 384, 140, 'UMTS', 0)
        createConnectivityProfileIfMissing(3600, 1500, 40, 'UMTS - HSDPA', 0)

        jobSchedulingService.scheduleAllActiveJobs()

        log.info "initJobScheduling() OSM ends"
    }

    void initUserData(boolean createDefaultUsers) {
        log.info "initUserData() OSM starts"

        // Roles ////////////////////////////////////////////////////////////////////////
        Role adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(failOnError: true)
        Role rootRole = Role.findByAuthority('ROLE_SUPER_ADMIN') ?: new Role(authority: 'ROLE_SUPER_ADMIN').save(failOnError: true)

        // Users ////////////////////////////////////////////////////////////////////////

        //read config entries
        String appAdminUserName = grailsApplication.config.grails.de.iteratec.osm.security.initialOsmAdminUser.username.isEmpty() ?
                null : grailsApplication.config.grails.de.iteratec.osm.security.initialOsmAdminUser.username
        String appAdminPassword = grailsApplication.config.grails.de.iteratec.osm.security.initialOsmAdminUser.password.isEmpty() ?
                null : grailsApplication.config.grails.de.iteratec.osm.security.initialOsmAdminUser.password
        String appRootUserName = grailsApplication.config.grails.de.iteratec.osm.security.initialOsmRootUser.username.isEmpty() ?
                null : grailsApplication.config.grails.de.iteratec.osm.security.initialOsmRootUser.username
        String appRootPassword = grailsApplication.config.grails.de.iteratec.osm.security.initialOsmRootUser.password.isEmpty() ?
                null : grailsApplication.config.grails.de.iteratec.osm.security.initialOsmRootUser.password
        String warnMessage = createDefaultUsers ? 'A default user will be created if no one existed.' : 'No such user will be created.'

        // admin user
        if (appAdminUserName == null || appAdminPassword == null) {
            log.warn("You haven't set environment variables to create an admin user. ${warnMessage}")
            if (createDefaultUsers) createUser('admin', 'admin', adminRole)
        } else {
            createUser(appAdminUserName, appAdminPassword, adminRole)
        }
        //root user
        if (appRootUserName == null || appRootPassword == null) {
            log.warn("You haven't set environment variables to create a root user. ${warnMessage}")
            if (createDefaultUsers) createUser('root', 'root', rootRole)
        } else {
            createUser(appRootUserName, appRootPassword, rootRole)
        }

        log.info "initUserData() OSM ends"
    }

    void createUser(String username, String password, Role role) {
        User user = User.findByUsername(username) ?: new User(
                username: username,
                password: password,
                enabled: true,
                accountExpired: false,
                accountLocked: false,
                passwordExpired: false).save(failOnError: true)
        UserRole.findByUser(user) ?: new UserRole(user: user, role: role).save(failOnError: true)
    }




    void initCsiData() {
        log.info "initCsiData starts"

        def csiGroupName = JobGroup.UNDEFINED_CSI
        JobGroup.findByName(csiGroupName) ?: new JobGroup(
                name: csiGroupName).save(failOnError: true)

        // here you can initialize the weights of the hours of the csiDay for csi calculation  (see de.iteratec.osm.csi.PageCsiAggregationService)
        if (CsiDay.count <= 0) {
            CsiDay initDay = new CsiDay()
            (0..23).each {
                initDay.setHourWeight(it, 1)
            }
            initDay.save(failOnError: true)
        }

        Page.findByName(Page.UNDEFINED) ?: new Page(name: Page.UNDEFINED).save(failOnError: true)

        CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY) ?: new CsiAggregationInterval(
                name: "hourly",
                intervalInMinutes: CsiAggregationInterval.HOURLY
        ).save(failOnError: true)

        CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.DAILY) ?: new CsiAggregationInterval(
                name: "daily",
                intervalInMinutes: CsiAggregationInterval.DAILY
        ).save(failOnError: true)

        CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.WEEKLY) ?: new CsiAggregationInterval(
                name: "weekly",
                intervalInMinutes: CsiAggregationInterval.WEEKLY
        ).save(failOnError: true)

        Date date = new DateTime(2000, 1, 1, 0, 0).toDate()
        Double percent = 90
        CsTargetValue val1 = CsTargetValue.findByDateAndCsInPercent(date, percent) ?: new CsTargetValue(
                date: date,
                csInPercent: percent,
        ).save(failOnError: true)
        date = new DateTime(2100, 12, 31, 23, 59).toDate()
        percent = 90
        CsTargetValue val2 = CsTargetValue.findByDateAndCsInPercent(date, percent) ?: new CsTargetValue(
                date: date,
                csInPercent: percent,
        ).save(failOnError: true)

        String labelTargetCsi_EN = i18nService.msgInLocale('de.iteratec.isocsi.targetcsi.label', Locale.ENGLISH, 'Target-CSI')
        String descriptionTargetCsi_EN = i18nService.msgInLocale('de.iteratec.isocsi.targetcsi.description', Locale.ENGLISH, 'Customer satisfaction index defined as target.')
        CsTargetGraph.findByLabel(labelTargetCsi_EN) ?: new CsTargetGraph(
                label: labelTargetCsi_EN,
                description: descriptionTargetCsi_EN,
                pointOne: val1,
                pointTwo: val2,
                defaultVisibility: true
        ).save(failOnError: true)
        String labelTargetCsi_DE = i18nService.msgInLocale('de.iteratec.isocsi.targetcsi.label', Locale.GERMAN, 'Target-CSI')
        String descriptionTargetCsi_DE = i18nService.msgInLocale('de.iteratec.isocsi.targetcsi.description', Locale.GERMAN, 'Customer satisfaction index defined as target.')
        CsTargetGraph.findByLabel(labelTargetCsi_DE) ?: new CsTargetGraph(
                label: labelTargetCsi_DE,
                description: descriptionTargetCsi_DE,
                pointOne: val1,
                pointTwo: val2,
                defaultVisibility: true
        ).save(failOnError: true)

        createDefaultTimeToCsiMappingIfMissing()

        if (CsiConfiguration.count <= 0) {
            CsiConfiguration initCsiConfiguration = new CsiConfiguration()
            initCsiConfiguration.with {
                label = "initial csi configuration"
                description = "a first csi configuration as template"
                csiDay = CsiDay.findAll()[0]
            }
            initCsiConfiguration.save(failOnError: true)
        }

        log.info "initCsiData ends"
    }

    /**
     * These default mappings can be assigned to measured pages if no data of a real customer survey exist.
     * Get created only if no one exist at all.
     */
    void createDefaultTimeToCsiMappingIfMissing() {

        if (DefaultTimeToCsMapping.list().size() == 0) {

            Map indexToMappingName = [1: '1 - impatient', 2: '2', 3: '3', 4: '4', 5: '5 - patient']
            String pathToFile
            String fileName = 'Default_CSI_Mappings.csv'
            InputStream csvIs = this.class.classLoader.getResourceAsStream(fileName)
            BufferedReader csvFileReader = new BufferedReader(new InputStreamReader(csvIs))
            int lineCounter = 0
            String line
            while ((line = csvFileReader.readLine()) != null) {
                // exclude header
                if (lineCounter >= 1) {
                    def tokenized = line.tokenize(';')
                    5.times { defaultMappingindex ->
                        new DefaultTimeToCsMapping(
                                name: indexToMappingName[defaultMappingindex + 1],
                                loadTimeInMilliSecs: tokenized[0],
                                customerSatisfactionInPercent: tokenized[defaultMappingindex + 1]
                        ).save(failOnError: true)
                    }

                }
                lineCounter++
            }
            csvFileReader.close();
        }

    }

    void createConnectivityProfileIfMissing(Integer bwDown, Integer bwUp, Integer latency, String name, Integer packetLoss) {
        ConnectivityProfile.findByName(name) ?:
                new ConnectivityProfile(
                        active: true,
                        bandwidthDown: bwDown,
                        bandwidthUp: bwUp,
                        latency: latency,
                        name: name,
                        packetLoss: packetLoss).save(failOnError: true)
    }

    void initMeasurementInfrastructure() {

        log.info "init measurement infrastructure OSM starts"

        //undefined
        String browserName = Browser.UNDEFINED
        Browser.findByName(browserName) ?: new Browser(name: browserName)
                .addToBrowserAliases(BrowserAlias.findOrCreateByAlias(Browser.UNDEFINED))
                .save(failOnError: true)

        //IE
        browserName = "IE"
        Browser.findByName(browserName) ?: new Browser(name: browserName)
                .addToBrowserAliases(BrowserAlias.findOrCreateByAlias("IE"))
                .addToBrowserAliases(BrowserAlias.findOrCreateByAlias("IE8"))
                .addToBrowserAliases(BrowserAlias.findOrCreateByAlias("Internet Explorer"))
                .addToBrowserAliases(BrowserAlias.findOrCreateByAlias("Internet Explorer 8"))
                .save(failOnError: true)

        //FF
        browserName = "Firefox"
        Browser.findByName(browserName) ?: new Browser(name: browserName)
                .addToBrowserAliases(BrowserAlias.findOrCreateByAlias("FF"))
                .addToBrowserAliases(BrowserAlias.findOrCreateByAlias("FF7"))
                .addToBrowserAliases(BrowserAlias.findOrCreateByAlias("Firefox"))
                .addToBrowserAliases(BrowserAlias.findOrCreateByAlias("Firefox7"))
                .save(failOnError: true)

        //Chrome
        browserName = "Chrome"
        Browser.findByName(browserName) ?: new Browser(name: browserName)
                .addToBrowserAliases(BrowserAlias.findOrCreateByAlias("Chrome"))
                .save(failOnError: true)

        log.info "init measurement infrastructure OSM ends"

    }

    def registerProxyListener = {
        log.info "registerProxyListener OSM ends"
        wptInstructionService.addLocationListener(locationPersisterService)
        jobResultPersisterService.addResultListener(resultPersisterService)

        // enable persistence of detailAnalysisData for JobResults if configured
        boolean persistenceEnabled = grailsApplication.config.grails.de?.iteratec?.osm?.detailAnalysis?.enablePersistenceOfDetailAnalysisData
        if (persistenceEnabled) {
            String microserviceUrl = grailsApplication.config.grails?.de?.iteratec?.osm?.detailAnalysis?.microserviceUrl
            UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS)
            if (!microserviceUrl || !urlValidator.isValid(microserviceUrl)) {
                throw new IllegalStateException("A valid url for the detailAnalysis microservice is required, if persistence of detailAnalysisData is to be enabled")
            }
            microserviceUrl = microserviceUrl.endsWith("/") ? microserviceUrl : microserviceUrl + "/"
            detailAnalysisPersisterService.enablePersistenceOfDetailAnalysisDataForJobResults(microserviceUrl)
            jobResultPersisterService.addResultListener(detailAnalysisPersisterService)
        }

        log.info "persistence of detailAnalysisData is enabled: " + persistenceEnabled
        log.info "registerProxyListener OSM ends"
    }

    void cancelActiveBatchActivity() {
        BatchActivity.findAllByStatus(Status.ACTIVE).each { BatchActivity batchActivity ->
            BatchActivity.withTransaction {
                batchActivity.status = Status.CANCELLED
                batchActivity.save(failOnError: true)
            }
        }
    }

    void excludePropertiesInJsonRepresentationsofDomainObjects() {

        ArrayList<String> propertiesToExcludeFromAllDomains = ['class', 'dirty', 'dirtyPropertyNames', 'errors', 'properties']

        grailsApplication.domainClasses*.clazz.each { domainClass ->
            grails.converters.JSON.registerObjectMarshaller(domainClass) {

                Map propertiesToRepresent = it.properties.findAll { k, v -> !propertiesToExcludeFromAllDomains.contains(k) }
                propertiesToRepresent['id'] = it.ident()

                removeAllServices(propertiesToRepresent)
                removeDomainSpecificProperties(domainClass, propertiesToRepresent)

                return propertiesToRepresent

            }
        }
    }

    void removeDomainSpecificProperties(Class domainClass, Map propertiesToRepresent) {
        if (domainClass == de.iteratec.osm.measurement.environment.BrowserAlias) propertiesToRepresent.remove('browser')
        else if (domainClass == de.iteratec.osm.measurement.schedule.JobGroup) propertiesToRepresent.remove('graphiteServers')
    }

    void removeAllServices(Map propertiesToRepresent) {
        Iterator iterator = propertiesToRepresent.keySet().iterator()
        while (iterator.hasNext()) {
            if (iterator.next().endsWith('Service')) iterator.remove()
        }
    }

    void fetchLocationsOfWebpagetestOnFirstStart() {
        if (initiallyCreatedWptServer) {
            Map retrievsLocationsUsableForPublicApiKeys = [k: "A"]
            wptInstructionService.fetchLocations(initiallyCreatedWptServer, retrievsLocationsUsableForPublicApiKeys)
        }
    }

    void updateScripts(){
        def scripts = Script.findAllByTestedPagesIsEmpty()
        scripts.each { script ->
            //forces a new parsing of the script
            script.beforeUpdate()
            script.save(flush:true)
        }
    }

}
