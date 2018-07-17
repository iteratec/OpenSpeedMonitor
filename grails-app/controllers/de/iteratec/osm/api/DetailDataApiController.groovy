package de.iteratec.osm.api

import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdater
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupService
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.util.ControllerUtils
import grails.converters.JSON
import grails.databinding.BindUsing
import io.swagger.annotations.*
import org.springframework.http.HttpStatus

@Api(value = '/rest', tags = ["Detail Data Service"], description = "Api for Detail Data Service communication.", position = 4)
class DetailDataApiController {

    JobGroupService jobGroupService
    JobDaoService jobDaoService
    BatchActivityService batchActivityService

    /**
     * Returns the names for given ids for each requested domain
     * @param mappingRequestCmd
     * @return
     */
    @ApiOperation(
            value = "Returns the names for given ids for each requested domain.",
            nickname = "domain/namesForIds/{requestedDomains}",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(
                    code = 400,
                    message = 'Just Job and JobGroup are allowed domains.'
            )
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "requestedDomains",
                    paramType = "path",
                    required = true,
                    value = "JSON representation of the id to name map, e.g. <pre>{JobGroup: [1,2,3,4], Job: [997]}</pre>",
                    dataType = "grails.converters.JSON"
            )
    ])
    public Map<String, Object> getNamesForIds(MappingRequestCommand mappingRequestCmd) {
        Map<String, Map<Long, String>> resultMappings = [:].withDefault { [:] }

        mappingRequestCmd.requestedDomains.each { domain, idList ->
            switch (domain) {
                case "JobGroup":
                    List<JobGroup> jobGroups = JobGroup.findAllByIdInList(idList)
                    resultMappings[domain] = [:]
                    jobGroups.each {
                        resultMappings[domain].put(it.id, it.name)
                    }
                    break;
                case "Job":
                    List<Job> jobs = jobDaoService.getJobsByIds(idList)
                    resultMappings[domain] = [:]
                    jobs.each {
                        resultMappings[domain].put(it.id, it.label)
                    }
                    break;
                default:
                    return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, "Request not allowed or domain does not exist: ${domain}")
            }
        }
        return ControllerUtils.sendObjectAsJSON(response, [target: resultMappings as HashMap], params.pretty && params.pretty == 'true')
    }

    /**
     * Returns the ids for given names for each requested domain
     * @param mappingRequestCmd
     * @return
     */
    @ApiOperation(
            value = "Returns the ids for given names for each requested domain.",
            nickname = "domain/idsForNames/{requestedDomains}",
            produces = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(
                    code = 400,
                    message = 'Only Browser, Location, Page or MeasuredEvent are allowed domains.'
            )
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "requestedDomains",
                    paramType = "path",
                    required = true,
                    value = "JSON representation of the name to id map, e.g. <pre>{Browser: [Firefox, Chrome], Page: [HP_entry]}</pre>",
                    dataType = "grails.converters.JSON"
            )
    ])
    public Map<String, Object> getIdsForNames(MappingRequestCommand mappingRequestCmd) {
        Map<String, Map<Long, String>> resultMappings = [:].withDefault { [:] }

        mappingRequestCmd.requestedDomains.each { domain, names ->
            switch (domain) {
                case "Browser":
                    List<Browser> browsers = Browser.findAllByNameInList(names)
                    resultMappings[domain] = [:]
                    browsers.each {
                        resultMappings[domain].put(it.id, it.name)
                    }
                    break;
                case "Location":
                    List<Location> locations = Location.findAllByUniqueIdentifierForServerInList(names)
                    resultMappings[domain] = [:]
                    locations.each {
                        resultMappings[domain].put(it.id, it.uniqueIdentifierForServer)
                    }
                    break;
                case "Page":
                    List<Page> pages = Page.findAllByNameInList(names)
                    resultMappings[domain] = [:]
                    pages.each {
                        resultMappings[domain].put(it.id, it.name)
                    }
                    break;
                case "MeasuredEvent":
                    List<MeasuredEvent> measuredEvents = MeasuredEvent.findAllByNameInList(names)
                    resultMappings[domain] = [:]
                    measuredEvents.each {
                        resultMappings[domain].put(it.id, it.name)
                    }
                    break;
                default:
                    return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, "Request not allowed or domain does not exist: ${domain}")
            }
        }
        return ControllerUtils.sendObjectAsJSON(response, [target: resultMappings as HashMap], params.pretty && params.pretty == 'true')
    }

    @ApiOperation(
            value = "Updates the BatchActivity for a Batch Job in Detail Data service Loading assets from WebPagetest.",
            nickname = "receiveCallback",
            produces = "application/json",
            httpMethod = "POST"
    )
    @ApiResponses([
            @ApiResponse(
                    code = 400,
                    message = 'Some of the params are missing or invalid.'
            ),
            @ApiResponse(
                    code = 404,
                    message = 'No BatchActivity exists with given id.'
            )
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "callBackId",
                    paramType = "query",
                    required = true,
                    value = "Id of BatchActivity to update status for.",
                    dataType = "int"
            ),
            @ApiImplicitParam(
                    name = "failureCount",
                    paramType = "query",
                    required = true,
                    value = "New Failure count of BatchActivity. Will be set.",
                    dataType = "int"
            ),
            @ApiImplicitParam(
                    name = "loadedAssets",
                    paramType = "query",
                    required = true,
                    value = "New progress of BatchActivity. Will be set.",
                    dataType = "int"
            ),
            @ApiImplicitParam(
                    name = "countAssets",
                    paramType = "query",
                    required = true,
                    value = "Absolute amount of assets to be loaded for the BatchActivity assigned Detail Data Batch Job. If loadedAssets equals this value BatchActivity gets finished.",
                    dataType = "int"
            )
    ])
    public Map<String, Object> receiveCallback(CallbackCommand cmd){
        if (!cmd.validate()){
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, "Some of the params are missing or invalid.")
        }
        BatchActivityUpdater batchActivity = batchActivityService.getActiveBatchActivity(cmd.callBackId)
        if (!batchActivity){
            return ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "No BatchActivity exists with id ${cmd.callBackId}.")
        }
        if(cmd.failureCount > 0 ) {
            int newFailures = cmd.failureCount - batchActivity.getCurrentFailures()
            if(newFailures){
                batchActivity.addFailures("${cmd.failureCount} FetchJobs failed",newFailures)
            }
        }
        if (cmd.loadedAssets > 0){
            int currentProgress = batchActivity.getCurrentProgress()
            batchActivity.addProgressToStage(cmd.loadedAssets-currentProgress)
            if(cmd.loadedAssets == cmd.countAssets) batchActivity.done()
        }
        return ControllerUtils.sendObjectAsJSON(response, [target: 'ok'], params.pretty && params.pretty == 'true')
    }
}

/**
 * The command object for the rest methods
 * /rest/domain/idsForNames
 * /rest/domain/namesForIds
 */
class MappingRequestCommand {
    @BindUsing({ obj, source ->
        if (source['requestedDomains'] instanceof HashMap) {
            obj = source['requestedDomains']
        } else {
            obj = JSON.parse(source['requestedDomains'])
        }
    })
    Map<String, List> requestedDomains = [:].withDefault { [] }
}

class CallbackCommand {
    int countAssets
    int loadedAssets
    int callBackId
    int failureCount
    static constraints = {
        countAssets(min: 0)
        loadedAssets(min: 0)
        callBackId(min: 1)
        failureCount(min: 0)
    }
}
