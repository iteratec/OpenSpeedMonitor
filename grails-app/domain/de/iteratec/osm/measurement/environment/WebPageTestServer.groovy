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

package de.iteratec.osm.measurement.environment

import groovy.transform.EqualsAndHashCode

/**
 * This class represents the PHP-application webpagetest-server (see http://webpagetest.org)
 * @author nkuhn
 */
@EqualsAndHashCode
class WebPageTestServer {

    String label
    String proxyIdentifier

    Date	dateCreated
    Date	lastUpdated
    Long	id

    String baseUrl
    Boolean active
    String description
    String contactPersonName
    String contactPersonEmail

    static constraints = {
        label(blank: false, maxSize: 150)
        proxyIdentifier(maxSize: 255)
        active(nullable: false)
        description(nullable: true, widget: 'textarea', maxSize: 255)
        baseUrl(blank: false, url: ["localhost(:(\\d{1,5}))?"], maxSize: 255)
        contactPersonName(maxSize: 200, nullable: true)
        contactPersonEmail(email: true, nullable: true, maxSize: 255)
    }

    public String getBaseUrl() {
        if (baseUrl) {
            return baseUrl.endsWith('/') ? "${baseUrl}" : "${baseUrl}/"
        } else {
            return ""
        }
    }

    static mapping = {
        //locations cascade: "all-delete-orphan"
    }

    @Override
    public String toString() {
        return "${this.label}";
    }
}