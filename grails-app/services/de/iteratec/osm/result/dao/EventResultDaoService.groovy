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

package de.iteratec.osm.result.dao

import de.iteratec.osm.dao.CriteriaAggregator
import de.iteratec.osm.dao.CriteriaSorting
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.persistence.OsmDataSourceService
import de.iteratec.osm.result.*
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import org.hibernate.criterion.CriteriaSpecification

/**
 * Contains only methods that query {@link EventResult}s from database. Doesn't contain any dependencies to other domains or
 * service-logic.
 *
 * @author rhe
 * @author mze
 */
public class EventResultDaoService {

    OsmDataSourceService osmDataSourceService
    JobResultDaoService jobResultDaoService
    PerformanceLoggingService performanceLoggingService

    /**
     * <p>
     * Finds the {@link EventResult} with the specified database id if existing.
     * </p>
     *
     * @param databaseId
     *         The database id of the event result to find.
     *
     * @return The found event result or <code>null</code> if not exists.
     */
    public EventResult tryToFindById(long databaseId) {
        return EventResult.get(databaseId);
    }

    /**
     * Gets {@link EventResult}s matching given params from db. tag-attribute is queried via rlike.
     * This method is untested cause rlike-queries are supported just for mysql and oracle up to grails 2.2., not for h2-db :-(
     * @param fromDate
     * @param toDate
     * @param cachedViews
     * @param rlikePattern
     * @return
     */
    public List<EventResult> getMedianEventResultsBy(Date fromDate, Date toDate, Set<CachedView> cachedViews, Long jobGroupId, Long measuredEventId, Long pageId, Long browserId, Long locationId) {
        def criteria = EventResult.createCriteria()

        return criteria.list {
            eq('medianValue', true)
            between('jobResultDate', fromDate, toDate)
            'in'('cachedView', cachedViews)
            jobGroup {
                eq('id', jobGroupId)
            }
            measuredEvent {
                eq('id', measuredEventId)
            }
            page {
                eq('id', pageId)
            }
            browser {
                eq('id', browserId)
            }
            location {
                eq('id', locationId)
            }
        }
    }

    /**
     * Gets {@link EventResult}s matching given params from db.
     * @param fromDate
     * @param toDate
     * @param cachedViews
     * @param rlikePattern
     * @return
     */
    public List<EventResult> getMedianEventResultsBy(Date fromDate, Date toDate, Set<CachedView> cachedViews, Collection<Long> jobGroupIds, Collection<Long> measuredEventIds, Collection<Long> pageIds, Collection<Long> browserIds, Collection<Long> locationIds) {
        def criteria = EventResult.createCriteria()

        return criteria.list {
            eq('medianValue', true)
            between('jobResultDate', fromDate, toDate)
            'in'('cachedView', cachedViews)
            jobGroup {
                'in'('id', jobGroupIds)
            }
            measuredEvent {
                'in'('id', measuredEventIds)
            }
            page {
                'in'('id', pageIds)
            }
            browser {
                'in'('id', browserIds)
            }
            location {
                'in'('id', locationIds)
            }
        }
    }

    /**
     * Returns a Collection of {@link EventResult}s for specified time frame and {@link MvQueryParams}.
     * @param erQueryParams
     * 			The relevant query params, not <code>null</code>.
     * @param fromDate
     * 			The first relevant date (inclusive), not <code>null</code>.
     * @param toDate
     * 			The last relevant date (inclusive), not <code>null</code>.
     * @param max
     * 			The number of records to display per page
     * @param offset
     * 			Pagination offset
     * @return never <code>null</code>, potently empty if no results available
     *         for selection.
     */
    public Collection<EventResult> getCountedByStartAndEndTimeAndMvQueryParams(
            ErQueryParams erQueryParams, Date fromDate, Date toDate, Integer max, Integer offset, CriteriaSorting sorting
    ) {

        List<EventResult> eventResults
        performanceLoggingService.logExecutionTime(LogLevel.DEBUG, 'getting event-results - getCountedByStartAndEndTimeAndMvQueryParams - getLimitedMedianEventResultsBy', IndentationDepth.ONE) {
            eventResults = getLimitedMedianEventResultsBy(
                    fromDate, toDate, [CachedView.UNCACHED, CachedView.CACHED] as Set, erQueryParams, [max: max, offset: offset], sorting
            )
        }
        return eventResults

    }

    /**
     * Gets {@link EventResult}s matching given params from db. tag-attribute is queried via rlike.
     * This method is untested cause rlike-queries are supported just for mysql and oracle up to grails 2.2., not for h2-db :-(
     * @param fromDate
     * @param toDate
     * @param cachedViews
     * @param rlikePattern
     * @return
     */
    public List<EventResult> getLimitedMedianEventResultsBy(
            Date fromDate,
            Date toDate,
            Set<CachedView> cachedViews,
            ErQueryParams queryParams,
            Map listCriteriaRestrictionMap,
            CriteriaSorting sorting
    ) {
        CriteriaAggregator eventResultQueryAggregator = getAggregatedCriteriasFor(
                fromDate, toDate, cachedViews, queryParams, sorting
        )

        List<EventResult> eventResults = eventResultQueryAggregator.runQuery("list", listCriteriaRestrictionMap)
        if (osmDataSourceService.getRLikeSupport()) {
            return eventResults
        } else {
            //FIXME: rlike isn't supported in H2-db used in unit-tests. The following Environment-switch should be replaced with metaclass-method-replacement in tests.
            return applyConnectivityQueryParamsToCriteriaWithoutRlike(eventResults, queryParams)
        }

    }

    private CriteriaAggregator getAggregatedCriteriasFor(
            Date fromDate,
            Date toDate,
            Set<CachedView> cachedViews,
            MvQueryParams queryParams,
            CriteriaSorting sorting) {

        CriteriaAggregator eventResultQueryAggregator = new CriteriaAggregator(EventResult.class)

        eventResultQueryAggregator.addCriteria {
            between('jobResultDate', fromDate, toDate)
            eq('medianValue', true)
            'in'('cachedView', cachedViews)
        }

        if (queryParams.jobGroupIds) {
            eventResultQueryAggregator.addCriteria {
                jobGroup {
                    'in'('id', queryParams.jobGroupIds)
                }
            }
        }
        if (queryParams.measuredEventIds) {
            eventResultQueryAggregator.addCriteria {
                measuredEvent {
                    'in'('id', queryParams.measuredEventIds)
                }
            }
        }
        if (queryParams.pageIds) {
            eventResultQueryAggregator.addCriteria {
                page {
                    'in'('id', queryParams.pageIds)
                }
            }
        }
        if (queryParams.browserIds) {
            eventResultQueryAggregator.addCriteria {
                browser {
                    'in'('id', queryParams.browserIds)
                }
            }
        }
        if (queryParams.locationIds) {
            eventResultQueryAggregator.addCriteria {
                location {
                    'in'('id', queryParams.locationIds)
                }
            }
        }

        if (queryParams instanceof ErQueryParams && osmDataSourceService.getRLikeSupport()) {
            addConnectivityRelatedCriteria((ErQueryParams) queryParams, eventResultQueryAggregator)
        }

        if (sorting.sortingActive) {
            eventResultQueryAggregator.addCriteria {
                order(sorting.sortAttribute, sorting.sortOrder.getHibernateCriteriaRepresentation())
            }
        }
        return eventResultQueryAggregator
    }

    private void addConnectivityRelatedCriteria(ErQueryParams queryParams, CriteriaAggregator eventResultQueryAggregator) {
        if (queryParams.connectivityProfileIds.size() > 0) {

            addConnectivityRelatedCriteriaWithPredefinedConnectivities(queryParams, eventResultQueryAggregator)

        } else {

            addConnectivityRelatedCriteriaWithoutPredefinedConnectivities(queryParams, eventResultQueryAggregator)

        }
    }

    private void addConnectivityRelatedCriteriaWithPredefinedConnectivities(ErQueryParams queryParams, CriteriaAggregator eventResultQueryAggregator) {
        List<ConnectivityProfile> predefinedProfiles = queryParams.connectivityProfileIds.collect {
            ConnectivityProfile.get(it)
        }

        boolean justPredefined = queryParams.includeCustomConnectivity == false && queryParams.includeNativeConnectivity == false
        boolean predefinedAndCustomAndNative = queryParams.includeCustomConnectivity == true && queryParams.includeNativeConnectivity == true
        boolean predefinedAndCustom = queryParams.includeCustomConnectivity == true
        boolean predefinedAndNative = queryParams.includeNativeConnectivity == true

        if (justPredefined) {
            eventResultQueryAggregator.addCriteria {
                connectivityProfile {
                    'in'('id', predefinedProfiles*.ident())
                }
            }
        } else if (predefinedAndCustomAndNative) {
            eventResultQueryAggregator.addCriteria {
                or {
                    connectivityProfile(CriteriaSpecification.LEFT_JOIN) {
                        'in'('id', predefinedProfiles*.ident())
                    }
                    rlike('customConnectivityName', ~/${queryParams.customConnectivityNameRegex}/)
                    eq('noTrafficShapingAtAll', true)
                }
            }
        } else if (predefinedAndCustom) {
            eventResultQueryAggregator.addCriteria {
                or {
                    connectivityProfile(CriteriaSpecification.LEFT_JOIN) {
                        'in'('id', predefinedProfiles*.ident())
                    }
                    rlike('customConnectivityName', ~/${queryParams.customConnectivityNameRegex}/)
                }
            }
        } else if (predefinedAndNative) {
            eventResultQueryAggregator.addCriteria {
                or {
                    connectivityProfile(CriteriaSpecification.LEFT_JOIN) {
                        'in'('id', predefinedProfiles*.ident())
                    }
                    eq('noTrafficShapingAtAll', true)
                }
            }

        }
    }

    private void addConnectivityRelatedCriteriaWithoutPredefinedConnectivities(ErQueryParams queryParams, CriteriaAggregator eventResultQueryAggregator) {

        boolean nativeAndCustom = queryParams.includeCustomConnectivity == true && queryParams.includeNativeConnectivity == true
        boolean justCustom = queryParams.includeCustomConnectivity == true
        boolean justNative = queryParams.includeNativeConnectivity == true

        if (nativeAndCustom) {
            eventResultQueryAggregator.addCriteria {
                or {
                    rlike('customConnectivityName', ~/${queryParams.customConnectivityNameRegex}/)
                    eq('noTrafficShapingAtAll', true)
                }
            }
        } else if (justCustom) {
            eventResultQueryAggregator.addCriteria {
                rlike('customConnectivityName', ~/${queryParams.customConnectivityNameRegex}/)
            }
        } else if (justNative) {
            eventResultQueryAggregator.addCriteria {
                eq('noTrafficShapingAtAll', true)
            }
        }
    }

    private List<EventResult> applyConnectivityQueryParamsToCriteriaWithoutRlike(List<EventResult> eventResults, ErQueryParams queryParams){

        if (queryParams.connectivityProfileIds.size() > 0){
            boolean predefinedAndCustom = queryParams.includeCustomConnectivity
            boolean predefinedAndNative = queryParams.includeNativeConnectivity
            boolean justPredefined = !predefinedAndCustom && !predefinedAndNative
            boolean predefinedAndCustomAndNative = predefinedAndCustom && predefinedAndNative


            if (justPredefined){
                eventResults = eventResults.findAll {
                    it.connectivityProfile != null && queryParams.connectivityProfileIds.contains(it.connectivityProfile.ident())
                }
            }else if (predefinedAndCustomAndNative){
                eventResults = eventResults.findAll {
                    (it.connectivityProfile != null && queryParams.connectivityProfileIds.contains(it.connectivityProfile.ident())) ||
                            (it.customConnectivityName != null && it.customConnectivityName ==~ ~/${queryParams.customConnectivityNameRegex}/) ||
                            (it.noTrafficShapingAtAll == true)
                }
            } else if (predefinedAndCustom){
                eventResults = eventResults.findAll {
                    (it.connectivityProfile != null && queryParams.connectivityProfileIds.contains(it.connectivityProfile.ident())) ||
                            (it.customConnectivityName != null && it.customConnectivityName ==~ ~/${queryParams.customConnectivityNameRegex}/)
                }
            } else if (predefinedAndNative){
                eventResults = eventResults.findAll {
                    (it.connectivityProfile != null && queryParams.connectivityProfileIds.contains(it.connectivityProfile.ident())) ||
                            (it.noTrafficShapingAtAll == true)
                }
            }
        }else{
            boolean justCustom = queryParams.includeCustomConnectivity
            boolean justNative = queryParams.includeNativeConnectivity
            boolean nativeAndCustom = justCustom && justNative

            if (nativeAndCustom){
                eventResults = eventResults.findAll {
                    (it.customConnectivityName != null && it.customConnectivityName ==~ ~/${queryParams.customConnectivityNameRegex}/) ||
                            (it.noTrafficShapingAtAll == true)
                }
            }else if (justCustom){
                eventResults = eventResults.findAll {
                    (it.customConnectivityName != null && it.customConnectivityName ==~ ~/${queryParams.customConnectivityNameRegex}/)
                }
            }else if (justNative){
                eventResults = eventResults.findAll {
                    (it.noTrafficShapingAtAll == true)
                }
            }
        }
        return eventResults
    }

    /**
     * Gets a Collection of {@link EventResult}s for specified time frame, {@link MvQueryParams} and {@link CachedView}s.
     *
     * <strong>Important:</strong> This method uses custom regex filtering when executed in a test environment
     * as H2+GORM/Hibernate used in test environments does not reliably support rlike statements.
     * @param fromDate
     *         The first relevant date (inclusive), not <code>null</code>.
     * @param toDate
     *         The last relevant date (inclusive), not <code>null</code>.
     * @param cachedViews
     *         The relevant cached views, not <code>null</code>.
     * @param mvQueryParams
     *         The relevant query params, not <code>null</code>.
     * @return never <code>null</code>, potently empty if no results available
     *         for selection.
     */
    public Collection<EventResult> getByStartAndEndTimeAndMvQueryParams(
            Date fromDate, Date toDate, Collection<CachedView> cachedViews, MvQueryParams mvQueryParams
    ) {

        CriteriaAggregator criteria = getAggregatedCriteriasFor(fromDate, toDate, cachedViews as Set, mvQueryParams, new CriteriaSorting(sortingActive: false))
        return criteria.runQuery("list", [:])
    }

    /**
     * Returns all EventResults belonging to the specified Job.
     */
    public Collection<EventResult> getEventResultsByJob(Job _job, Date fromDate, Date toDate, Integer max, Integer offset) {
        return EventResult.where {
            jobResultJobConfigId == _job.id && jobResultDate >= fromDate && jobResultDate <= toDate
        }.list(sort: 'jobResultDate', order: 'desc', max: max, offset: offset)
    }
}