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

package de.iteratec.osm.util

import grails.plugin.cookie.CookieService
import sun.misc.BASE64Decoder

/**
 * Decorates CookieService of grails Cookie plugin.
 * @author nkuhn
 * @see https://github.com/stokito/grails-cookie
 */
class OsmCookieService {


    CookieService cookieService

    /**
     * Gets the (Base64 decoded) value of the named cookie.
     * @param name Case-sensitive cookie name
     * @return Returns Base64 decoded cookie value or null if cookie does not exist
     */
    String getBase64DecodedCookieValue(String name) {

        String cookieRaw = cookieService.getCookie(name)
        log.debug("Cookie raw=${cookieRaw}")

        if (cookieRaw) {
            String cookieDecoded = new String(cookieRaw.decodeBase64())
            log.debug("Cookie url decoded=${cookieDecoded}")
            return cookieDecoded
        } else {
            return null
        }

    }
}
