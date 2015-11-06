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
import de.iteratec.osm.measurement.schedule.ConnectivityProfileService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.persistence.OsmDataSourceService
import de.iteratec.osm.result.*
import de.iteratec.osm.result.detail.WebPerformanceWaterfall
import org.hibernate.criterion.CriteriaSpecification

import java.util.regex.Pattern

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
	MeasuredValueTagService measuredValueTagService

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
	public List<EventResult> getMedianEventResultsBy(Date fromDate, Date toDate, Set<CachedView> cachedViews, String rlikePattern){
		def criteria = EventResult.createCriteria()
		
		if(osmDataSourceService.getRLikeSupport()){
			return criteria.list {
				eq('medianValue', true)
				between('jobResultDate', fromDate, toDate)
				'in'('cachedView', cachedViews)
				rlike('tag', rlikePattern)
			}
		} else {
			List<EventResult> eventResults = criteria.list {
				eq('medianValue', true)
				between('jobResultDate', fromDate, toDate)
				'in'('cachedView', cachedViews)
			}
			return eventResults.grep{ it.tag ==~ rlikePattern }
		}
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
	public List<EventResult> getMedianEventResultsBy(
            Date fromDate, Date toDate, Set<CachedView> cachedViews, MvQueryParams mvQueryParams
    ){

		Pattern rlikePattern=measuredValueTagService.getTagPatternForHourlyMeasuredValues(mvQueryParams)
		return getMedianEventResultsBy(fromDate, toDate, cachedViews, rlikePattern.pattern)

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
    ){

        return getLimitedMedianEventResultsBy(
            fromDate, toDate, [CachedView.UNCACHED, CachedView.CACHED] as Set, erQueryParams, [:], [:], [max: max, offset:offset], sorting
        )

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
        Map<String, Number> gtConstraints,
        Map<String, Number> ltConstraints,
        Map listCriteriaRestrictionMap,
        CriteriaSorting sorting
    ){
		
		Pattern rlikePattern=measuredValueTagService.getTagPatternForHourlyMeasuredValues(queryParams)

        if(osmDataSourceService.getRLikeSupport()){

            return getLimitedMedianEventResultsWithTheAidOfRlike(
                    rlikePattern,
                    fromDate,
                    toDate,
                    cachedViews,
                    gtConstraints,
                    ltConstraints,
                    queryParams,
                    sorting,
                    listCriteriaRestrictionMap
            )

        } else {

            //FIXME: rlike isn't supported in H2-db used in unit-tests. The following Environment-switch should be replaced with metaclass-method-replacement in tests.
            return getLimitedMedianEventResultsByGrepingListWithRlikePatternInMemory(
                    listCriteriaRestrictionMap,
                    fromDate,
                    toDate,
                    cachedViews,
                    gtConstraints,
                    ltConstraints,
                    rlikePattern,
                    queryParams
            )

        }
	}

    private List<EventResult> getLimitedMedianEventResultsByGrepingListWithRlikePatternInMemory(
            Map listCriteriaRestrictionMap,
            Date fromDate,
            Date toDate,
            Set<CachedView> cachedViews,
            Map<String, Number> gtConstraints,
            Map<String, Number> ltConstraints,
            Pattern rlikePattern,
            ErQueryParams queryParams
    ) {

        List<EventResult> eventResults = criteria.list(listCriteriaRestrictionMap) {
            between('jobResultDate', fromDate, toDate)
            eq('medianValue', true)
            'in'('cachedView', cachedViews)
            gtConstraints.each { attr, gtValue ->
                gt(attr, gtValue)
            }
            ltConstraints.each { attr, ltValue ->
                lt(attr, ltValue)
            }
        }.grep { it.tag ==~ rlikePattern }

        return applyConnectivityQueryParamsToCriteriaWithoutRlike(eventResults, queryParams)

    }

    private List<EventResult> getLimitedMedianEventResultsWithTheAidOfRlike(
            Pattern rlikePattern,
            Date fromDate,
            Date toDate,
            Set<CachedView> cachedViews,
            Map<String, Number> gtConstraints,
            Map<String, Number> ltConstraints,
            ErQueryParams queryParams,
            CriteriaSorting sorting,
            Map listCriteriaRestrictionMap
    ) {

        CriteriaAggregator eventResultQueryAggregator = getAggregatedCriteriasFor(
                rlikePattern, fromDate, toDate, cachedViews, gtConstraints, ltConstraints, queryParams, sorting
        )

        return eventResultQueryAggregator.runQuery("list", listCriteriaRestrictionMap);

    }

    private CriteriaAggregator getAggregatedCriteriasFor(
            Pattern rlikePattern,
            Date fromDate,
            Date toDate,
            Set<CachedView> cachedViews,
            Map<String, Number> gtConstraints,
            Map<String, Number> ltConstraints,
            ErQueryParams queryParams,
            CriteriaSorting sorting) {

        CriteriaAggregator eventResultQueryAggregator = new CriteriaAggregator(EventResult.class)

        eventResultQueryAggregator.addCriteria {
            rlike('tag', rlikePattern)
            between('jobResultDate', fromDate, toDate)
            eq('medianValue', true)
            'in'('cachedView', cachedViews)
        }

        gtConstraints.each { attr, gtValue ->
            eventResultQueryAggregator.addCriteria {
                gt(attr, gtValue)
            }
        }
        ltConstraints.each { attr, ltValue ->
            eventResultQueryAggregator.addCriteria {
                lt(attr, ltValue)
            }
        }

        addConnectivityRelatedCriteria(queryParams, eventResultQueryAggregator)

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

    private void addConnectivityRelatedCriteriaWithPredefinedConnectivities(ErQueryParams queryParams, CriteriaAggregator eventResultQueryAggregator){
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
        } else if (predefinedAndCustomAndNative){
            eventResultQueryAggregator.addCriteria {
                or {
                    connectivityProfile (CriteriaSpecification.LEFT_JOIN){
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
                    rlike('customConnectivityName', ~/${ queryParams.customConnectivityNameRegex}/)
                }
            }
        } else if (predefinedAndNative) {
            eventResultQueryAggregator.addCriteria {
                or {
                    connectivityProfile (CriteriaSpecification.LEFT_JOIN){
                        'in'('id', predefinedProfiles*.ident())
                    }
                    eq('noTrafficShapingAtAll', true)
                }
            }

        }
    }
    private void addConnectivityRelatedCriteriaWithoutPredefinedConnectivities(ErQueryParams queryParams, CriteriaAggregator eventResultQueryAggregator){

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

            boolean justPredefined = queryParams.includeCustomConnectivity == false && queryParams.includeNativeConnectivity == false
            boolean predefinedAndCustomAndNative = queryParams.includeCustomConnectivity == true && queryParams.includeNativeConnectivity == true
            boolean predefinedAndCustom = queryParams.includeCustomConnectivity == true
            boolean predefinedAndNative = queryParams.includeNativeConnectivity == true

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

            boolean nativeAndCustom = queryParams.includeCustomConnectivity == true && queryParams.includeNativeConnectivity == true
            boolean justCustom = queryParams.includeCustomConnectivity == true
            boolean justNative = queryParams.includeNativeConnectivity == true

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
    ){

        Pattern rlikePattern=measuredValueTagService.getTagPatternForHourlyMeasuredValues(mvQueryParams);

		def criteria = EventResult.createCriteria()
		if(osmDataSourceService.getRLikeSupport()) {
			return criteria.list {
				between("jobResultDate", fromDate, toDate)
				'in'("cachedView", cachedViews)
				rlike("tag", rlikePattern.pattern)
			}
		} else { 
			List<EventResult> eventResults = criteria.list {
				between("jobResultDate", fromDate, toDate)
				'in'("cachedView", cachedViews)
			}
			return eventResults.grep{ it.tag ==~ rlikePattern }
		}
	}

	/**
	 * Returns all EventResults belonging to the specified Job.
	 */
	public Collection<EventResult> getEventResultsByJob(Job _job, Date fromDate, Date toDate, Integer max, Integer offset){
		return EventResult.where { jobResultJobConfigId == _job.id && jobResultDate >= fromDate && jobResultDate <= toDate }.list(sort: 'jobResultDate', order: 'desc', max: max, offset: offset)
	}

    /**
     * Gets all {@link EventResult}s from db associated with {@link WebPerformanceWaterfall} waterfall.
     * @param waterfall
     * @return List of {@link EventResult}s associated with {@link WebPerformanceWaterfall} waterfall.
     */
    public List<EventResult> findEventResultsAssociatedTo(WebPerformanceWaterfall waterfall) {
        return EventResult.findAllByWebPerformanceWaterfall(waterfall)
    }
}