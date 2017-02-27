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

package de.iteratec.osm.report.chart

import de.iteratec.osm.csi.CsiSystem
import de.iteratec.osm.csi.Page
import de.iteratec.osm.dao.CriteriaAggregator
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.util.PerformanceLoggingService
import org.joda.time.DateTime

/**
 * Contains only methods that query {@link CsiAggregation}s from database. Doesn't contain any dependencies to other domains or
 * service-logic.
 *
 * @author nkuhn
 *
 */
class CsiAggregationDaoService {

    CsiAggregationUtilService csiAggregationUtilService
    PerformanceLoggingService performanceLoggingService

    /**
     * <p>
     * Finds a {@linkplain CsiAggregation measured value} by its database id
     * if it exists.
     * </p>
     *
     * @param databaseId
     *         The database id to search for.
     * @return The found measured value or <code>null</code> if no
     *         corresponding measured value exists.
     */
    public CsiAggregation tryToFindById(long databaseId) {
        return CsiAggregation.get(databaseId);
    }

    /**
     * Gets all {@link CsiAggregation}s from db respective given arguments.
     *
     * @param fromDate
     * @param toDate
     * @param interval
     * @param aggregator
     * @param connectivityProfiles
     * @return
     */
    List<CsiAggregation> getMvs(
            Date fromDate,
            Date toDate,
            CsiAggregationInterval interval,
            AggregatorType aggregator,
            List<CsiSystem> csiSystems
    ) {
        toDate = fromDate == toDate ? toDate + interval.intervalInMinutes : toDate
        List<CsiAggregation> result = CsiAggregation.findAllByStartedBetweenAndStartedLessThanAndIntervalAndAggregator(fromDate, toDate, toDate, interval, aggregator)
        result.findAll {
            csiSystems.contains(it.csiSystem)
        }
    }

    /**
     * <p>
     * Finds all {@link CsiAggregation}s within the specified date range,
     * within the specified {@link CsiAggregationInterval} and with the
     * specified {@link AggregatorType}.
     * </p>
     *
     * @param fromDate first date, inclusive; not <code>null</code>.
     * @param toDate last date, inclusive; not <code>null</code>.
     * @param interval the interval to match; not <code>null</code>.
     * @param aggregator the aggregator to match; not <code>null</code>.
     *
     * @return Matching values, not <code>null</code> but possibly empty.
     */
    public List<CsiAggregation> getCsiAggregations(
            Date fromDate,
            Date toDate,
            CsiAggregationInterval interval,
            AggregatorType aggregator
    ) {
        def criteria = CsiAggregation.createCriteria()
        return criteria.list {
            between("started", fromDate, toDate)
            eq("interval", interval)
            eq("aggregator", aggregator)
        }
    }

    /**
     * Returns all {@link CsiAggregationUpdateEvent}s for given id's csiAggregationIds of {@link CsiAggregation}s.
     * @param csiAggregationIds
     * @return A list of all {@link CsiAggregationUpdateEvent}s persisted for {@link CsiAggregation}s with id's from list csiAggregationIds.
     */
    public List<CsiAggregationUpdateEvent> getUpdateEvents(List<Long> csiAggregationIds) {
        return CsiAggregationUpdateEvent.createCriteria().list {
            'in'("csiAggregationId", csiAggregationIds)
        }
    }

    /**
     * Returns the last {@link CsiAggregationUpdateEvent} for the given csiAggregationId
     * @param csiAggregationId
     * @return the last {@link CsiAggregationUpdateEvent}
     */
    public CsiAggregationUpdateEvent getLatestUpdateEvent(Long csiAggregationId) {
        return CsiAggregationUpdateEvent.findByCsiAggregationId(csiAggregationId,
                [sort: "dateOfUpdate", order: "desc"])
    }

    /**
     * Returns all {@link CsiAggregationUpdateEvent}s for given {@link CsiAggregation}-id csiAggregationId.
     * @param csiAggregationId
     * @return A list of all {@link CsiAggregationUpdateEvent}s persisted for {@link CsiAggregation} with id csiAggregationId.
     */
    public List<CsiAggregationUpdateEvent> getUpdateEvents(Long csiAggregationId) {
        return CsiAggregationUpdateEvent.findAllByCsiAggregationId(csiAggregationId)
    }

    /**
     * Returns all open {@link CsiAggregation}s (that is who's attribute closedAndCalculated is false) with start-date equal or before Date toFindBefore.
     * @param toFindBefore
     * @return All open {@link CsiAggregation}s (that is who's attribute closedAndCalculated is false) with start-date equal or before Date toFindBefore.
     */
    List<CsiAggregation> getOpenCsiAggregationsEqualsOrBefore(Date toFindBefore) {
        return CsiAggregation.createCriteria().list {
            le("started", toFindBefore)
            eq("closedAndCalculated", false)
        }
    }

    /**
     * Delivers all {@link CsiAggregation}s with closedAndCalculated=false who's time-interval has expired for at least minutes minutes.
     * @param minutes
     * 					Time for which the CsiAggregation has to be expired.  e.g.
     * 					<ul>
     * 					<li>A HOURLY-CsiAggregation with <code>started=2014-07-07 15:00:00</code> and an expiration-time of 90 minutes expires at "2014-07-07 17:30:00"</li>
     * 					<li>A DAILY-CsiAggregation with <code>started=2014-07-07 00:00:00</code> and an expiration-time of 180 minutes expires at "2014-07-08 03:00:00"</li>
     * 					<li>A WEEKLY-CsiAggregation with <code>started=2014-07-04 00:00:00</code> and an expiration-time of 300 minutes expires at "2014-07-11 05:00:00"</li>
     * 					</ul>
     * @return
     */
    public List<CsiAggregation> getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(int minutes) {

        DateTime expirationTimeAgo = csiAggregationUtilService.getNowInUtc().minusMinutes(minutes)
        DateTime expirationTimePlusOneHourAgo = csiAggregationUtilService.subtractOneInterval(expirationTimeAgo, CsiAggregationInterval.HOURLY)

        List<CsiAggregation> openAndExpired = []
        getOpenCsiAggregationsEqualsOrBefore(expirationTimePlusOneHourAgo.toDate()).each { openMv ->

            boolean isHourly = openMv.interval.intervalInMinutes == CsiAggregationInterval.HOURLY
            if (isHourly) {
                openAndExpired.add(openMv)
            } else {
                addIfDailyOrWeeklyAndExpired(openMv, expirationTimeAgo, openAndExpired)
            }

        }
        return openAndExpired
    }

    private void addIfDailyOrWeeklyAndExpired(CsiAggregation openMv, DateTime expirationTimeAgo, List openAndExpired) {

        boolean isDaily = openMv.interval.intervalInMinutes == CsiAggregationInterval.DAILY
        DateTime expirationTimePlusOneDayAgo = csiAggregationUtilService.subtractOneInterval(expirationTimeAgo, CsiAggregationInterval.DAILY)

        boolean isOlderThanOneDay = !new DateTime(openMv.started).isAfter(expirationTimePlusOneDayAgo)
        if (isDaily && isOlderThanOneDay) {
            openAndExpired.add(openMv)
        } else {
            addIfWeeklyAndExpired(openMv, expirationTimeAgo, openAndExpired)
        }
    }

    private void addIfWeeklyAndExpired(CsiAggregation openMv, DateTime expirationTimeAgo, List openAndExpired) {
        boolean isWeekly = openMv.interval.intervalInMinutes == CsiAggregationInterval.WEEKLY
        DateTime expirationTimePlusOneWeekAgo = csiAggregationUtilService.subtractOneInterval(expirationTimeAgo, CsiAggregationInterval.WEEKLY)
        boolean isOlderThanOneWeek = !new DateTime(openMv.started).isAfter(expirationTimePlusOneWeekAgo)
        if (isWeekly && isOlderThanOneWeek) {
            openAndExpired.add(openMv)
        }
    }

    /*-
     * *mze-2013-08-15 - Design-Note zu IT-60*
     *
     * Für die Anfrage aus der UI ggf. hier eine findAllBy(MvQueryParams) hinzufügen.
     *
     * Dies ist zwar "etwas zu viel" Logik für diesen DAO, aber die Schnittstelle
     * belibt dann verständlicher. Der Kommentar der Klasse müsste dann angepasst
     * werden. Schöner wäre natürlich, ein HourlyCsiAggregationTag reinzureichen,
     * der bereits vorher aus den MvQueryParams per MVTS erstellt wurde
     * (siehe hierzu auch: IT-62).
     *
     * In diesem Zuge sollte man ggf. auch die Klasse MvQueryParams umbenennen zu
     * HourlyCsiAggregationQueryArguments, da die Klasse für für hourly mvs wirklich
     * nutzbar ist.
     *
     * Ferner sollte die "Validierung" der Klasse MvQueryParams in die Klasse selbst
     * wandern, also von:
     *     de.iteratec.isocsi.EventCsiAggregationService.validateMvQueryParams(MvQueryParams)
     * in MvQueryParams verschoben werden.
     */

    /**
     * Gets all pageCsiAggregations for given JobGroups and Pages
     * @param fromDate the start date
     * @param toDate the end date
     * @param jobGroups the {@link JobGroup}s for csiAggregations
     * @param pages the {@link Page}s for csiAggregations
     * @param targetInterval the {@link CsiAggregationInterval}
     * @return a list of all csiAggregations with given criteria
     */
    public List<CsiAggregation> getPageCsiAggregations(Date fromDate, Date toDate, List<JobGroup> jobGroups, List<Page> pages, CsiAggregationInterval targetInterval) {
        AggregatorType aggregatorType = AggregatorType.findByName(AggregatorType.PAGE)
        return CsiAggregation.findAllByStartedBetweenAndAggregatorAndIntervalAndJobGroupInListAndPageInList(fromDate, toDate, aggregatorType, targetInterval, jobGroups, pages)
    }

    /**
     * Gets all shopCsiAggregations for given JobGroups
     * @param fromDate the start date
     * @param toDate the end date
     * @param jobGroups the {@link JobGroup}s for csiAggregations
     * @param targetInterval the {@link CsiAggregationInterval}
     * @return a list of all csiAggregations with given criteria
     */
    public List<CsiAggregation> getShopCsiAggregations(Date fromDate, Date toDate, List<JobGroup> jobGroups, CsiAggregationInterval targetInterval) {
        AggregatorType aggregatorType = AggregatorType.findByName(AggregatorType.SHOP)
        return CsiAggregation.findAllByStartedBetweenAndAggregatorAndIntervalAndJobGroupInList(fromDate, toDate, aggregatorType, targetInterval, jobGroups)
    }

    public List<CsiAggregation> getMvs(Date fromDate, Date toDate, MvQueryParams mvQueryParams, CsiAggregationInterval interval, AggregatorType aggregatorType) {
        def result
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, "CsiAggregationDaoService: getMvs", 1) {

            List<JobGroup> jobGroups = JobGroup.getAll(mvQueryParams.jobGroupIds)
            List<MeasuredEvent> measuredEvents = MeasuredEvent.getAll(mvQueryParams.measuredEventIds)
            List<Page> pages = Page.getAll(mvQueryParams.pageIds)
            List<Browser> browsers = Browser.getAll(mvQueryParams.browserIds)
            List<Location> locations = Location.getAll(mvQueryParams.locationIds)
            List<ConnectivityProfile> connectivityProfiles = ConnectivityProfile.getAll(mvQueryParams.connectivityProfileIds)

            def criteria = new CriteriaAggregator(CsiAggregation.class)

            criteria.addCriteria {
                between("started", fromDate, toDate)
                eq("interval", interval)
                eq("aggregator", aggregatorType)
            }

            if (jobGroups) {
                criteria.addCriteria {
                    'in'('jobGroup', jobGroups)
                }
            }
            if (measuredEvents) {
                criteria.addCriteria {
                    'in'('measuredEvent', measuredEvents)
                }
            }
            if (pages) {
                criteria.addCriteria {
                    'in'('page', pages)
                }
            }
            if (browsers) {
                criteria.addCriteria {
                    'in'('browser', browsers)
                }
            }
            if (locations) {
                criteria.addCriteria {
                    'in'('location', locations)
                }
            }
            if (connectivityProfiles) {
                criteria.addCriteria {
                    'in'('connectivityProfile', connectivityProfiles)
                }
            }

            result = criteria.runQuery('list', [:])
        }
        return result
    }
}
