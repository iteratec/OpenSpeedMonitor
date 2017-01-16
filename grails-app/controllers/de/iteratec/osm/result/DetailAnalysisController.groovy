package de.iteratec.osm.result

import de.iteratec.osm.api.MicroServiceApiKey
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.wptserverproxy.AssetRequestPersisterService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.util.ControllerUtils
import grails.web.mapping.LinkGenerator
import groovyx.net.http.RESTClient
import org.joda.time.Interval

import java.lang.reflect.InvocationTargetException

class DetailAnalysisController {
    JobGroupDaoService jobGroupDaoService
    LinkGenerator grailsLinkGenerator
    EventResultDashboardService eventResultDashboardService
    AssetRequestPersisterService assetRequestPersisterService

    public final static String DATE_FORMAT_STRING_FOR_HIGH_CHART = 'dd.mm.yyyy';
    public final static int MONDAY_WEEKSTART = 1

    def intervals = ['not', 'hourly', 'daily', 'weekly']

    def show(DetailAnalysisDashboardShowCommand cmd) {
        Map modelToRender = constructStaticViewDataOfShowAll();
        cmd.copyRequestDataToViewModelMap(modelToRender);

        if (ControllerUtils.isEmptyRequest(params)) {
            return modelToRender
        }
        if (!ControllerUtils.isEmptyRequest(params) && !cmd.validate()) {
            modelToRender.put('command', cmd)
        }

        String osmUrl = grailsLinkGenerator.getServerBaseURL()
        def errorList = []

        if (!osmUrl) {
            errorList << message(code: 'default.serverUrl.undefined', args: [message(code: 'default.serverUrl.undefined', default: 'The server url is undefined. You can set it in the custom osm-properties.\n')])
        }
        String microServiceUrl = grailsApplication.config.getProperty('grails.de.iteratec.osm.assetRequests.microserviceUrl')
        microServiceUrl = microServiceUrl.endsWith("/") ? microServiceUrl : microServiceUrl + "/"
        if (!microServiceUrl) {
            errorList << message(code: 'default.microService.osmDetailAnalysis.url.undefined', args: [message(code: 'default.microService.osmDetailAnalysis.url.undefined', default: 'The url for the OsmDetailAnalysis micro service is undefined. You can set it in the custom osm-properties.\n')])
        } else {
            microServiceUrl = microServiceUrl.endsWith("/") ? microServiceUrl : "${microServiceUrl}/"
        }

        String apiKey = MicroServiceApiKey.findByMicroService("OsmDetailAnalysis").secretKey
        if (!apiKey) {
            errorList << message(code: 'default.microService.osmDetailAnalysis.apiKey.undefined', args: [message(code: 'default.microService.osmDetailAnalysis.apiKey.undefined', default: 'The api key for the OsmDetailAnalysis micro service is undefined. You can set it in the custom osm-properties.\n')])
        }
        if (osmUrl && microServiceUrl && apiKey) {
            try {
                if (osmUrl.endsWith("/")) osmUrl = osmUrl.substring(0, osmUrl.length() - 1)
                def timeFrame = cmd.getSelectedTimeFrame()
                String queryString = "?apiKey=${apiKey}&osmUrl=${osmUrl}&toDate=${timeFrame.endMillis}&fromDate=${timeFrame.startMillis}&" + request.queryString
                String detailDataWebPageAsString = new URL(microServiceUrl + "detailAnalysisDashboard/show" + queryString).getText()
                modelToRender.put("osmDetailAnalysisRequest", detailDataWebPageAsString)
            } catch (InvocationTargetException ex) {
                errorList << message(code: 'default.microService.osmDetailAnalysis.unreachable', args: [message(code: 'default.microService.osmDetailAnalysis.unreachable', default: 'Microservice unreachable\n')])
            } catch (ConnectException ex) {
                errorList << message(code: 'default.microService.osmDetailAnalysis.unreachable', args: [message(code: 'default.microService.osmDetailAnalysis.unreachable', default: 'Microservice unreachable\n')])
            }
        }
        modelToRender.put("errorList", errorList)

        return modelToRender
    }

    private Map constructStaticViewDataOfShowAll() {
        Map<String, Object> result = [:]

        // JobGroups
        List<JobGroup> jobGroups = eventResultDashboardService.getAllJobGroups()
        result.put('folders', jobGroups)

        // Pages
        List<Page> pages = eventResultDashboardService.getAllPages()
        result.put('pages', pages)

        // MeasuredEvents
        List<MeasuredEvent> measuredEvents = eventResultDashboardService.getAllMeasuredEvents()
        result.put('measuredEvents', measuredEvents)

        // Browsers
        List<Browser> browsers = eventResultDashboardService.getAllBrowser()
        result.put('browsers', browsers)

        // Locations
        List<Location> locations = eventResultDashboardService.getAllLocations()
        result.put('locations', locations)

        // ConnectivityProfiles
        result['connectivityProfiles'] = eventResultDashboardService.getAllConnectivityProfiles()

        // JavaScript-Utility-Stuff:
        result.put("dateFormat", DATE_FORMAT_STRING_FOR_HIGH_CHART)
        result.put("weekStart", MONDAY_WEEKSTART)

        result.put("selectedChartType", 0);
        result.put("warnAboutExceededPointsPerGraphLimit", false);

        result.put("tagToJobGroupNameMap", jobGroupDaoService.getTagToJobGroupNameMap())

        result.put('persistenceOfAssetRequestsEnabled', grailsApplication.config.getProperty('grails.de.iteratec.osm.assetRequests.enablePersistenceOfAssetRequests') == 'true')

        // Done! :)
        return result;
    }

    def sendFetchAssetsAsBatchCommand(DetailAnalysisDashboardShowCommand cmd){
        Map<String, Object> modelToRender = constructStaticViewDataOfShowAll();

        cmd.copyRequestDataToViewModelMap(modelToRender);

        def connectivityProfiles
        if (cmd.selectedConnectivityProfiles){
            connectivityProfiles=ConnectivityProfile.findAllByIdInList(cmd.selectedConnectivityProfiles)
        }

        List<Job> jobs = Job.createCriteria().list {
            if(cmd.includeCustomConnectivity)inList("customConnectivityName",[cmd.customConnectivityName,null]) // if custom connectivity is included
            else eq("customConnectivityProfile",cmd.includeCustomConnectivity)
            if(!cmd.includeNativeConnectivity)eq("noTrafficShapingAtAll", cmd.includeNativeConnectivity)
            if(!cmd.selectedAllConnectivityProfiles)inList("connectivityProfile", connectivityProfiles)
        }
        Interval timeFrame = cmd.getSelectedTimeFrame();
        def jobGroupList = []
        cmd.selectedFolder.each{
            jobGroupList.add(JobGroup.findById(it).name)
        }
        def selectedBrowsersList = []
        cmd.selectedBrowsers.each{
            selectedBrowsersList.add(Browser.findById(it).name)
        }
        def selectedLocationsList = []
        cmd.selectedLocations.each {
            selectedLocationsList.add(Location.findById(it).location)
        }
        List<JobResult> jobResults =[]
        if(jobs) {
            jobResults = JobResult.createCriteria().list {
                inList("job", jobs)
                if (cmd.selectedFolder) inList("jobGroupName", jobGroupList)
                if (!cmd.selectedAllBrowsers) inList("locationBrowser", selectedBrowsersList)
                if (!cmd.selectedAllLocations) inList("locationLocation", selectedLocationsList)
                between("date", timeFrame.getStart().toDate(), timeFrame.getEnd().toDate())
            }
        }
        def batchIsQueued = assetRequestPersisterService.sendFetchAssetsAsBatchCommand(jobResults)
        modelToRender.put("startedBatchActivity",batchIsQueued)
        render(view: "show", model: modelToRender)
    }

    RESTClient getRestClient(String url) {
        return new RESTClient(url)
    }
}
