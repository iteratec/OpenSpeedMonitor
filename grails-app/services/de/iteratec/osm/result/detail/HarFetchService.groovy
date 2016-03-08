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

package de.iteratec.osm.result.detail

import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.measurement.environment.wptserverproxy.HttpRequestService
import de.iteratec.osm.persistence.HarPersistenceService
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult

/**
 * HarFetchService
 * This Service provides functionallity to fetch the Detail data for every JobResult.
 * Use the add...ToQueue to add results. They will be processed FIFO.
 */
class HarFetchService {

    HttpRequestService httpRequestService
    HarPersistenceService harPersistenceService
    ConfigService configService
    int maximumTries = 5
    int waitTime = 5000

    static transactional = false
    /**
     * Find the newest x JobResults without a persisted HAR and fetches the HAR Data to persist it
     * @param max maximum amount of JobResults to search
     */
    public void addNextNotPersistedJobResultsToQueue(int max) {
        List jobResultIds = JobResult.createCriteria().list(max: max) {
            eq("harStatus", HARStatus.NOT_PERSISTED)
            order("date", "desc")
            projections {
                property("id")
            }
        } as List
        addJobResultToQueue(jobResultIds)
    }

    /**
     * Adds a list of JobResult to the queue, so the HAR data for these ids will be fetched soon.
     * @param ids
     */
    public void addJobResultToQueue(Collection ids){
        new HARJob(jobResultIDs: ids).save(failOnError: true)
    }

    /**
     * Adds a single JobResult to the queue, so the HAR data for this id will be fetched soon.
     * This will only happen, if fetching is enabled
     * @param id
     */
    public void addJobResultToQueue(long id){
        if(configService.isDetailFetchingEnabled()){
            new HARJob(jobResultIDs: [id]).save(failOnError: true)
        }
    }

    /**
     * If there is at least one Job in our Queue, we will fetch them
     */
    public void fetch(){
        HARJob harJob = getNextJob()
        while(harJob){
            log.debug("HARJob with ${harJob.jobResultIDs.size()} Results found")
            fetchHarsFromJobResultIDs(harJob)
            if(harJob.jobResultIDs.isEmpty()){
                harJob.delete(failOnError:true, flush: true)
                log.debug("HARJob finished")
            }
            harJob = getNextJob()
        }
    }

    /**
     * Finds the next HARJob
     * @return HARJob if one exists or null if not
     */
    private static HARJob getNextJob(){
        if(HARJob.count()>0){
            return HARJob.list(sort: "created", max: 1, order: "asc").get(0)
        } else{
            return null
        }
    }

    /**
     * Fetches all HAR from JobResults located between the given dates
     *
     * @param from
     * @param to
     */
    public void addHarsBetweenToQueue(Date from, Date to){
        def jobResultIds = getJobResultsFrom(from, to)
        addJobResultToQueue(jobResultIds)
    }

    public List getJobResultsFrom(Date from, Date to){
        JobResult.createCriteria().list() {
            eq("harStatus", HARStatus.NOT_PERSISTED)
            gte("date", from)
            lte("date", to)
            order("date", "desc")
            projections {
                property("id")
            }
        } as List
    }

    /**
     * Fetches all har to the JobResultIds. You can optionally pass a Closure, which will be called after every JobResult fetch.
     * The ID will be passed to this closure
     * @param jobResultIds
     * @param c
     */
    public void fetchHarsFromJobResultIDs(HARJob harjob){
        def ids = harjob.getJobResultIDs()
        //we delete every id, after we have the har data. Because of that we use a while loop,
        //so we can remove elements while iterating
        while (!ids.isEmpty()){
            long id = ids.first()
            JobResult jobResult = JobResult.findById(id)
            //All eventResults from a jobResult are pointing to the same testDetailsWaterfallURL,
            //so we just need the first eventResult
            if(jobResult.harStatus != HARStatus.PERSISTED){
                //Since we can run a job with takes a lot of time to fetch all jobs, it could happen
                //that someone adds another job, with contains some of this JobResults.
                //To prevent the persistence of multiply HARs for the same JobResults, we check right
                //before if this job ist not already persisted
                EventResult eventResult = EventResult.findByJobResult(jobResult)
                harPersistenceService.saveHARDataForJobResults(jobResult, fetchHarFromWPTInstance(eventResult?.testDetailsWaterfallURL))
            }
            harjob.getJobResultIDs().remove(id)
            harjob.save(failOnError: true)
        }
    }

    /**
     * Fetches the HAR from the given detail page.
     * If testDetailsWaterfallURL is null, this method will return null
     * @param testDetailsWaterfallURL
     * @return HAR from local Database given server, if it's not already fetched
     */
    private Map fetchHarFromWPTInstance(URL testDetailsWaterfallURL) {
        if(!testDetailsWaterfallURL) return null
        URL hARURL = getHARURL(testDetailsWaterfallURL)
        String baseUrl = "${hARURL.getProtocol()}://${hARURL.getHost()}"
        String path = hARURL.getPath()
        println baseUrl+path
        Map query = httpRequestService.splitQueryStringToMap(hARURL.getQuery())
        println query
        return getResponse(baseUrl, path, query)
    }

    /**
     * Get the HAR Archive fom the WPT-Server, if something went wrong, we will try a maximum of maximumTries.
     * After each try we will wait.
     * If we don't get a useful response after the maximum of tries, this method will return null
     * @param baseUrl
     * @param path
     * @param query
     * @param tries
     * @return
     */
    private Map getResponse(String baseUrl, String path, Map query, int tries = 1){
        //It could happen that we can't connect to Server because of several reasons.
        //If that happens, we will just wait a moment and start another try.
        //The maximum amount of tries and the waiting time can be configured
        try {
            return httpRequestService.getJsonResponse(baseUrl, path, query) as Map
        }catch(Exception e){
            log.debug("Exceptions while getting HAR: ${e}")
            if(tries<=maximumTries){
                log.debug("Tries left: ${maximumTries-tries}, wait $waitTime and try again")
                sleep(waitTime)
                return getResponse(baseUrl, path, query, tries+1)
            } else{
                log.debug("No tries left for $baseUrl$path $query")
                return null
            }
        }
    }

    /**
     * Creates a URL to the HAR location, from a given test URL
     * @param testDetailsWaterfallURL
     * @return
     */
    private static URL getHARURL(URL testDetailsWaterfallURL) {
        String harURL = testDetailsWaterfallURL.toString()
        harURL = harURL.replace("/details.php", "/export.php")
        int andIndex = harURL.indexOf("&")
        if (andIndex > 0) {
            harURL = harURL.substring(0, andIndex)
        }
        return new URL(harURL)
    }

}
