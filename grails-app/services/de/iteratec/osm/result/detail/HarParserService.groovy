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

import grails.transaction.Transactional
import groovy.json.JsonSlurper

import org.joda.time.DateTime

import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.PageService



/**
 * The HarParserService transforms HAR files into WebPerformanceWaterfalls.
 */
@Transactional
class HarParserService {

	PageService pageService
	
	/**
	 * Software wpt-monitor (http://www.wptmonitor.org/) adds this suffix to the job-names before transmitting it as event name to wpt-servers. 
	 */
	private static final WPTMONITOR_EVENTNAME_SUFFIX = ' 1'
	enum PageIdComponent{
		RUN_NUMBER, EVENT_NAME, CACHED_VIEW_NUMBER
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
