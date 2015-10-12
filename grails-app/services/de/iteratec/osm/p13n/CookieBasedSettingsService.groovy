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

package de.iteratec.osm.p13n

import de.iteratec.osm.util.OsmCookieService

import static de.iteratec.osm.util.Constants.*

import de.iteratec.osm.report.chart.ChartingLibrary

/**
 * CookieBasedSettingsService
 * Provides settings that can be configured user-defined by setting cookies.
 * @author nkuhn
 */
class CookieBasedSettingsService {

    static transactional = false

    OsmCookieService osmCookieService
    def grailsApplication

    /**
     * Gets charting library to use. Value is read by cookie value or from static grails config as default.
     * @return
     * @deprecated since 2015-10-08
     *      Highcharts as charting library was removed so set charting lib to use as a cookie based p13n isn't
     *      necessary anymore.
     */
    @Deprecated
    ChartingLibrary getChartingLibraryToUse() {
        String cookieValue = osmCookieService.getBase64DecodedCookieValue(COOKIE_KEY_CHARTING_LIB_TO_USE)
        log.debug("cookie-value while getting charting library to use: ${cookieValue}")
        if (cookieValue != null){
            return ChartingLibrary."$cookieValue"
        } else {
            //default
            return grailsApplication.config.grails.de.iteratec.osm.report.chart.chartTagLib
        }
    }
}
