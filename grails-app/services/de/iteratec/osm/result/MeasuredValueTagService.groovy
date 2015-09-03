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

package de.iteratec.osm.result

import static Contract.*

import java.util.regex.Pattern

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.measurement.schedule.JobService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location

/**
 * <p>
 * A service to create an inspect tags used to identify and group 
 * {@link MeasuredValue}s.
 * </p>
 * 
 * <p>
 * Measured values identified by an tag, which is just text, to be 
 * decoupled from the data the measured value is based on. This service
 * provides methods to create tags (starting with {@code create}) and
 * to inspect tags (starting with {@code get} or {@code find}).
 * </p>
 * 
 * <p>
 * FIXME mze 2013-08-26: Change to use DAO-Services.
 * </p>
 * 
 * @author nkuhn
 * @author mze
 */
class MeasuredValueTagService {

	/**
	 * A fragment, a sub pattern matches all IDs. More formally it matches all
	 * characters expect the tag separation char ';'.
	 */
	private static String SUB_PATTERN_MATCHES_ALL_IDS = '[^;]*';
	
	PageService pageService
	JobService jobService
	JobResultDaoService jobResultDaoService

	/*
	 * Creation of tags
	 */

	/**
	 * <p>
	 * Creates a tag to mark hourly event-{@link MeasuredValue}s.
	 * </p>
	 * 
	 * <p>
	 * The result tag looks like:
	 * {@code JobGroup-ID;MeasuredEvent-ID“;Page-ID;Browser-ID;Location-ID}
	 * </p>
	 * 
	 * @param jobGroup 
	 *        The (previously saved) {@link JobGroup}, not <code>null</code>.
	 * @param measuredEvent
	 *        The (previously saved) {@link MeasuredEvent}, not <code>null</code>.
	 * @param page
	 *        The (previously saved) {@link Page}, not <code>null</code>.
	 * @param browser
	 *        The (previously saved) {@link Browser}, not <code>null</code>.
	 * @param location
	 *        The (previously saved) {@link Location}, not <code>null</code>.
	 * @return The calculated tag, never <code>null</code>, never 
	 *         {@linkplain String#isEmpty() empty}.
	 * @throws NullPointerException if at least one argument is 
	 *         <code>null</code>.
	 * @throws IllegalArgumentException if at least one passed entity 
	 *         has no id (was not saved before).
	 * @since JIRA IT-40
	 */
	public String createHourlyEventTag(
			JobGroup jobGroup,
			MeasuredEvent measuredEvent,
			Page page,
			Browser browser,
			Location location) throws NullPointerException, IllegalArgumentException {

		requiresArgumentNotNull('jobGroup', jobGroup);
		requiresArgumentNotNull('measuredEvent', measuredEvent);
		requiresArgumentNotNull('page', page);
		requiresArgumentNotNull('browser', browser);
		requiresArgumentNotNull('location', location);

		checkEntityHasAnId('jobGroup', jobGroup)
		checkEntityHasAnId('measuredEvent', measuredEvent)
		checkEntityHasAnId('page', page)
		checkEntityHasAnId('browser', browser)
		checkEntityHasAnId('location', location)
		
		StringBuilder resultBuilder = new StringBuilder()
		resultBuilder.append(jobGroup.ident())
		resultBuilder.append(';')
		resultBuilder.append(measuredEvent.ident())
		resultBuilder.append(';')
		resultBuilder.append(page.ident())
		resultBuilder.append(';')
		resultBuilder.append(browser.ident())
		resultBuilder.append(';')
		resultBuilder.append(location.ident())

		return resultBuilder.toString()
	}
			
	/**
	 * <p>
	 * Creates a tag to mark event result-{@link MeasuredValue}s.
	 * </p>
	 *
	 * <p>
	 * The result tag looks like:
	 * {@code JobGroup-ID;MeasuredEvent-ID“;Page-ID;Browser-ID;Location-ID}
	 * </p>
	 *
	 * @param jobGroup
	 *        The (previously saved) {@link JobGroup}, not <code>null</code>.
	 * @param measuredEvent
	 *        The (previously saved) {@link MeasuredEvent}, not <code>null</code>.
	 * @param page
	 *        The (previously saved) {@link Page}, not <code>null</code>.
	 * @param browser
	 *        The (previously saved) {@link Browser}, not <code>null</code>.
	 * @param location
	 *        The (previously saved) {@link Location}, not <code>null</code>.
	 * @return The calculated tag, never <code>null</code>, never
	 *         {@linkplain String#isEmpty() empty}.
	 * @throws NullPointerException if at least one argument is
	 *         <code>null</code>.
	 * @throws IllegalArgumentException if at least one passed entity
	 *         has no id (was not saved before).
	 * @since JIRA IT-61
	 */
	public String createEventResultTag(
			JobGroup jobGroup,
			MeasuredEvent measuredEvent,
			Page page,
			Browser browser,
			Location location) throws NullPointerException, IllegalArgumentException {

		return createHourlyEventTag(jobGroup, measuredEvent, page, browser, location)
	}
	/**
	 * <p>
	 * Creates an aggregator tag for page measured values.
	 * </p>
	 * 
	 * <i>Notice:</i> Used generally, changed to page tag in IT-89
	 * 
	 * @param group 
	 *        The {@link JobGroup} of corresponding Jobs, 
	 *        not <code>null</code>.
	 * @param page 
	 *        The Page which values relevant, not <code>null</code>.
	 * @return The tag, not <code>null</code>.
	 * @since JIRA IT-40
	 * @version IT-89
	 */
	String createPageAggregatorTag(JobGroup group, Page page){
		requiresArgumentNotNull('group', group)
		requiresArgumentNotNull('page', page)
	

		return group.ident().toString() + ';' + page.ident().toString()
	}

	/**
	 * <p>
	 * Creates an aggregator tag for weekly shop measured values.
	 * </p>
	 *
	 * @param group 
	 *        The {@link JobGroup} of corresponding Jobs, 
	 *        not <code>null</code>.
	 * @return The tag, not <code>null</code>.
	 * @since JIRA IT-40
	 * @version IT-89
	 */
	String createShopAggregatorTag(JobGroup group){
		requiresArgumentNotNull('group', group)

		return group.ident().toString()
	}

	/**
	 * TODO Doc
	 * 
	 * @param newResult
	 * @return
	 */
	String createPageAggregatorTagByEventResult(EventResult newResult){
		String pageTag
		JobResult jobResult = newResult.jobResult
		Job job = jobResult.job
		if (job) {
			JobGroup jobGroup = jobService.getCsiJobGroupOf(job)
			if (newResult.getMeasuredEvent() && newResult.getMeasuredEvent().getTestedPage()
			&& jobGroup && jobGroup.groupType == JobGroupType.CSI_AGGREGATION) {
				pageTag = "${jobGroup.ident()};${newResult.getMeasuredEvent().getTestedPage().ident()}"
			}
		}
		return pageTag
	}

	/*
	 * Retrieving objects from tags. 
	 */
	
	/**
	 * <p>
	 * Finds the {@link JobGroup} referenced by the specified weekly shop-tag.
	 * </p>
	 *
	 * @param weeklyShopTag The shop-tag, not <code>null</code>
	 * @return The {@link JobGroup} or <code>null</code> if the group was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code weeklyShopTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an weekly shop tag.
	 * @version IT-89
	 */
	public findJobGroupOfWeeklyShopTag(String weeklyShopTag){
		requiresArgumentNotNull('hourlyEventMvTag', weeklyShopTag)
		String[] tagSegments = getTagSegmentsOfTag(weeklyShopTag, 1)
		return JobGroup.get(tagSegments[0] as Serializable)
	}

	/**
	 * <p>
	 * Finds the {@link JobGroup} referenced by the specified {@link EventResult} tag.
	 * </p>
	 *
	 * @param eventResultTag The event result tag, not <code>null</code>
	 * @return The {@link JobGroup} or <code>null</code> if the group was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code eventResultTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an event result tag.
	 */
	public JobGroup findJobGroupOfEventResultTag(String eventResultTag) throws NullPointerException, IllegalArgumentException {
		return this.findJobGroupOfHourlyEventTag(eventResultTag)
	}
	
	/**
	 * <p>
	 * Finds the {@link JobGroup} referenced by the specified hourly aggregator tag.
	 * </p>
	 * 
	 * @param hourlyEventMvTag The hourly event tag, not <code>null</code>
	 * @return The {@link JobGroup} or <code>null</code> if the group was not found anymore (ex. was deleted).
	 * @throws NullPointerException 
	 *         if {@code hourlyEventMvTag} is <code>null</code>.
	 * @throws IllegalArgumentException 
	 *         if the specified tag is not an hourly event tag.
	 * @since JIRA IT-40
	 */
	public JobGroup findJobGroupOfHourlyEventTag(String hourlyEventMvTag) throws NullPointerException, IllegalArgumentException {
		Serializable jobGroupId = findJobGroupIdOfHourlyEventTag(hourlyEventMvTag)
		return JobGroup.get(jobGroupId)
	}
	/**
	 * <p>
	 * Finds the {@link JobGroup}-ID referenced by the specified hourly aggregator tag.
	 * </p>
	 *
	 * @param hourlyEventMvTag The hourly event tag, not <code>null</code>
	 * @return The {@link JobGroup}-ID or <code>null</code> if the group was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code hourlyEventMvTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an hourly event tag.
	 * @since JIRA IT-40
	 */
	public Serializable findJobGroupIdOfHourlyEventTag(String hourlyEventMvTag) throws NullPointerException, IllegalArgumentException {
		requiresArgumentNotNull('hourlyEventMvTag', hourlyEventMvTag)

		String[] tagSegments = getTagSegmentsOfTag(hourlyEventMvTag, 5)

		return tagSegments[0] as Serializable
	}

	/**
	 * <p>
	 * Finds the {@link Page} referenced by the specified hourly event tag.
	 * </p>
	 *
	 * @param hourlyEventMvTag The hourly event tag, not <code>null</code>
	 * @return The {@link Page} or <code>null</code> if the page was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code hourlyEventMvTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an hourly event tag.
	 * @since JIRA IT-40
	 */
	public Page findPageOfHourlyEventTag(String hourlyEventMvTag) throws NullPointerException, IllegalArgumentException {
		Serializable pageId = findPageIdOfHourlyEventTag(hourlyEventMvTag)
		return Page.get(pageId)
	}
	/**
	 * <p>
	 * Finds the {@link Page} referenced by the specified event result tag.
	 * </p>
	 *
	 * @param eventResultTag The event result tag, not <code>null</code>
	 * @return The {@link Page} or <code>null</code> if the page was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code eventResultTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an hourly event tag.
	 * @since JIRA IT-40
	 */
	public Page findPageOfEventResultTag(String eventResultTag) throws NullPointerException, IllegalArgumentException {
		return findPageOfHourlyEventTag(eventResultTag)
	}
	/**
	 * <p>
	 * Finds the {@link Page}-ID referenced by the specified hourly event tag.
	 * </p>
	 *
	 * @param hourlyEventMvTag The hourly event tag, not <code>null</code>
	 * @return The {@link Page}-ID or <code>null</code> if the page was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code hourlyEventMvTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an hourly event tag.
	 * @since JIRA IT-40
	 */
	public Serializable findPageIdOfHourlyEventTag(String hourlyEventMvTag) throws NullPointerException, IllegalArgumentException {
		requiresArgumentNotNull('hourlyEventMvTag', hourlyEventMvTag)

		String[] tagSegments = getTagSegmentsOfTag(hourlyEventMvTag, 5)

		return tagSegments[2] as Serializable
	}
	
	/**
	 * <p>
	 * Finds the {@link JobGroup} referenced by the specified weekly page tag.
	 * </p>
	 *
	 * @param weeklyPageTag The weekly page tag, not <code>null</code>
	 * @return The {@link JobGroup} or <code>null</code> if the JobGroup was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code weeklyPageTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an weekly page tag.
	 * @since JIRA IT-8
	 */
	public JobGroup findJobGroupOfWeeklyPageTag(String weeklyPageTag) throws NullPointerException, IllegalArgumentException {
		requiresArgumentNotNull('weeklyPageTag', weeklyPageTag)

		String[] tagSegments = getTagSegmentsOfTag(weeklyPageTag, 2)

		return JobGroup.get(tagSegments[0] as Serializable)
	}
	
	/**
	 * <p>
	 * Finds the {@link Page} referenced by the specified daily page tag.
	 * <b>Note:</b> Daily page tag is the same as weekly page tag.
	 * </p>
	 * 
	 * @param dailyPageTag The daily page tag, not <code>null</code>
	 * @return The {@link Page} or <code>null</code> if the page was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code dailyPageTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not a daily page tag.
	 */
	public Page findPageOfDailyPageTag(String dailyPageTag) throws NullPointerException, IllegalArgumentException {
		return findPageOfWeeklyPageTag(dailyPageTag)
	}
	/**
	 * <p>
	 * Finds the {@link Page} referenced by the specified weekly page tag.
	 * </p>
	 *
	 * @param weeklyPageTag The weekly page tag, not <code>null</code>
	 * @return The {@link Page} or <code>null</code> if the page was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code weeklyPageTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not a daily page tag.
	 * @since JIRA IT-8
	 */
	public Page findPageOfWeeklyPageTag(String weeklyPageTag) throws NullPointerException, IllegalArgumentException {
		requiresArgumentNotNull('weeklyPageTag', weeklyPageTag)

		String[] tagSegments = getTagSegmentsOfTag(weeklyPageTag, 2)

		return Page.get(tagSegments[1] as Serializable)
	}
	/**
	 * <p>
	 * Finds the {@link MeasuredEvent} referenced by the specified hourly aggregator tag.
	 * </p>
	 *
	 * @param hourlyEventMvTag The hourly event tag, not <code>null</code>
	 * @return The {@link MeasuredEvent} or <code>null</code> if the measured event was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code hourlyEventMvTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an hourly event tag.
	 * @since JIRA IT-40
	 */
	public MeasuredEvent findMeasuredEventOfHourlyEventTag(String hourlyEventMvTag) throws NullPointerException, IllegalArgumentException {
		Serializable measuredEvenId = findMeasuredEventIdOfHourlyEventTag(hourlyEventMvTag)
		return MeasuredEvent.get(measuredEvenId)
	}
	/**
	 * <p>
	 * Finds the {@link MeasuredEvent} referenced by the specified event result tag.
	 * </p>
	 *
	 * @param eventResultTag The event result tag, not <code>null</code>
	 * @return The {@link MeasuredEvent} or <code>null</code> if the measured event was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code eventResultTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an hourly event tag.
	 * @since JIRA IT-40
	 */
	public MeasuredEvent findMeasuredEventOfEventResult(String eventResultTag) throws NullPointerException, IllegalArgumentException {
		return findMeasuredEventOfHourlyEventTag(eventResultTag)
	}
	/**
	 * <p>
	 * Finds the {@link MeasuredEvent}-ID referenced by the specified hourly aggregator tag.
	 * </p>
	 *
	 * @param hourlyEventMvTag The hourly event tag, not <code>null</code>
	 * @return The {@link MeasuredEvent}-ID or <code>null</code> if the measured event was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code hourlyEventMvTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an hourly event tag.
	 * @since JIRA IT-40
	 */
	public Serializable findMeasuredEventIdOfHourlyEventTag(String hourlyEventMvTag) throws NullPointerException, IllegalArgumentException {
		requiresArgumentNotNull('hourlyEventMvTag', hourlyEventMvTag)

		String[] tagSegments = getTagSegmentsOfTag(hourlyEventMvTag, 5)

		return tagSegments[1] as Serializable
	}

	/**
	 * <p>
	 * Finds the {@link Browser} referenced by the specified hourly aggregator tag.
	 * </p>
	 *
	 * @param hourlyEventMvTag The hourly event tag, not <code>null</code>
	 * @return The {@link Browser} or <code>null</code> if the browser was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code hourlyEventMvTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an hourly event tag.
	 * @since JIRA IT-40
	 */
	public Browser findBrowserOfHourlyEventTag(String hourlyEventMvTag) throws NullPointerException, IllegalArgumentException {
		Serializable browserId = findBrowserIdOfHourlyEventTag(hourlyEventMvTag)
		return Browser.get(browserId)
	}
	/**
	 * <p>
	 * Finds the {@link Browser}-ID referenced by the specified hourly aggregator tag.
	 * </p>
	 *
	 * @param hourlyEventMvTag The hourly event tag, not <code>null</code>
	 * @return The {@link Browser}-ID or <code>null</code> if the browser was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code hourlyEventMvTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an hourly event tag.
	 * @since JIRA IT-40
	 */
	public Serializable findBrowserIdOfHourlyEventTag(String hourlyEventMvTag) throws NullPointerException, IllegalArgumentException {
		requiresArgumentNotNull('hourlyEventMvTag', hourlyEventMvTag)

		String[] tagSegments = getTagSegmentsOfTag(hourlyEventMvTag, 5)

		return tagSegments[3] as Serializable
	}

	/**
	 * <p>
	 * Finds the {@link Location} referenced by the specified hourly aggregator tag.
	 * </p>
	 *
	 * @param hourlyEventMvTag The hourly event tag, not <code>null</code>
	 * @return The {@link Location} or <code>null</code> if the location was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code hourlyEventMvTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an hourly event tag.
	 * @since JIRA IT-40
	 */
	public Location findLocationOfHourlyEventTag(String hourlyEventMvTag) throws NullPointerException, IllegalArgumentException {
		Serializable locationId = findLocationIdOfHourlyEventTag(hourlyEventMvTag)
		return Location.get(locationId)
	}
	/**
	 * <p>
	 * Finds the {@link Location}-ID referenced by the specified hourly aggregator tag.
	 * </p>
	 *
	 * @param hourlyEventMvTag The hourly event tag, not <code>null</code>
	 * @return The {@link Location}-ID or <code>null</code> if the location was not found anymore (ex. was deleted).
	 * @throws NullPointerException
	 *         if {@code hourlyEventMvTag} is <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the specified tag is not an hourly event tag.
	 * @since JIRA IT-40
	 */
	public Serializable findLocationIdOfHourlyEventTag(String hourlyEventMvTag) throws NullPointerException, IllegalArgumentException {
		requiresArgumentNotNull('hourlyEventMvTag', hourlyEventMvTag)

		String[] tagSegments = getTagSegmentsOfTag(hourlyEventMvTag, 5)

		return tagSegments[4] as Serializable
	}

	/**
	 * <p>
	 * Gets a tag segments.
	 * </p>
	 * 	
	 * @param tag The tag which segments to get, not <code>null</code>.
	 * @param requiredSegmentCount The required segment count required for the tag to be valid.
	 * @return The tags segments, never <code>null</code>.
	 * @throws IllegalArgumentException 
	 *         if the count of segments in the specified tag does not match {@code requiredSegmentCount}.
	 * @since JIRA IT-40
	 */
	private String[] getTagSegmentsOfTag(String tag, int requiredSegmentCount) throws IllegalArgumentException {
		requiresArgumentNotNull('tag', tag)

		String[] result = tag.split(";")
		if( result.length != requiredSegmentCount ) {
			throw new IllegalArgumentException(
			'The tag is not an hourly aggregation tag, the segment count is not ' +
			requiredSegmentCount + '; current segment count: ' +
			result.length + ', passed argument: ' + tag)
		}

		return result
	}
	
	/*-
	 * Creation of tag-patterns for querying with gorm. 
	 */

	/**
	 * <p>
	 * Creates a tag-pattern to find weekly page {@link MeasuredValue}s by 
	 * {@link JobGroup}s and {@link Page}s. 
	 * </p>
	 * 
	 * <p>
	 * <em>Note:</em> Passing an empty list to at least one of the 
	 * arguments will cause a pattern which fits none measured value.
	 * </p>
	 * 
	 * @param groups The groups result should match, not <code>null</code>.
	 * @param pages The pages the result should match, not <code>null</code>.
	 * 
	 * @return A tag-pattern, never <code>null</code>.
	 */
	public Pattern getTagPatternForWeeklyPageMvsWithJobGroupsAndPages(List<JobGroup> groups, List<Page> pages) {
		requiresArgumentNotNull("groups", groups)
		requiresArgumentNotNull("pages", pages)

		String groupsPattern = createSubPatternToMatchIDs(groups*.ident())
		String pagesPattern = createSubPatternToMatchIDs(pages*.ident())
		
		return ~/^${groupsPattern};${pagesPattern}$/
	}

	/**
	 * <p>
	 * Creates a tag-pattern to find weekly shop {@link MeasuredValue}s by 
	 * {@link JobGroup}s.
	 * </p>
	 * 
	 * <p>
	 * <em>Note:</em> Passing an empty list to {@code groups} will cause a 
	 * pattern which fits none measured value.
	 * </p>
	 * 
	 * @param groups The groups result should match, not <code>null</code>.
	 * 
	 * @return A tag-pattern, never <code>null</code>.
	 */
	Pattern getTagPatternForWeeklyShopMvsWithJobGroups(List<JobGroup> groups){
		requiresArgumentNotNull('groups', groups);
		
		String groupsPattern = createSubPatternToMatchIDs(groups*.ident())
		
		return ~/^${groupsPattern}$/
	}

	/**
	 * <p>
	 * Creates a search pattern to find hourly {@link MeasuredValue}s matching 
	 * at least one of the elements in the corresponding collection of 
	 * elements.
	 * </p>
	 * 
	 * <p>
	 * The returned {@link Pattern} could be used to query MaesuredValues from 
	 * the database via {@code rlike} search.
	 * </p>
	 * 
	 * <p>
	 * <em>Note:</em> Passing an empty collection for a specific ID-scope will 
	 * cause the creation of a pattern that finds any ID in this scope. For 
	 * example: Passing an empty group-ID-collection will result in a pattern
	 * that will find elements for all groups.
	 * </p>
	 * 
	 * @param queryParams 
	 * 	      The {@linkplain MvQueryParams query arguments} for which measured
	 *        values should be found, not <code>null</code>.
	 * @return A pattern to match corresponding hourly measured values, 
	 *         never <code>null</code>.
	 * @throws NullPointerException if at least one argument is 
	 *         <code>null</code>.
	 * @since JIRA IT-40
	 * @version 2 (since JIRA IT-85)
	 */
	public Pattern getTagPatternForHourlyMeasuredValues(MvQueryParams queryParams) {
		requiresArgumentNotNull('queryParams', queryParams)
		
		String jobGroupIDPattern = createSubPatternToMatchIDs(queryParams.jobGroupIds);
		String measuredEventIDPattern = createSubPatternToMatchIDs(queryParams.measuredEventIds);
		String pageIDPattern = createSubPatternToMatchIDs(queryParams.pageIds);		
		String browserIDPattern = createSubPatternToMatchIDs(queryParams.browserIds);		
		String locationIDPattern = createSubPatternToMatchIDs(queryParams.locationIds);
		
		return ~/^${jobGroupIDPattern};${measuredEventIDPattern};${pageIDPattern};${browserIDPattern};${locationIDPattern}$/
	}
	
	
	/**
	 * <p>
	 * Creates a search pattern to find result {@link MeasuredValue}s matching
	 * at least one of the elements in the corresponding collection of
	 * elements.
	 * </p>
	 *
	 * <p>
	 * The returned {@link Pattern} could be used to query MaesuredValues from
	 * the database via {@code rlike} search.
	 * </p>
	 *
	 * <p>
	 * <em>Note:</em> Passing an empty collection for a specific ID-scope will
	 * cause the creation of a pattern that finds any ID in this scope. For
	 * example: Passing an empty group-ID-collection will result in a pattern
	 * that will find elements for all groups.
	 * </p>
	 *
	 * @param queryParams
	 * 	      The {@linkplain MvQueryParams query arguments} for which measured
	 *        values should be found, not <code>null</code>.
	 * @return A pattern to match corresponding hourly measured values,
	 *         never <code>null</code>.
	 * @throws NullPointerException if at least one argument is
	 *         <code>null</code>.
	 * @since JIRA IT-7
	 */
	public Pattern getTagPatternForResultMeasuredValues(MvQueryParams queryParams) {
		return getTagPatternForHourlyMeasuredValues(queryParams);
	}
		
	
	/**
	 * <p>
	 * Creates a search tag to find {@link MeasuredValue}s matching
	 * the exact tag.
	 * </p>
	 *
	 * <p>
	 * The Tag is only designed for single attribute {@link MvQueryParams}!
	 * </p>
	 *
	 * <p>
	 * <em>Note:</em> Passing an empty collection for a specific ID-scope will
	 * cause the creation of a pattern that finds any ID in this scope. For
	 * example: Passing an empty group-ID-collection will result in a pattern
	 * that will find elements for all groups.
	 * </p>
	 *
	 * @param queryParams
	 * 	      The {@linkplain MvQueryParams query arguments} for which measured
	 *        values should be found, not <code>null</code>.
	 * @return A string to match corresponding hourly measured values,
	 *         never <code>null</code>.
	 * @throws NullPointerException if at least one argument is
	 *         <code>null</code>.
	 * @throws IllegalArgumentException if at least one queryParam size() is greater than 1!
	 * @since JIRA IT-60
	 */
	public String getTagStringForResultMeasuredValues(Long jobGroup, Long measuredEvent, Long page, Long browser, Long location) {
		requiresArgumentNotNull('queryParams', jobGroup)
		requiresArgumentNotNull('queryParams', measuredEvent)
		requiresArgumentNotNull('queryParams', page)
		requiresArgumentNotNull('queryParams', browser)
		requiresArgumentNotNull('queryParams', location)
				
		return jobGroup.toString() + ";" + measuredEvent.toString() + ";" + page.toString() + ";" + browser.toString() + ";" + location.toString();
	}
	
	/**
	 * <p>
	 * Creates a sub-pattern to match any ID in the specified collection. If 
	 * the collection is empty, the {@link #SUB_PATTERN_MATCHES_ALL_IDS} is 
	 * returned.
	 * </p>
	 * 
	 * <p>
	 * Examples:
	 * <br/>
	 * The collection {@code [1,2,3]} results in the tag {@code (1|2|3)} and  
	 * an empty collection {@code []} results in the tag 
	 * {@code [^;]*} which fits all IDs.
	 * </p>
	 * 
	 * @param ids 
	 *         The collection of IDs, not <code>null</code>; possibly
	 *         {@linkplain Collection#isEmpty() empty}.
	 * @return A sub pattern to find tags as described above;
	 *         never <code>null</code>.
	 */
	private String createSubPatternToMatchIDs(Collection<Long> ids)
	{
		String result = ids.join('|');
		if( result.isEmpty() )
		{
			result = SUB_PATTERN_MATCHES_ALL_IDS;
		} else {
			result = '(' + result + ')'
		}
		
		return result;
	}
	
	/**
	 * Proofs whether given tag is a valid hourly event-tag.
	 * A valid hourly event-tag is composed of id's of the following five domains in this order:
	 * <ul>
	 * <li>{@link JobGroup}</li>
	 * <li>{@link MeasuredEvent}</li>
	 * <li>{@link Page}</li>
	 * <li>{@link Browser}</li>
	 * <li>{@link Location}</li>
	 * </ul>
	 *  
	 * @param tagToProof
	 * @return True if tagToProof is composed of five Longs where  
	 * <ul>
	 * <li>First is a valid identifier for an object of domain {@link JobGroup}</li>
	 * <li>Second is a valid identifier for an object of domain {@link MeasuredEvent}</li>
	 * <li>Third is a valid identifier for an object of domain {@link Page}</li>
	 * <li>Fourth is a valid identifier for an object of domain {@link Browser}</li>
	 * <li>Fifth is a valid identifier for an object of domain {@link Location}</li>
	 * </ul>
	 */
	public Boolean isValidHourlyEventTag(String tagToProof){
		try {
			findJobGroupOfHourlyEventTag(tagToProof).name
			findMeasuredEventOfHourlyEventTag(tagToProof).name
			findPageOfHourlyEventTag(tagToProof).name
			findBrowserOfHourlyEventTag(tagToProof).name
			findLocationOfHourlyEventTag(tagToProof).uniqueIdentifierForServer
		} catch (IllegalArgumentException iae) {
			return false
		} catch (NullPointerException npe){
			return false
		}
		return true
	}
}
