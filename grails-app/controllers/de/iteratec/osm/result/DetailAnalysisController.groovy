package de.iteratec.osm.result

import de.iteratec.osm.ConfigService
import de.iteratec.osm.measurement.environment.wptserverproxy.DetailAnalysisPersisterService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.util.ControllerUtils
import grails.web.mapping.LinkGenerator
import org.joda.time.Interval

import javax.xml.ws.http.HTTPException
import java.lang.reflect.InvocationTargetException

class DetailAnalysisController {
    JobGroupDaoService jobGroupDaoService
    LinkGenerator grailsLinkGenerator
    EventResultDashboardService eventResultDashboardService
    DetailAnalysisPersisterService detailAnalysisPersisterService
    ConfigService configService

    public final static String JS_DATE_FORMAT = 'dd.mm.yyyy'

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
        String microServiceUrl = configService.getDetailAnalysisUrl()
        if (!microServiceUrl) {
            errorList << message(code: 'default.microService.osmDetailAnalysis.url.undefined', args: [message(code: 'default.microService.osmDetailAnalysis.url.undefined', default: 'The url for the OsmDetailAnalysis micro service is undefined. You can set it in the custom osm-properties.\n')])
        }

        String apiKey = configService.getDetailAnalysisApiKey()
        if (!apiKey) {
            errorList << message(code: 'default.microService.osmDetailAnalysis.apiKey.undefined', args: [message(code: 'default.microService.osmDetailAnalysis.apiKey.undefined', default: 'The api key for the OsmDetailAnalysis micro service is undefined. You can set it in the custom osm-properties.\n')])
        }
        if (osmUrl && microServiceUrl && apiKey) {
            try {
                if (osmUrl.endsWith("/")) osmUrl = osmUrl.substring(0, osmUrl.length() - 1)
                String queryString = "?apiKey=${apiKey}&osmUrl=${osmUrl}&" + request.queryString
                def detailDataWebPageAsString = (microServiceUrl + "detailAnalysisDashboard/show" + queryString).toURL().openConnection().with { conn ->
                    if (responseCode != 200) {
                        throw new HTTPException(responseCode)
                    }
                    conn.content.withReader { r ->
                        r.text
                    }
                }
                modelToRender.put("osmDetailAnalysisRequest", detailDataWebPageAsString)
            } catch (HTTPException ex) {
                if (ex.statusCode == 403) {
                    errorList << message(code: 'de.iteratec.detailAnalysis.notAllowed', default: 'not allowed.')
                } else {
                    errorList << ex.statusCode
                }
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
        boolean detailAnalysisEnabled = grailsApplication.config.getProperty('grails.de.iteratec.osm.detailAnalysis.enablePersistenceOfDetailAnalysisData') == 'true'
        return[
            'folders': eventResultDashboardService.getAllJobGroups(),
            'pages': eventResultDashboardService.getAllPages(),
            'dateFormat': JS_DATE_FORMAT,
            'tagToJobGroupNameMap': jobGroupDaoService.getTagToJobGroupNameMap(),
            'persistenceOfDetailAnalysisDataEnabled': detailAnalysisEnabled
        ]
    }

    def sendFetchAssetsAsBatchCommand(DetailAnalysisDashboardShowCommand cmd) {
        Map<String, Object> modelToRender = constructStaticViewDataOfShowAll();

        cmd.copyRequestDataToViewModelMap(modelToRender);

        Interval timeFrame = cmd.createTimeFrameInterval()
        def jobGroupList = []
        cmd.selectedFolder.each {
            jobGroupList.add(JobGroup.findById(it).name)
        }

        List<JobResult> jobResults = JobResult.createCriteria().list {
            if (cmd.selectedFolder) inList("jobGroupName", jobGroupList)
            between("date", timeFrame.getStart().toDate(), timeFrame.getEnd().toDate())
        }
        def batchIsQueued = detailAnalysisPersisterService.sendFetchAssetsAsBatchCommand(jobResults)
        modelToRender.put("startedBatchActivity", batchIsQueued)
        render(view: "show", model: modelToRender)
    }

}
