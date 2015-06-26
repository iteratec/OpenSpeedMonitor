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

import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.ConnectivityProfileService

import java.util.regex.Pattern

import de.iteratec.osm.result.detail.WebPerformanceWaterfall
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.ErQueryParams
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResultService
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.persistence.OsmDataSourceService;

/**
 * Contains only methods that query {@link EventResult}s from database. Doesn't contain any dependencies to other domains or
 * service-logic.
 * 
 * @author rhe
 * @author mze
 */
public class EventResultDaoService {
		
	OsmDataSourceService osmDataSourceService

	JobResultService jobResultService

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
	public List<EventResult> getMedianEventResultsBy(Date fromDate, Date toDate, Set<CachedView> cachedViews, MvQueryParams mvQueryParams){
		Pattern rlikePattern=measuredValueTagService.getTagPatternForHourlyMeasuredValues(mvQueryParams)
		return getMedianEventResultsBy(fromDate, toDate, cachedViews, rlikePattern.pattern)
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
		Date fromDate, Date toDate, Set<CachedView> cachedViews, ErQueryParams queryParams, Map<String, Number> gtConstraints, Map<String, Number> ltConstraints){
		
		Pattern rlikePattern=measuredValueTagService.getTagPatternForHourlyMeasuredValues(queryParams)
		def criteria = EventResult.createCriteria()
		
		if(osmDataSourceService.getRLikeSupport()){
			List result = criteria.list {
				rlike('tag', rlikePattern)
				between('jobResultDate', fromDate, toDate)
				eq('medianValue', true)
				'in'('cachedView', cachedViews)
				gtConstraints.each {attr, gtValue ->
					gt(attr, gtValue)
				}
				ltConstraints.each {attr, ltValue ->
					lt(attr, ltValue)
				}
                if (queryParams.connectivityProfileIds.size() > 0){
                    List<ConnectivityProfile> predefinedProfiles = queryParams.connectivityProfileIds.collect {ConnectivityProfile.get(it)}
                    if (queryParams.customConnectivityNameRegex == null && queryParams.includeNativeConnectivity == false){
                        connectivityProfile{
                            'in'('id', predefinedProfiles*.ident())
                        }
                    }else if (queryParams.includeNativeConnectivity == false){
                        or {
                            connectivityProfile{
                                'in'('id', predefinedProfiles*.ident())
                            }
                            rlike('customConnectivityName', ~/${queryParams.customConnectivityNameRegex}/)
                        }
                    }else if (queryParams.customConnectivityNameRegex == null){
                        or {
                            connectivityProfile{
                                'in'('id', predefinedProfiles*.ident())
                            }
                            eq('customConnectivityName', ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE)
                        }
                    }else{
                        or {
                            connectivityProfile{
                                'in'('id', predefinedProfiles*.ident())
                            }
                            rlike('customConnectivityName', ~/${queryParams.customConnectivityNameRegex}/)
                            eq('customConnectivityName', ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE)
                        }
                    }
                }else{
                    if (queryParams.customConnectivityNameRegex != null && queryParams.includeNativeConnectivity == true){
                        or {
                            rlike('customConnectivityName', ~/${queryParams.customConnectivityNameRegex}/)
                            eq('customConnectivityName', ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE)
                        }
                    }else if (queryParams.customConnectivityNameRegex != null){
                        rlike('customConnectivityName', ~/${queryParams.customConnectivityNameRegex}/)
                    }else if (queryParams.includeNativeConnectivity == true){
                        eq('customConnectivityName', ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE)
                    }
                }
			}
			return result;
		} else {
			List<EventResult> eventResults = criteria.list {
				between('jobResultDate', fromDate, toDate)
				eq('medianValue', true)
				'in'('cachedView', cachedViews)
				gtConstraints.each {attr, gtValue ->
					gt(attr, gtValue)
				}
				ltConstraints.each {attr, ltValue ->
					lt(attr, ltValue)
				}
			}.grep{ it.tag ==~ rlikePattern }

			return applyConnectivityQueryParams(eventResults, queryParams)
		}
	}

    private List<EventResult> applyConnectivityQueryParams(List<EventResult> eventResults, ErQueryParams queryParams){
        if (queryParams.connectivityProfileIds.size() > 0){
            if (queryParams.customConnectivityNameRegex == null && queryParams.includeNativeConnectivity == false){
                eventResults = eventResults.findAll {
                    it.connectivityProfile != null && queryParams.connectivityProfileIds.contains(it.connectivityProfile.ident())
                }
            }else if (queryParams.includeNativeConnectivity == false){
                eventResults = eventResults.findAll {
                    (it.connectivityProfile != null && queryParams.connectivityProfileIds.contains(it.connectivityProfile.ident())) ||
                            (it.customConnectivityName != null && it.customConnectivityName ==~ ~/${queryParams.customConnectivityNameRegex}/)
                }
            }else if (queryParams.customConnectivityNameRegex == null){
                eventResults = eventResults.findAll {
                    (it.connectivityProfile != null && queryParams.connectivityProfileIds.contains(it.connectivityProfile.ident())) ||
                    (it.customConnectivityName != null && it.customConnectivityName.equals(ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE))
                }
            }else{
                eventResults = eventResults.findAll {
                    (it.connectivityProfile != null && queryParams.connectivityProfileIds.contains(it.connectivityProfile.ident())) ||
                    (it.customConnectivityName != null && it.customConnectivityName ==~ ~/${queryParams.customConnectivityNameRegex}/) ||
                    (it.customConnectivityName != null && it.customConnectivityName.equals(ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE))
                }
            }
        }else{
            if (queryParams.customConnectivityNameRegex != null && queryParams.includeNativeConnectivity == true){
                eventResults = eventResults.findAll {
                    (it.customConnectivityName != null && it.customConnectivityName ==~ ~/${queryParams.customConnectivityNameRegex}/) ||
                            (it.customConnectivityName != null && it.customConnectivityName.equals(ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE))
                }
            }else if (queryParams.customConnectivityNameRegex != null){
                eventResults = eventResults.findAll {
                    (it.customConnectivityName != null && it.customConnectivityName ==~ ~/${queryParams.customConnectivityNameRegex}/)
                }
            }else if (queryParams.includeNativeConnectivity == true){
                eventResults = eventResults.findAll {
                    (it.customConnectivityName != null && it.customConnectivityName.equals(ConnectivityProfileService.CUSTOM_CONNECTIVITY_NAME_FOR_NATIVE))
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
	public Collection<EventResult> getByStartAndEndTimeAndMvQueryParams(Date fromDate, Date toDate, Collection<CachedView> cachedViews, MvQueryParams mvQueryParams){
		Pattern rlikePattern=measuredValueTagService.getTagPatternForHourlyMeasuredValues(mvQueryParams);
		
		
		def criteria = EventResult.createCriteria()
		//FIXME: rlike isn't supported in H2-db used in unit-tests. The following Environment-switch should be replaced with metaclass-method-replacement in tests.
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
	 * Gets all {@link EventResult}s from db associated with {@link WebPerformanceWaterfall} waterfall.
	 * @param waterfall
	 * @return List of {@link EventResult}s associated with {@link WebPerformanceWaterfall} waterfall.
	 */
	public List<EventResult> findEventResultsAssociatedTo(WebPerformanceWaterfall waterfall) {
		return EventResult.findAllByWebPerformanceWaterfall(waterfall)
	}
	
	/**
	 * Returns a Collection of {@link EventResult}s for specified time frame and {@link MvQueryParams}.
	 * @param mvQueryParams
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
	public Collection<EventResult> getCountedByStartAndEndTimeAndMvQueryParams(MvQueryParams mvQueryParams, Date fromDate, Date toDate, Integer max, Integer offset){
		Pattern rlikePattern=measuredValueTagService.getTagPatternForHourlyMeasuredValues(mvQueryParams);
			
		def eventResultCriteria = EventResult.createCriteria()
			
		if(osmDataSourceService.getRLikeSupport()){
			def eventResults = eventResultCriteria.list (max: max, offset:offset) {
				and {	
					
					rlike("tag", rlikePattern.pattern)
					between("jobResultDate", fromDate, toDate)
					
				}
				order('jobResultDate', 'desc')
			}
			return eventResults
		}
		
		List<EventResult> eventResults = eventResultCriteria.list (max: max, offset:offset) {
			between("jobResultDate", fromDate, toDate)
		}
		return eventResults.grep{ it.tag ==~ rlikePattern }
		
	}
	
	/**
	 * Returns all EventResults belonging to the specified Job.
	 */
	public Collection<EventResult> getEventResultsByJob(Job _job, Date fromDate, Date toDate, Integer max, Integer offset){
		return EventResult.where { jobResultJobConfigId == _job.id && jobResultDate >= fromDate && jobResultDate <= toDate }.list(sort: 'jobResultDate', order: 'desc', max: max, offset: offset)
	}
}