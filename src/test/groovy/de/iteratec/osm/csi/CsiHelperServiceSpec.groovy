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

import de.iteratec.osm.ConfigService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.OsmCookieService
import grails.buildtestdata.BuildDataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class CsiHelperServiceSpec extends Specification implements BuildDataTest, ServiceUnitTest<CsiHelperService> {
    void setupSpec() {
        mockDomains(ConnectivityProfile, Script)
    }

    void "check getCsiChartDefaultTitle"(String fromConfig, String fromCookie, String fromI18n, int expectedI18nInterations, String expectedOutcome) {
        setup: "init Mocks and specify wanted invocations"
        ConfigService configService = Mock(ConfigService) {
            1 * getMainUrlUnderTest() >> fromConfig
        }
        OsmCookieService osmCookieService = Mock(OsmCookieService) {
            1 * getBase64DecodedCookieValue(_) >> fromCookie
        }
        I18nService i18nService = Mock(I18nService) {
            expectedI18nInterations * msg(_, _) >> fromI18n
        }

        when: "mocks are set"
        service.configService = configService
        service.osmCookieService = osmCookieService
        service.i18nService = i18nService

        then: "chart default title is built accordingly"
        service.getCsiChartDefaultTitle().equals(expectedOutcome)

        where:
        fromConfig | fromCookie | fromI18n | expectedI18nInterations | expectedOutcome
        "config"   | "cookie"   | "i18n"   | 0                       | "cookie config"
        "config"   | null       | null     | 1                       | "null config"
        null       | "cookie"   | null     | 0                       | "cookie"
        null       | null       | "i18n"   | 1                       | "i18n"
        "config"   | "cookie"   | null     | 0                       | "cookie config"
        "config"   | null       | "i18n"   | 1                       | "i18n config"
        null       | "cookie"   | "i18n"   | 0                       | "cookie"
    }
}
