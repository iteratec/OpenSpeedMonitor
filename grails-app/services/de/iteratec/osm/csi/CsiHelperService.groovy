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

import static de.iteratec.osm.util.Constants.COOKIE_KEY_CSI_DASHBOARD_TITLE
import de.iteratec.osm.ConfigService
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.OsmCookieService

class CsiHelperService {

	OsmCookieService osmCookieService
	ConfigService configService
	I18nService i18nService
	
	/**
	 * Provides default title for csi dasboard charts. That default title is composed of a base title and an optional main url under test as suffix.
	 * </br></br>Components of title:
	 * <ul>
	 * <li>base title: defined in i18n, can be overridden by a cookie setting</li>
	 * <li>main url under test: optional osm config that builds suffix for title</li>
	 * </ul>
	 * @return
	 */
	String getCsiChartDefaultTitle(){
		String baseChartTitle = osmCookieService.getBase64DecodedCookieValue(COOKIE_KEY_CSI_DASHBOARD_TITLE) ?:
			i18nService.msg('de.iteratec.isocsi.csi.defaultdashboard.chart.title', 'Customer Satisfaction (CSI)')
		String mainUrlUnderTest = configService.getMainUrlUnderTest()
		mainUrlUnderTest = mainUrlUnderTest ? " ${mainUrlUnderTest}" : ''
		return "${baseChartTitle}${mainUrlUnderTest}"
	}
	
}
