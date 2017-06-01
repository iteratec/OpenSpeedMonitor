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

package de.iteratec.osm.csi

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.weighting.WeightingService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.*
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import grails.transaction.Transactional
import org.joda.time.DateTime

@Transactional
class EventCsiAggregationService {

    PerformanceLoggingService performanceLoggingService
    JobService jobService
    OsmConfigCacheService osmConfigCacheService
    CsiAggregationDaoService csiAggregationDaoService
    WeightingService weightingService
    CsiAggregationUpdateEventDaoService csiAggregationUpdateEventDaoService
    CsiValueService csiValueService

    /**
     * Just gets CsiAggregations from DB. No creation or calculation.
     * @param fromDate
     * @param toDate
     * @param targetInterval
     * @return
     */
    List<CsiAggregation> findAll(Date fromDate, Date toDate, CsiAggregationInterval targetInterval, ConnectivityProfile connProfile = null) {
        def query
        AggregatorType measuredEventAggregator = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)

        if (connProfile == null) {
            query = CsiAggregation.where {
                started >= fromDate
                started <= toDate
                interval == targetInterval
                aggregator == measuredEventAggregator
            }
        } else {
            query = CsiAggregation.where {
                started >= fromDate
                started <= toDate
                interval == targetInterval
                aggregator == measuredEventAggregator
                connectivityProfile == connProfile
            }
        }
        return query.list()
    }

    /**
     * Calculates or recalculates hourly-job {@link CsiAggregation}s which depend from param newResult.
     * @param newResult
     */
    void createOrUpdateHourlyValue(DateTime hourlyStart, EventResult newResult) {
        AggregatorType eventAggregator = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)
        CsiAggregation hmv = ensurePresence(
                hourlyStart,
                CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY),
                newResult.jobGroup,
                newResult.measuredEvent,
                newResult.page,
                newResult.browser,
                newResult.location,
                eventAggregator,
                true,
                newResult.connectivityProfile)
        calcCsiAggregationForJobAggregatorWithoutQueryResultsFromDb(hmv, newResult)

    }

    /**
     * Provides all hourly event-{@link CsiAggregation}s between toDate and fromDate for query-params jobs mvQueryParams.
     * Non-existent {@link CsiAggregation}s will NOT be created and/or calculated. That happens exclusively on arrival of {@link EventResult}s in backgound.
     * @param fromDate
     * @param toDate
     * @param mvQueryParams
     * 				Contains all parameters necessary for querying {@link CsiAggregation}s from db.
     * @return
     */
    List<CsiAggregation> getHourlyCsiAggregations(Date fromDate, Date toDate, MvQueryParams mvQueryParams) {
        List<CsiAggregation> calculatedMvs = []
        if (fromDate > toDate) {
            throw new IllegalArgumentException("toDate must not be later than fromDate: fromDate=${fromDate}; toDate=${toDate}")
        }

        DateTime toDateTime = new DateTime(toDate)
        DateTime fromDateTime = new DateTime(fromDate)

        calculatedMvs.addAll(getAllCalculatedHourlyCas(mvQueryParams, fromDateTime, toDateTime))
        return calculatedMvs
    }

    private getAllCalculatedHourlyCas(MvQueryParams mvQueryParams, DateTime fromDateTime, DateTime toDateTimeEndOfInterval) {
        def result = []
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting csi-results - getAllCalculatedHourlyCas - getMvs', 1) {
            result = csiAggregationDaoService.getMvs(
                    fromDateTime.toDate(),
                    toDateTimeEndOfInterval.toDate(),
                    mvQueryParams,
                    CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY),
                    AggregatorType.findByName(AggregatorType.MEASURED_EVENT))
        }
        return result
    }

    private CsiAggregation ensurePresence(DateTime startDate, CsiAggregationInterval interval, JobGroup jobGroup, MeasuredEvent measuredEvent, Page page, Browser browser, Location location, AggregatorType eventAggregator, boolean initiallyClosed, ConnectivityProfile connectivityProfile) {
        CsiAggregation toCreateAndOrCalculate
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, "EventCsiAggregationService: ensurePresence.findByStarted", 4) {
            toCreateAndOrCalculate = CsiAggregation.findByStartedAndIntervalAndAggregatorAndJobGroupAndMeasuredEventAndPageAndBrowserAndLocationAndConnectivityProfile(
                    startDate.toDate(), interval, eventAggregator, jobGroup, measuredEvent, page, browser, location, connectivityProfile)
            log.debug("CsiAggregation.findByStartedAndIntervalAndAggregatorAndJobGroupAndMeasuredEventAndPageAndBrowserAndLocationAndConnectivityProfile delivered ${toCreateAndOrCalculate ? 'a' : 'no'} result")
        }
        if (!toCreateAndOrCalculate) {
            toCreateAndOrCalculate = new CsiAggregation(
                    started: startDate.toDate(),
                    interval: interval,
                    aggregator: eventAggregator,
                    jobGroup: jobGroup,
                    measuredEvent: measuredEvent,
                    page: page,
                    browser: browser,
                    location: location,
                    csByWptDocCompleteInPercent: null,
                    csByWptVisuallyCompleteInPercent: null,
                    underlyingEventResultsByWptDocComplete: '',
                    closedAndCalculated: initiallyClosed,
                    connectivityProfile: connectivityProfile
            ).save(failOnError: true)
        }
        return toCreateAndOrCalculate
    }
    /**
     * Re-calculates {@link CsiAggregation} toBeCalculated cause data-basis changed with new {@link EventResult} newResult.
     * @param toBeCalculated
     * @param newResult
     * @return
     */
    private CsiAggregation calcCsiAggregationForJobAggregatorWithoutQueryResultsFromDb(CsiAggregation toBeCalculated, EventResult newResult) {
        Integer countUnderlyingEventResultsByWptDocComplete = toBeCalculated.countUnderlyingEventResultsByWptDocComplete()
        Integer countUnderlyingEventResultsByWptVisuallyComplete = toBeCalculated.underlyingEventResultsByVisuallyComplete.size()
        Double newCsByWptDocCompleteInPercent
        Double newCsByWptVisuallyCompleteInPercent
        if (csiValueService.isCsiRelevant(newResult)) {
            // add value for csByDocComplete
            if (!toBeCalculated.containsInUnderlyingEventResultsByWptDocComplete(newResult.ident())) {
                if (countUnderlyingEventResultsByWptDocComplete > 0 && newResult.csByWptDocCompleteInPercent != null) {
                    Double sumOfPreviousResults = (toBeCalculated.csByWptDocCompleteInPercent ?
                            toBeCalculated.csByWptDocCompleteInPercent :
                            0) * countUnderlyingEventResultsByWptDocComplete
                    newCsByWptDocCompleteInPercent = (sumOfPreviousResults + newResult.csByWptDocCompleteInPercent) / (countUnderlyingEventResultsByWptDocComplete + 1)
                } else if (countUnderlyingEventResultsByWptDocComplete == 0) {
                    newCsByWptDocCompleteInPercent = newResult.csByWptDocCompleteInPercent
                }
                toBeCalculated.csByWptDocCompleteInPercent = newCsByWptDocCompleteInPercent
                toBeCalculated.addToUnderlyingEventResultsByWptDocComplete(newResult.ident())
            }

            //add value for csByVisuallyComplete
            if (!toBeCalculated.underlyingEventResultsByVisuallyComplete.contains(newResult)) {
                if (countUnderlyingEventResultsByWptVisuallyComplete > 0 && newResult.csByWptVisuallyCompleteInPercent != null) {
                    Double sumOfPreviousResults = (toBeCalculated.csByWptVisuallyCompleteInPercent ?
                            toBeCalculated.csByWptVisuallyCompleteInPercent :
                            0) * countUnderlyingEventResultsByWptVisuallyComplete
                    newCsByWptVisuallyCompleteInPercent = (sumOfPreviousResults + newResult.csByWptVisuallyCompleteInPercent) / (countUnderlyingEventResultsByWptVisuallyComplete + 1)
                } else if (countUnderlyingEventResultsByWptVisuallyComplete == 0) {
                    newCsByWptVisuallyCompleteInPercent = newResult.csByWptVisuallyCompleteInPercent
                }
                toBeCalculated.csByWptVisuallyCompleteInPercent = newCsByWptVisuallyCompleteInPercent
                toBeCalculated.underlyingEventResultsByVisuallyComplete.add(newResult)
            }
        }
        toBeCalculated.save(failOnError: true)
        csiAggregationUpdateEventDaoService.createUpdateEvent(toBeCalculated.ident(), CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        return toBeCalculated
    }

    /**
     * Returns a list of hourlyCsiAggregations
     * @param fromDate the start date
     * @param toDate the end date
     * @param jobGroup the jobGroup
     * @param page the page
     * @return a list of csiAggregations or an empty list
     */
    List<CsiAggregation> getHourlyCsiAggregations(Date fromDate, Date toDate, List<JobGroup> jobGroups, List<Page> pages) {
        if (fromDate > toDate) {
            throw new IllegalArgumentException("toDate must not be later than fromDate: fromDate=${fromDate}; toDate=${toDate}")
        }

        CsiAggregationInterval interval = CsiAggregationInterval.findByIntervalInMinutes(CsiAggregationInterval.HOURLY)
        AggregatorType aggregatorType = AggregatorType.findByName(AggregatorType.MEASURED_EVENT)

        return CsiAggregation.createCriteria().list {
            eq('interval', interval)
            eq('aggregator', aggregatorType)
            between('started', fromDate, toDate)
            jobGroup {
                'in'('id', jobGroups*.id)
            }
            page {
                'in'('id', pages*.id)
            }
        }

    }
}
