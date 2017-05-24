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

package de.iteratec.osm

import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ConfigService)
@Build([OsmConfiguration])
@Mock([OsmConfiguration])
class ConfigServiceSpec extends Specification {
    static final Integer DEFAULT_MAX_DOWNLOAD_TIME_IN_MINUTES = 60
    static final Integer DEFAULT_MIN_DOCCOMPLETE_TIME_IN_MILLISECS = 250
    static final Integer DEFAULT_MAX_DOCCOMPLETE_TIME_IN_MILLISECS = 180000
    static final Integer DEFAULT_INITIAL_CHART_HEIGHT_IN_PIXELS = 400

    void "one config is fine"() {
        when: "only one config has been saved with a value"
        OsmConfiguration.build(detailDataStorageTimeInWeeks: 4)

        then: "config service hat its values including the defaults"
        service.getDetailDataStorageTimeInWeeks() == 4
        service.getDefaultMaxDownloadTimeInMinutes() == DEFAULT_MAX_DOWNLOAD_TIME_IN_MINUTES
        service.getMinDocCompleteTimeInMillisecs() == DEFAULT_MIN_DOCCOMPLETE_TIME_IN_MILLISECS
        service.getMaxDocCompleteTimeInMillisecs() == DEFAULT_MAX_DOCCOMPLETE_TIME_IN_MILLISECS
        service.getInitialChartHeightInPixels() == DEFAULT_INITIAL_CHART_HEIGHT_IN_PIXELS
    }

    void "more than one config is bad"() {
        given: "two configs have been saved"
        2.times { OsmConfiguration.build() }

        when: "trying to access any value from config service"
        service.getDetailDataStorageTimeInWeeks()

        then: "an illegalStateExpection should be thrown"
        thrown(IllegalStateException)
    }

    void "values in config can be changed"(int from, int to) {
        given: "value in config has been set"
        OsmConfiguration conf = OsmConfiguration.build(detailDataStorageTimeInWeeks: from)

        when: "value is changed in config"
        conf.detailDataStorageTimeInWeeks = to

        then: "config service has the new value"
        service.getDetailDataStorageTimeInWeeks() == to

        where:
        from | to
        1    | 2
        3    | 4
        5    | 1
    }

    void "Failure with no config"() {
        given: "no config"

        when: "getting DetailDataStorageTimeInWeeks"
        service.getDetailDataStorageTimeInWeeks()

        then: "should fail with IllegalStateException"
        thrown(IllegalStateException)
    }

}
