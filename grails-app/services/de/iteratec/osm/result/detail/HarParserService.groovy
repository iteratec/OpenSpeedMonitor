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

import groovy.json.JsonSlurper

import org.joda.time.DateTime

import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.PageService



/**
 * The HarParserService transforms HAR files into WebPerformanceWaterfalls.
 */
class HarParserService {

	PageService pageService
	
    static transactional = true
	/**
	 * Software wpt-monitor (http://www.wptmonitor.org/) adds this suffix to the job-names before transmitting it as event name to wpt-servers. 
	 */
	private static final WPTMONITOR_EVENTNAME_SUFFIX = ' 1'
	enum PageIdComponent{
		RUN_NUMBER, EVENT_NAME, CACHED_VIEW_NUMBER
	}
	
	/**
	 * Parse the JSON input into an object.
	 * @param json The JSON formatted data
	 * @return The java Object
	 */
	private Object parseJSON(String json) {
		return new JsonSlurper().parseText(json)
	}
	
	/**
	 * <p>
	 * Splits the parsed har into lists of waterfall-entries. Each list represents the entries of one waterfall contained in the har.
	 * The lists get delivered in a map with the page-id of the waterfalls as key.
	 * </p>
	 * @param harParsedBySlurper 
	 * 					The parsed har. Can contain multiple waterfalls: One for each {@link MeasuredEvent}, run and {@link CachedView}. 
	 * @return The entries of all the waterfalls contained in harParsedBySlurper, delivered in a map with the page-id of each Waterfall as key.<br>Example:<br>
	 * [<br>'page-id of waterfall1': [entry1OfWaterfall1, entry2OfWaterfall1, entry3OfWaterfall1, ..., entryNOfWaterfall1],<br>
	 * 'page-id of waterfall2': [entry1OfWaterfall2, entry2OfWaterfall2, entry3OfWaterfall2, ..., entryNOfWaterfall2],<br>
	 * 'page-id of waterfall2': [entry1OfWaterfall3, entry2OfWaterfall3, entry3OfWaterfall3, ..., entryNOfWaterfall3]<br>]
	 */
	private Map<String, List<Object>> splitWaterfalls(Object harParsedBySlurper) {
		Map<String, List<Object>> pageidToEntriesMap = [:].withDefault{[]}
		harParsedBySlurper.log.entries.each{entry -> pageidToEntriesMap[entry.pageref] << entry}
		return pageidToEntriesMap
	}
	
	/**
	 * <p>
	 * Converts the JSON data of a single HTTP-archive (HAR) into {@link WebPerformanceWaterfall}s.
	 * The waterfalls are delivered in a map with the page-id of the waterfalls as key. {@link WebPerformanceWaterfall}s
	 * without {@link WaterfallEntry}s get removed from map.
	 * </p>
	 * 
	 * @param har The source HTTP-archive
	 * @return A map of the resulting {@link WebPerformanceWaterfall}s with the page-id of the waterfalls as key.
	 * @throws IllegalArgumentException If some of the data can't be parsed form har.
	 */
	public Map<String, WebPerformanceWaterfall> getWaterfalls(String har) throws IllegalArgumentException{
		Object json = parseJSON(har)
		if(json.log.creator.name != "WebPagetest") {
			throw new IllegalArgumentException("Only http-archives created by the WebPagetest are supported!")
		}
		Map<String, List<Object>> pageidToEntriesMap = splitWaterfalls(json)
		Map<String, WebPerformanceWaterfall> pageidToWebPerformanceWaterfallMap = [:].withDefault{new WebPerformanceWaterfall()}
		// First parse the entries ...
		pageidToEntriesMap.each{pageId, waterfallEntries ->
			pageidToWebPerformanceWaterfallMap[pageId].waterfallEntries = waterfallEntries.collect{entry ->
				WaterfallEntry waterfall = new WaterfallEntry()
				waterfall.dnsLookupTimeStartInMillisecs = entry._dns_start.toInteger()
				waterfall.dnsLookupTimeEndInMillisecs = entry._dns_end.toInteger()
				waterfall.initialConnectTimeStartInMillisecs = entry._connect_start.toInteger()
				waterfall.initialConnectTimeEndInMillisecs = entry._connect_end.toInteger()
				waterfall.sslNegotationTimeStartInMillisecs = entry._ssl_start.toInteger()
				waterfall.sslNegotationTimeEndInMillisecs = entry._ssl_end.toInteger()
				waterfall.timeToFirstByteStartInMillisecs = entry._ttfb_start.toInteger()
				waterfall.timeToFirstByteEndInMillisecs = entry._ttfb_end.toInteger()
				waterfall.downloadTimeStartInMillisecs = entry._ttfb_start.toInteger()
				waterfall.downloadTimeEndInMillisecs = entry._ttfb_end.toInteger()
				waterfall.startOffset = entry._all_start.toInteger()
				waterfall.httpStatus = entry.response.status.toInteger()
				waterfall.host = entry._host
				waterfall.path = entry._url
				waterfall.mimeType = entry.response.content.mimeType
				waterfall.blocked = entry.timings.blocked != -1
				waterfall.downloadedBytes = entry._bytesIn.toInteger()
				waterfall.uploadedBytes = entry._bytesOut.toInteger()
				waterfall.oneBasedIndexInWaterfall = entry._number.toInteger()
				return waterfall
			}
		}
		// ... then the page metadata
		json.log.pages.each{page ->
			pageidToWebPerformanceWaterfallMap[page.id].title = page.title
			pageidToWebPerformanceWaterfallMap[page.id].startDate = new DateTime(page.startedDateTime).toDate() 
			pageidToWebPerformanceWaterfallMap[page.id].url = page._URL
			pageidToWebPerformanceWaterfallMap[page.id].startRenderInMillisecs = page._render.toInteger()
			pageidToWebPerformanceWaterfallMap[page.id].docCompleteTimeInMillisecs = page._docTime.toInteger()
			pageidToWebPerformanceWaterfallMap[page.id].domTimeInMillisecs = page._domTime.toInteger()
			pageidToWebPerformanceWaterfallMap[page.id].fullyLoadedTimeInMillisecs = page._fullyLoaded.toInteger()

			//run number			
			String runNumberFromPageId = parseFromPageId(page.id.toString(), PageIdComponent.RUN_NUMBER)
			if (page._runNumber == null && runNumberFromPageId == null) {
				throw new IllegalArgumentException("Can't parse run number from page of http-archive: ${page}")
			} else{
				pageidToWebPerformanceWaterfallMap[page.id].numberOfWptRun = (page._runNumber ?: runNumberFromPageId).toInteger()
			}
			//event name
			String eventNameFromPageId = parseFromPageId(page.id.toString(), PageIdComponent.EVENT_NAME)
			pageidToWebPerformanceWaterfallMap[page.id].eventName = page._eventName?:eventNameFromPageId?:''
			//cached view
			String cachedViewFromPageId = parseFromPageId(page.id.toString(), PageIdComponent.CACHED_VIEW_NUMBER)
			if (page._cacheWarmed ==null && cachedViewFromPageId == null) {
				throw new IllegalArgumentException("Can't parse CachedView from page of http-archive: ${page}")
			}else{
				if ((page._cacheWarmed ?: cachedViewFromPageId).toInteger()==0) {
					pageidToWebPerformanceWaterfallMap[page.id].cachedView = CachedView.UNCACHED
				} else if ((page._cacheWarmed ?: cachedViewFromPageId).toInteger()==1) {
					pageidToWebPerformanceWaterfallMap[page.id].cachedView = CachedView.CACHED
				}else{
					throw new IllegalArgumentException("Can't parse CachedView from page of http-archive: ${page}")
				}
			}
		}
		return pageidToWebPerformanceWaterfallMap.findAll{it.value.waterfallEntries}
	}
	
	/**
	 * <p>
	 * Converts the JSON data of multiple HTTP-archives into {@link WebPerformanceWaterfall}s.
	 * </p>
	 * 
	 * @see #getWaterfalls(String)
	 * @param hars The collection of HAR documents
	 * @return A collection of the results of getWaterfalls(String) for every input
	 */
	Collection<Map<String, WebPerformanceWaterfall>> getWaterfalls(Collection<String> hars) {
		return hars.collect{getWaterfalls(it)}
	}
	
	/**
	 * <p>
	 * Creates a page-id-String from run, eventName and cachedView.
	 * </p>
	 * @param run 
	 * 				Number of run in WPT-Test.
	 * @param eventName
	 * 				Name of the measured event.
	 * @param cachedView
	 * 				Cached view of the test. Must be {@link CachedView.UNCACHED} or {@link CachedView.CACHED}.
	 * @return The page-id the single waterfalls are identified by in http-archives (see https://sites.google.com/a/webpagetest.org/docs/advanced-features/har-upload#TOC-What-assumptions-do-we-make-about-HAR-files-that-agents-upload-).
	 * @throws IllegalArgumentException if cachedView is invalid (NOT {@link CachedView.UNCACHED} or {@link CachedView.CACHED}).
	 * @see https://sites.google.com/a/webpagetest.org/docs/advanced-features/har-upload#TOC-What-assumptions-do-we-make-about-HAR-files-that-agents-upload-
	 */
	String createPageIdFrom(Integer run, String eventName, CachedView cachedView){
		String cachedViewAsString
		if (cachedView.equals(CachedView.UNCACHED)) {
			cachedViewAsString = '0'
		} else if (cachedView.equals(CachedView.CACHED)) {
			cachedViewAsString = '1'
		} else{
			throw new IllegalArgumentException("Cached view has to be one of ${CachedView.UNCACHED} or ${CachedView.CACHED} and actually is ${cachedView}")
		}
		String eventNamePossiblyEmpty = eventName?"${eventName}_":''
		return "page_${run}_${eventNamePossiblyEmpty}${cachedViewAsString}"
	}
	
	/**
	 * Removes the following from event name in page-id and {@link WebPerformanceWaterfall#eventName} in map pageidToWaterfallMap:
	 * <ul>
	 * <li>{@link #WPTMONITOR_EVENTNAME_SUFFIX} added by wpt-monitor to event name</li>
	 * <li>Pagename-prefix</li>
	 * </ul> 
	 * @param pageidToWaterfallMap Map to remove {@link #WPTMONITOR_EVENTNAME_SUFFIX} from. 
	 * @return A new map in which {@link #WPTMONITOR_EVENTNAME_SUFFIX} and Pagename-prefix are removed from pageid (keys in pageidToWaterfallMap) and {@link WebPerformanceWaterfall#eventName}.
	 */
	Map<String, WebPerformanceWaterfall> removeWptMonitorSuffixAndPagenamePrefixFromEventnames(Map<String, WebPerformanceWaterfall> pageidToWaterfallMap){
		// replace pageid's in maps
		Map<String, String> mappingOriginToNewPageids = [:]
		pageidToWaterfallMap.each {pageid, waterfall ->
			String eventNameInPageid = parseFromPageId(pageid, PageIdComponent.EVENT_NAME) 
			String eventNameExcludedPagenamePart = eventNameInPageid ? pageService.excludePagenamePart(eventNameInPageid) : ''
			boolean wptMonitorSuffixExist = eventNameExcludedPagenamePart.endsWith(WPTMONITOR_EVENTNAME_SUFFIX)
			if (!eventNameInPageid.equals(eventNameExcludedPagenamePart) || wptMonitorSuffixExist) {
				String runNumberInPageid = parseFromPageId(pageid, PageIdComponent.RUN_NUMBER)
				String cachedViewNumberInPageid = parseFromPageId(pageid, PageIdComponent.CACHED_VIEW_NUMBER)
				mappingOriginToNewPageids[pageid] = createPageIdFrom(
					new Integer(runNumberInPageid), 
					wptMonitorSuffixExist ? eventNameExcludedPagenamePart[0..-(WPTMONITOR_EVENTNAME_SUFFIX.size()+1)] : eventNameExcludedPagenamePart,
					cachedViewNumberInPageid.equals('0')?CachedView.UNCACHED:CachedView.CACHED)
			}
		}
		Map<String, WebPerformanceWaterfall> suffixesRemoved = pageidToWaterfallMap.inject( [:] ) { resultingMap, iteratedMapEntry -> 
			resultingMap[ mappingOriginToNewPageids[ iteratedMapEntry.key ] ?: iteratedMapEntry.key ] = iteratedMapEntry.value 
			return resultingMap 
		}
		// change event names
		suffixesRemoved.each {pageid, waterfall ->
			String eventNameExcludedPagenamepart = pageService.excludePagenamePart(waterfall.eventName)
			waterfall.eventName = eventNameExcludedPagenamepart.endsWith(WPTMONITOR_EVENTNAME_SUFFIX) ? eventNameExcludedPagenamepart[0..-3] : eventNameExcludedPagenamepart 
		} 
		return suffixesRemoved
	}
	/**
	 * Parses {@link PageIdComponent} from pageId.
	 * @param pageId To parse from.
	 * @param component To parse.
	 * @return Parsed {@link PageIdComponent} or null if component can't get parsed from pageId.
	 */
	private String parseFromPageId(String pageId, PageIdComponent component){
		def matcher = pageId =~ /page_([0-9])(?:_(.*))?_([01])/
		switch(component){
			case PageIdComponent.RUN_NUMBER:  
				return matcher[0][1]
				break
			case PageIdComponent.EVENT_NAME:
				return matcher[0][2]
				break
			case PageIdComponent.CACHED_VIEW_NUMBER:
				return matcher[0][3]
				break
			default: 
				throw new IllegalArgumentException("Parsing ${component} from Page-ID isn't implemented.")
				break
		}
	}
			
}
