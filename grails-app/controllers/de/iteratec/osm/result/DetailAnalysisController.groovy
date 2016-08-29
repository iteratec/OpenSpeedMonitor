package de.iteratec.osm.result

import de.iteratec.osm.api.ApiKey
import de.iteratec.osm.api.MicroServiceApiKey
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.util.ControllerUtils
import grails.web.mapping.LinkGenerator
import groovy.xml.XmlUtil
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.springframework.web.util.HtmlUtils

import java.lang.reflect.InvocationTargetException

class DetailAnalysisController {
    EventResultDashboardService eventResultDashboardService
    JobGroupDaoService jobGroupDaoService
    LinkGenerator grailsLinkGenerator

    public final
    static Map<CachedView, Map<String, List<String>>> AGGREGATOR_GROUP_VALUES = ResultCsiAggregationService.getAggregatorMapForOptGroupSelect()

    public final
    static List<String> AGGREGATOR_GROUP_LABELS = ['de.iteratec.isocsi.csi.per.job', 'de.iteratec.isocsi.csi.per.page', 'de.iteratec.isocsi.csi.per.csi.group']

    public final static String DATE_FORMAT_STRING = 'dd.mm.yyyy';
    public final static int MONDAY_WEEKSTART = 1

    def show(DetailAnalysisDashboardCommand cmd) {
        Map<String, Object> modelToRender = constructStaticViewDataOfShowAll();

        cmd.copyRequestDataToViewModelMap(modelToRender);

        if (!ControllerUtils.isEmptyRequest(params)) {
            if (!cmd.validate()) {
                modelToRender.put('command', cmd)
            } else {

                String osmUrl = grailsLinkGenerator.getServerBaseURL()
                def errorList = []
                if (!osmUrl){
                    errorList << message(code: 'default.serverUrl.undefined', args: [message(code: 'default.serverUrl.undefined', default: 'The server url is undefined. You can set it in the custom osm-properties.\n')])
                }
                String microServiceUrl = grailsApplication.config.getProperty('grails.de.iteratec.osm.assetRequests.microserviceUrl')
                if (!microServiceUrl){
                    errorList << message(code: 'default.microService.osmDetailAnalysis.url.undefined', args: [message(code: 'default.microService.osmDetailAnalysis.url.undefined', default: 'The url for the OsmDetailAnalysis micro service is undefined. You can set it in the custom osm-properties.\n')])
                }
                String apiKey = MicroServiceApiKey.findByMicroService("OsmDetailAnalysis").secretKey
                if (!apiKey){
                    errorList << message(code: 'default.microService.osmDetailAnalysis.apiKey.undefined', args: [message(code: 'default.microService.osmDetailAnalysis.apiKey.undefined', default: 'The api key for the OsmDetailAnalysis micro service is undefined. You can set it in the custom osm-properties.\n')])
                }
                if (osmUrl &&  microServiceUrl && apiKey ) {
                    try {

                        if (osmUrl.endsWith("/")) osmUrl = osmUrl.substring(0, osmUrl.length() - 1)
                        String detailDataWebPageAsString = new URL(microServiceUrl + "detailAnalysisDashboard/show" + "?apiKey=" + apiKey + "&osmUrl=" + osmUrl + "&" + request.queryString).getText()
                        modelToRender.put("osmDetailAnalysisRequest", detailDataWebPageAsString)
                    } catch (InvocationTargetException ex) {
                        errorList << message(code: 'default.microService.osmDetailAnalysis.apiKey.undefined', args: [message(code: 'default.microService.osmDetailAnalysis.unreachable', default: 'Microservice unreachable\n')])
                    } catch (ConnectException ex) {
                        errorList << message(code: 'default.microService.osmDetailAnalysis.apiKey.undefined', args: [message(code: 'default.microService.osmDetailAnalysis.unreachable', default: 'Microservice unreachable\n')])
                    }
                }
                modelToRender.put("errorList", errorList)

            }
        }

        return modelToRender
    }

    public Map<String, Object> constructStaticViewDataOfShowAll() {
        Map<String, Object> result = [:]

        // AggregatorTypes
        result.put('aggrGroupLabels', AGGREGATOR_GROUP_LABELS)
        result.put('aggrGroupValuesCached', AGGREGATOR_GROUP_VALUES.get(CachedView.CACHED))
        result.put('aggrGroupValuesUnCached', AGGREGATOR_GROUP_VALUES.get(CachedView.UNCACHED))

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
        result.put("dateFormat", DATE_FORMAT_STRING)
        result.put("weekStart", MONDAY_WEEKSTART)

        // --- Map<PageID, Set<MeasuredEventID>> for fast view filtering:
        Map<Long, Set<Long>> eventsOfPages = new HashMap<Long, Set<Long>>()
        for (Page eachPage : pages) {
            Set<Long> eventIds = new HashSet<Long>();

            Collection<Long> ids = measuredEvents.findResults {
                it.testedPage.getId() == eachPage.getId() ? it.getId() : null
            }
            if (!ids.isEmpty()) {
                eventIds.addAll(ids);
            }

            eventsOfPages.put(eachPage.getId(), eventIds);
        }
        result.put('eventsOfPages', eventsOfPages);

        // --- Map<BrowserID, Set<LocationID>> for fast view filtering:
        Map<Long, Set<Long>> locationsOfBrowsers = new HashMap<Long, Set<Long>>()
        for (Browser eachBrowser : browsers) {
            Set<Long> locationIds = new HashSet<Long>();

            Collection<Long> ids = locations.findResults {
                it.browser.getId() == eachBrowser.getId() ? it.getId() : null
            }
            if (!ids.isEmpty()) {
                locationIds.addAll(ids);
            }

            locationsOfBrowsers.put(eachBrowser.getId(), locationIds);
        }
        result.put('locationsOfBrowsers', locationsOfBrowsers);

        result.put("tagToJobGroupNameMap", jobGroupDaoService.getTagToJobGroupNameMap())

        // Done! :)
        return result;
    }

    RESTClient getRestClient(String url) {
        return new RESTClient(url)
    }
}
