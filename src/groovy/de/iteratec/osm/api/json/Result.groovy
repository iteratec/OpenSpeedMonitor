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

package de.iteratec.osm.api.json

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent

/**
 * <p>
 * A result which is rendered as JSON.
 * </p>
 * 
 * @author mze
 * @since IT-81
 */
public final class Result {
	
	public static final DecimalFormat API_DECIMAL_FORMAT = new DecimalFormat();
	static {
		DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.GERMANY);
		formatSymbols.setDecimalSeparator((char)',');
		formatSymbols.setNaN('0');
		formatSymbols.setInfinity('0');
		
		API_DECIMAL_FORMAT.applyPattern('0.00', false);
		API_DECIMAL_FORMAT.setDecimalFormatSymbols(formatSymbols);
		API_DECIMAL_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
	}
	
	/**
	 * Execution time of of the test.
	 */
	public final Date executionTime
	
	/**
	 * <p>
	 * The CSI percent value as String. Rendered from a number with a 
	 * scale of 2. A comma (,) is used to separate the fraction part 
	 * (German notation). As defined for percents, the numbers range 
	 * is from 0 to 100 (both inclusive)
	 * </p>
	 */
	public final String csiValue;
	
	/**
	 * Time when the browser triggers the window.onload event.
	 * 2014-08-26: This time is basis for the calculation of csiValue.
	 */
	public final Integer docCompleteTimeInMillisecs
	
	/**
	 * Number of the webpagetest run this result comes from.
	 * The REST method which uses this class queries and delivers just median results. These can be from different runs.
	 */
	public final Integer numberOfWptRun
	
	/**
	 * Whether this result represents a first (empty local browser cache) or a repeated view.
	 * Can be one of {@link CachedView.UNCACHED} or {@link CachedView.UNCACHED}.
	 */
	public final String cachedView
	
	/**
	 * The name of the {@link Page} this result is assigned to.
	 * 
	 * @see Page#getName()
	 */
	public final String page;
	
	/**
	 * The name of the {@link MeasuredEvent} this result is assigned to.
	 * 
	 * @see MeasuredEvent#getName()
	 */
	public final String step;
	
	/**
	 * The name of the {@link Browser} this result is assigned to.
	 * 
	 * @see Browser#getName()
	 */
	public final String browser;
	
	/**
	 * The location of the {@link Location} this result is assigned to.
	 * 
	 * @see Location#getLocation()
	 */
	public final String location;
	
	/**
	 * The URL to the details of this webpagetest-result.
	 */
	public final String detailUrl
	/**
	 * The URL to download the http-archive of the {@link JobResult}.
	 * 
	 * @see http://www.softwareishard.com/blog/har-12-spec/
	 */
	public final String httpArchiveUrl
	
	public Result(
			JobResult jobResult,
			EventResult eventResultOfJobResult) {
		
		if( jobResult.getEventResults().count( {it.id == eventResultOfJobResult.id} ) < 1 )
		{
			throw new IllegalArgumentException('The job result does not contain the specified event result. This is illegal.');
		}
			
		this.csiValue = API_DECIMAL_FORMAT.format( (double)
				eventResultOfJobResult.customerSatisfactionInPercent);
			
		this.browser = jobResult.locationBrowser; 
		this.location = jobResult.locationUniqueIdentifierForServer;
		
		MeasuredEvent event = eventResultOfJobResult.getMeasuredEvent();
		this.page = event.getTestedPage().getName();
		this.step = event.getName();
		String baseUrlWithTrailingSlash = jobResult.job.location.wptServer.baseUrl.endsWith('/') ? jobResult.job.location.wptServer.baseUrl : jobResult.job.location.wptServer.baseUrl + '/'  
		String testId = jobResult.testId
		if (baseUrlWithTrailingSlash && testId) {
			this.detailUrl = "${baseUrlWithTrailingSlash}result/${jobResult.testId}"
			this.httpArchiveUrl = "${baseUrlWithTrailingSlash}export.php?test=${jobResult.testId}"
		}else{
			this.detailUrl = ''
			this.httpArchiveUrl = ''
		}
		this.executionTime = jobResult.date
		this.docCompleteTimeInMillisecs = eventResultOfJobResult.docCompleteTimeInMillisecs
		this.numberOfWptRun = eventResultOfJobResult.numberOfWptRun
		this.cachedView = eventResultOfJobResult.cachedView.toString()
	}
			
	@Override
	public String toString() {
		return "Result [page=" + page + ", step=" + step + ", browser=" + browser + ", location=" + location + ", csiValue=" + csiValue + "]";
	}
}
