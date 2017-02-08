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

package de.iteratec.osm.measurement.environment.wptserverproxy

import de.iteratec.osm.api.MicroServiceApiKey
import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdater

import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.WptXmlResultVersion
import grails.web.mapping.LinkGenerator
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

import java.lang.reflect.InvocationTargetException

import static groovyx.net.http.ContentType.URLENC

/**
 * Persists locations and results. Observer of ProxyService.
 * @author rschuett , nkuhn
 * grails-app/services/de/iteratec/ispc/ResultPersisterService.groovy
 */
class AssetRequestPersisterService implements iResultListener {

    private boolean persistenceOfAssetRequestsEnabled = false
    private String microserviceUrl
    private boolean callListenerAsync = true

    private final int MAX_ATTEMPTS = 3
    private final int TIMEOUT_IN_SECONDS = 5

    BatchActivityService batchActivityService
    LinkGenerator grailsLinkGenerator
    JobDaoService jobDaoService

    /**
     * Persisting fetched {@link EventResult}s. If associated JobResults and/or Jobs and/or Locations don't exist they will be persisted, too.
     * Dependent {@link de.iteratec.osm.report.chart.CsiAggregation}s will be created/marked/calculated.
     * Persisted {@link EventResult} will be reported to graphite if configured respectively.
     * <br><b>Note:</b> Persistance of the {@link EventResult}s of one test step (i.e. for one {@link MeasuredEvent}) is wrapped into a transaction. So ANY other downstream operations may not rollback the persistance
     * of the {@link EventResult}s
     */
    @Override
    String getListenerName() {
        return "AssetRequestPersisterService"
    }

    @Override
    public void listenToResult(
            WptResultXml resultXml,
            WebPageTestServer wptserverOfResult) {

        try {
            persistAssetRequests(resultXml, wptserverOfResult)

        } catch (OsmResultPersistanceException e) {
            log.error(e.message, e)
        }

    }

    @Override
    boolean callListenerAsync() {
        return callListenerAsync
    }

    /**
     * Triggers the persistence the AssetRequests for a JobResults if persistence is enabled
     * @param resultXml
     */
    private void persistAssetRequests(WptResultXml resultXml, WebPageTestServer wptServerOfResult) {
        if (!persistenceOfAssetRequestsEnabled){
            log.debug("Can not send persistAssetRequests since persistenceOfAssetRequests is disabled")
            return
        }
        final String jobLabel = resultXml.getLabel()
        Job job = jobDaoService.getJob(jobLabel)
        if (!job) {
            throw new OsmResultPersistanceException("Can't trigger persistence of assetRequests for TestID: " + resultXml.getTestId() +
                    "\n Job with name " + jobLabel + "doesn't exist")
        }
        Long jobId = job.id
        Long jobGroupId = job.jobGroup.id

        // If persisting of detail data is not activated for jobGroup do nothing
        if(!JobGroup.get(jobGroupId).persistDetailData)
            return

        // persistence of detail data is only available for wpt-server > 2.19
        if(resultXml.version.toString() != WptXmlResultVersion.MULTISTEP.toString()) {
            log.debug("Persisctence of detail data not available for wpt-server with version ${wptVersion}")
            return
        }

        RESTClient client = new RESTClient(microserviceUrl)
        String osmUrl = grailsLinkGenerator.getServerBaseURL()
        if (osmUrl.endsWith("/")) osmUrl = osmUrl.substring(0, osmUrl.length() - 1)
        String apiKey = MicroServiceApiKey.findByMicroService("OsmDetailAnalysis").secretKey
        List<String> wptTestIds = [resultXml.getTestId()]
        String wptVersion = getWptVersion(resultXml)
        String wptServerBaseUrl = wptServerOfResult.getBaseUrl()

        def resp
        int attempts = 0

        while ((!resp || resp.status != 200) && attempts < MAX_ATTEMPTS) {
            attempts++
            try {
                resp = client.post(path: 'restApi/persistAssetsForWptResult',
                        body: [osmUrl: osmUrl, jobId: jobId, wptVersion: wptVersion, wptTestId: wptTestIds, wptServerBaseUrl: wptServerBaseUrl, jobGroupId: jobGroupId, apiKey: apiKey],
                        requestContentType: URLENC)
            } catch (ConnectException ex) {
                log.error("Couldn't queue persistAssetRequests", ex)
                sleep(1000 * TIMEOUT_IN_SECONDS)
            }

        }

        if (!resp || resp.status != 200)
            throw new OsmResultPersistanceException("Can't trigger persistence of assetRequests for TestID: " + resultXml.getTestId())
    }

    /**
     * Creates a OSM-DA conform representation of the wpt version
     * @param resultXml
     * @return String of the version
     */
    private String getWptVersion(WptResultXml resultXml){
        //The version is only noted since 2.19. So we for Versions < 2.19 we can
        //just tell that they are smaller then 2.19 and we just call them "undefined", since the DA only supports version >= 2.19
        resultXml.version == WptXmlResultVersion.MULTISTEP ? "2.19":"2.18"
        switch (resultXml.version){
            case WptXmlResultVersion.MULTISTEP:
                return "2.19"
            default:
                return "undefined"

        }
    }

    public boolean sendFetchAssetsAsBatchCommand(List<JobResult> jobResults) {
        if (!persistenceOfAssetRequestsEnabled){
            log.debug("Can not sendFetchAssetsAsBatchCommand since persistenceOfAssetRequests is disabled")
            return false
        }
        if ( !jobResults || jobResults.empty) {
            log.debug("Can not sendFetchAssetsAsBatchCommand since jobResultsList is emtpy")
            return false
        }
        def returnValue = false
        def persistanceJobList = []
        jobResults.each { JobResult jobResult ->
            persistanceJobList.add([wptVersion: "2.19",
                    jobId: jobResult.job.id,
                    wptTestId: jobResult.testId,
                    wptServerBaseUrl: jobResult.wptServerBaseurl,
                    jobGroupId: jobResult.job.jobGroup.id])
        }

        RESTClient client = new RESTClient(microserviceUrl)
        String osmUrl = grailsLinkGenerator.getServerBaseURL()
        if (osmUrl.endsWith("/")) osmUrl = osmUrl.substring(0, osmUrl.length() - 1)
        String apiKey = MicroServiceApiKey.findByMicroService("OsmDetailAnalysis").secretKey
        String callbackUrl = "rest/receiveCallback"

        def resp
        int attempts = 0
        while ((!resp || resp.status != 200) && attempts < MAX_ATTEMPTS) {
            attempts++
            BatchActivityUpdater batchActivity
            try {
                batchActivity = batchActivityService.createActiveBatchActivity(BatchActivity.class, Activity.UPDATE, "Postload Asset Job" ,1, true, 1)
                log.debug("Attempt ${attempts+1} to contact DetailDatenService: MicroserviceUrl=${microserviceUrl} OsmUrl=${osmUrl} CallbackUrl=${callbackUrl} CallbackJobId=${batchActivity.getBatchActivityId()} PersistanceJobList =${persistanceJobList}")
                resp = client.post(path: 'restApi/persistAssetsBatchJob',
                        body: [osmUrl: osmUrl, apiKey: apiKey, callbackUrl: callbackUrl, callbackJobId: batchActivity.getBatchActivityId(), persistanceJobList: persistanceJobList],
                contentType: ContentType.JSON)
                if(resp.data["target"]) {
                    batchActivity.beginNewStage("Update Stats",  resp.data["target"], 1)
                    returnValue=true
                }

            } catch (Exception ex) {
                log.error("Couldn't queue assetRequestBatch", ex)
                if(batchActivity)batchActivity.delete()
                sleep(1000 * TIMEOUT_IN_SECONDS)

            }
        }


//        if (!resp || resp.status != 200)
//            throw new OsmResultPersistanceException("Can't trigger persistence of assetRequests")
        return returnValue

    }


    public void enablePersistenceOfAssetRequestsForJobResults(String microserviceUrl) {
        this.microserviceUrl = microserviceUrl.endsWith('/') ? microserviceUrl : microserviceUrl + "/"
        this.persistenceOfAssetRequestsEnabled = true
    }

    public void disablePersitenceOfAssetRequestsForJobResults() {
        persistenceOfAssetRequestsEnabled = false
        microserviceUrl = null
    }
}
