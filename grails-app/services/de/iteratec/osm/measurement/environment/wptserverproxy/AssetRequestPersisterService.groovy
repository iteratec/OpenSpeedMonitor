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
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasuredEvent
import grails.web.mapping.LinkGenerator
import groovyx.net.http.RESTClient

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.XML

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
        if (!persistenceOfAssetRequestsEnabled)
            return

        final String jobLabel = resultXml.getLabel()
        Job job = jobDaoService.getJob(jobLabel)
        if (!job) {
            throw new OsmResultPersistanceException("Can't trigger persistence of assetRequests for TestID: " + resultXml.getTestId() +
                    "\n Job with name " + jobLabel + "doesn't exist")
        }
        Long jobId = job.id
        Long jobGroupId = job.jobGroup.id

        if(!JobGroup.get(jobGroupId).persistDetailData)
            return

        RESTClient client = new RESTClient(microserviceUrl)
        String osmUrl = grailsLinkGenerator.getServerBaseURL()
        if (osmUrl.endsWith("/")) osmUrl = osmUrl.substring(0, osmUrl.length() - 1)
        String apiKey = MicroServiceApiKey.findByMicroService("OsmDetailAnalysis").secretKey
        String wptVersion = "2.19"
        List<String> wptTestIds = [resultXml.getTestId()]
        String wptServerBaseUrl = wptServerOfResult.getBaseUrl()

        def resp
        int attempts = 0

        while ((!resp || resp.status != 200) && attempts < MAX_ATTEMPTS) {
            attempts++
            try {
                resp = client.post(path: 'restApi/persistAssetsForWptResult',
                        body: [osmUrl: osmUrl, jobId: jobId, wptVersion: wptVersion, wptTestId: wptTestIds, wptServerBaseUrl: wptServerBaseUrl, jobGroupId: jobGroupId, apiKey: apiKey],
                        requestContentType: URLENC)
            } catch (ConnectException) {
                sleep(1000 * TIMEOUT_IN_SECONDS)
            }

        }

        if (!resp || resp.status != 200)
            throw new OsmResultPersistanceException("Can't trigger persistence of assetRequests for TestID: " + resultXml.getTestId())
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
