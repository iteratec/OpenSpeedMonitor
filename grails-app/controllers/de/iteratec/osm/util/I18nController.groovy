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

import grails.converters.JSON
import org.springframework.web.servlet.support.RequestContextUtils as RCU

/**
 * I18nController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class I18nController {
    def messageSource
    static defaultAction = "index"

    //TODO: The following method should provide the i18n messages to publish in javascript code by ajax call
    // see http://www.razum.si/blog/grails-javascript-i18n-messages
    // Doesn't work cause of this bug: https://issues.apache.org/jira/browse/GROOVY-7295
    //Alternative solution would require to write a plugin: https://sergiosmind.wordpress.com/2013/07/25/getting-all-i18n-messages-in-javascript/
    def getAllMessages() {
//        def keys = messageSource.withTraits(MessagePropertiesTrait).getMessageKeys(RCU.getLocale(request))
//        def jsKeys = keys.findAll { code, value -> code.startsWith('js.') }
//        render keys as JSON
    }
}
