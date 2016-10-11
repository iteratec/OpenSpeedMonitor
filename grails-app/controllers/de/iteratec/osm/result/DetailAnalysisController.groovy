package de.iteratec.osm.result

import de.iteratec.osm.api.MicroServiceApiKey
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import grails.web.mapping.LinkGenerator
import groovyx.net.http.RESTClient

import java.lang.reflect.InvocationTargetException

class DetailAnalysisController {
    JobGroupDaoService jobGroupDaoService
    LinkGenerator grailsLinkGenerator

    public final static String DATE_FORMAT_STRING = 'dd.mm.yyyy';

    def show() {
        Map modelToRender = [:]

        String osmUrl = grailsLinkGenerator.getServerBaseURL()
        def errorList = []

        if (!osmUrl) {
            errorList << message(code: 'default.serverUrl.undefined', args: [message(code: 'default.serverUrl.undefined', default: 'The server url is undefined. You can set it in the custom osm-properties.\n')])
        }
        String microServiceUrl = grailsApplication.config.getProperty('grails.de.iteratec.osm.assetRequests.microserviceUrl')
        microServiceUrl = microServiceUrl.endsWith("/")?microServiceUrl: microServiceUrl + "/"
        if (!microServiceUrl) {
            errorList << message(code: 'default.microService.osmDetailAnalysis.url.undefined', args: [message(code: 'default.microService.osmDetailAnalysis.url.undefined', default: 'The url for the OsmDetailAnalysis micro service is undefined. You can set it in the custom osm-properties.\n')])
        }
        String apiKey = MicroServiceApiKey.findByMicroService("OsmDetailAnalysis").secretKey
        if (!apiKey) {
            errorList << message(code: 'default.microService.osmDetailAnalysis.apiKey.undefined', args: [message(code: 'default.microService.osmDetailAnalysis.apiKey.undefined', default: 'The api key for the OsmDetailAnalysis micro service is undefined. You can set it in the custom osm-properties.\n')])
        }
        if (osmUrl && microServiceUrl && apiKey) {
            try {

                if (osmUrl.endsWith("/")) osmUrl = osmUrl.substring(0, osmUrl.length() - 1)
                String detailDataWebPageAsString = new URL(microServiceUrl + "detailAnalysisDashboard/show" + "?apiKey=" + apiKey + "&osmUrl=" + osmUrl + "&" + request.queryString).getText()
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

    RESTClient getRestClient(String url) {
        return new RESTClient(url)
    }
}
