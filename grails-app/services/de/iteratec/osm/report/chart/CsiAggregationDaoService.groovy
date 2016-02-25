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
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.persistence.OsmDataSourceService
import org.joda.time.DateTime

/**
 * Contains only methods that query {@link CsiAggregation}s from database. Doesn't contain any dependencies to other domains or
 * service-logic.
 * 
 * <p>
 * <strong>Important:</strong> If you add a query method with an rlike statement make sure to replace the rlike statement
 * with a manual regex in test environments.
 * </p>
 * 
 * @author nkuhn
 *
 */
class CsiAggregationDaoService {
	
	CsiAggregationUtilService csiAggregationUtilService
	OsmDataSourceService osmDataSourceService = new OsmDataSourceService()
	
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
	public CsiAggregation tryToFindById(long databaseId)
	{
		return CsiAggregation.get(databaseId);
	}
	
	/**
	 * Gets all {@link CsiAggregation}s from db respective given arguments. tag-attribute is queried via rlike.
	 * 
	 * <strong>Important:</strong> This method uses custom regex filtering when executed in a test environment
	 * as H2+GORM/Hibernate used in test environments does not reliably support rlike statements. 
	 * @param fromDate
	 * @param toDate
	 * @param rlikePattern
	 * @param interval
	 * @param aggregator
	 * @return
	 */
	List<CsiAggregation> getMvs(
			Date fromDate,
			Date toDate,
			String rlikePattern,
			CsiAggregationInterval interval,
			AggregatorType aggregator
							   ){
		def criteria = CsiAggregation.createCriteria()
		return criteria.list {
				between("started", fromDate, toDate)
				eq("interval", interval)
				eq("aggregator", aggregator)
				rlike("tag", rlikePattern)
			}
	}

	/**
	 * Gets all {@link CsiAggregation}s from db respective given arguments. tag-attribute is queried via rlike.
	 *
	 * <strong>Important:</strong> This method uses custom regex filtering when executed in a test environment
	 * as H2+GORM/Hibernate used in test environments does not reliably support rlike statements.
	 * @param fromDate
	 * @param toDate
	 * @param rlikePattern
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
							   ){
		toDate = fromDate == toDate ? toDate + interval.intervalInMinutes : toDate
		//TODO: optimize query to something like:
		//findAllByStartedBetweenAndStartedLessThanAndIntervalAndAggregatorAndCsiSystemInListAndTagRlike
		//... which works in running App, but NOT in unit-tests!
		List<CsiAggregation> result =  CsiAggregation.findAllByStartedBetweenAndStartedLessThanAndIntervalAndAggregator(fromDate,toDate,toDate,interval,aggregator)
		result.findAll {
			csiSystems.contains(it.csiSystem)
		}
	}

	/**
	 * Gets all {@link CsiAggregation}s from db respective given arguments. tag-attribute is queried via rlike.
	 *
	 * <strong>Important:</strong> This method uses custom regex filtering when executed in a test environment
	 * as H2+GORM/Hibernate used in test environments does not reliably support rlike statements.
	 * @param fromDate
	 * @param toDate
	 * @param rlikePattern
	 * @param interval
	 * @param aggregator
	 * @param connectivityProfiles
	 * @return
	 */
	List<CsiAggregation> getMvs(
			Date fromDate,
			Date toDate,
			String rlikePattern,
			CsiAggregationInterval interval,
			AggregatorType aggregator,
			List<ConnectivityProfile> connectivityProfiles
							   ){
		List<CsiAggregation> result
		toDate = fromDate == toDate ? toDate + interval.intervalInMinutes : toDate

		//TODO: optimize query to something like:
		//findAllByStartedBetweenAndStartedLessThanAndIntervalAndAggregatorAndConnectivityProfileInListAndTagRlike
		//... which works in running App, but NOT in unit-tests!
		if(osmDataSourceService.getRLikeSupport()){
			result =  CsiAggregation.findAllByStartedBetweenAndStartedLessThanAndIntervalAndAggregatorAndTagRlike(fromDate,toDate,toDate,interval,aggregator,rlikePattern)
		} else {
			result = CsiAggregation.findAllByStartedBetweenAndStartedLessThanAndIntervalAndAggregator(fromDate, toDate, toDate, interval, aggregator)
			result.grep{ it.tag ==~ rlikePattern }
		}

		result.findAll {
			connectivityProfiles.contains(it.connectivityProfile)
		}
	}
	/**
	 * Gets calc-not {@link CsiAggregation}s from db. tag-attribute is queried via rlike.
	 * TODO: dri-2014-01-13 Replace rlike statements when using this method in a test.
	 * @param fromDate
	 * @param toDate
	 * @param rlikePattern
	 * @param interval
	 * @param aggregators
	 * @return
	 */
//	List<CsiAggregation> getMvs(
//		Date fromDate,
//		Date toDate,
//		String rlikePattern,
//		CsiAggregationInterval interval,
//		Collection<AggregatorType> aggregators
//		){
//		def criteria = CsiAggregation.createCriteria()
//		return criteria.list {
//			between("started", fromDate, toDate)
//			eq("interval", interval)
//			'in'('aggregator', aggregators)
//			rlike("tag", rlikePattern)
//		}
//	}
	
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
	public List<CsiAggregationUpdateEvent> getUpdateEvents(List<Long> csiAggregationIds){
		return CsiAggregationUpdateEvent.createCriteria().list{
			'in'("csiAggregationId", csiAggregationIds)
		}
	}
	/**
	 * Returns all {@link CsiAggregationUpdateEvent}s for given {@link CsiAggregation}-id csiAggregationId.
	 * @param csiAggregationId
	 * @return A list of all {@link CsiAggregationUpdateEvent}s persisted for {@link CsiAggregation} with id csiAggregationId.
	 */
	public List<CsiAggregationUpdateEvent> getUpdateEvents(Long csiAggregationId){
		return CsiAggregationUpdateEvent.findAllByCsiAggregationId(csiAggregationId)
	}
	
	/**
	 * Returns all open {@link CsiAggregation}s (that is who's attribute closedAndCalculated is false) with start-date equal or before Date toFindBefore.
	 * @param toFindBefore
	 * @return All open {@link CsiAggregation}s (that is who's attribute closedAndCalculated is false) with start-date equal or before Date toFindBefore.
	 */
	public List<CsiAggregation> getOpenCsiAggregationsEqualsOrBefore(Date toFindBefore){
		def criteria = CsiAggregation.createCriteria()
		return criteria.list {
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
	public List<CsiAggregation> getOpenCsiAggregationsWhosIntervalExpiredForAtLeast(int minutes){
		
		DateTime expirationTimeAgo = csiAggregationUtilService.getNowInUtc().minusMinutes(minutes)
		DateTime expirationTimePlusOneHourAgo = csiAggregationUtilService.subtractOneInterval(expirationTimeAgo, CsiAggregationInterval.HOURLY)

		List<CsiAggregation> openAndExpired = []
		getOpenCsiAggregationsEqualsOrBefore(expirationTimePlusOneHourAgo.toDate()).each { openMv ->
			
			boolean isHourly = openMv.interval.intervalInMinutes==CsiAggregationInterval.HOURLY
			if( isHourly ) { 
				openAndExpired.add(openMv)
			}else{
				addIfDailyOrWeeklyAndExpired(openMv, expirationTimeAgo, openAndExpired)
			} 
			
		}
		return openAndExpired
	}

	private void addIfDailyOrWeeklyAndExpired(CsiAggregation openMv, DateTime expirationTimeAgo, List openAndExpired) {
		
		boolean isDaily = openMv.interval.intervalInMinutes==CsiAggregationInterval.DAILY
		DateTime expirationTimePlusOneDayAgo = csiAggregationUtilService.subtractOneInterval(expirationTimeAgo, CsiAggregationInterval.DAILY)
		
		boolean isOlderThanOneDay = !new DateTime(openMv.started).isAfter(expirationTimePlusOneDayAgo)
		if( isDaily && isOlderThanOneDay ) {
			openAndExpired.add(openMv)
		}else{
			addIfWeeklyAndExpired(openMv, expirationTimeAgo, openAndExpired)
		}
	}

	private void addIfWeeklyAndExpired(CsiAggregation openMv, DateTime expirationTimeAgo, List openAndExpired) {
		boolean isWeekly = openMv.interval.intervalInMinutes==CsiAggregationInterval.WEEKLY
		DateTime expirationTimePlusOneWeekAgo = csiAggregationUtilService.subtractOneInterval(expirationTimeAgo, CsiAggregationInterval.WEEKLY)
		boolean isOlderThanOneWeek = !new DateTime(openMv.started).isAfter(expirationTimePlusOneWeekAgo)
		if( isWeekly && isOlderThanOneWeek ) { openAndExpired.add(openMv)}
	}
	
			
	/*-
	 * *mze-2013-08-15 - Design-Note zu IT-60*
	 *
	 * Für die Anfrage aus der UI ggf. hier eine findAllBy(MvQueryParams) hinzufügen.
	 * Diese benutzt dann den MVTS um eine Tag für die rlike-Anfrage zu bauen.
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
}
