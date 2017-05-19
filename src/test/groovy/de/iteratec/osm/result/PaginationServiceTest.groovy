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


package de.iteratec.osm.result

import org.joda.time.DateTime
import grails.web.mapping.LinkGenerator
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.report.ui.PaginationListing

/**
 * Test-suite of {@link de.iteratec.osm.result.PaginationService}.
 *
 * @author rhc
 */
@TestFor(PaginationService)
@Mock([Job])
@Build([Job])
class PaginationServiceTest extends Specification {

    PaginationService serviceUnderTest

    def setup() {
        serviceUnderTest = service
        serviceUnderTest.grailsLinkGenerator = Stub(LinkGenerator)
    }

    def "test correct amount of pagination links for result list"() {
        given: "a given amount of event results and a maximal amount of rows per page"
        int eventResultsTotalRecords = 100
        int maxRowsPerPage = 20
        TabularResultListResultsCommand cmd = new TabularResultListResultsCommand(
                max: maxRowsPerPage,
        )

        stubbedLinkGeneratorFromService(maxRowsPerPage)

        when: "the pagination links are generated"
        PaginationListing paginationListing = serviceUnderTest.buildListResultsPagination(cmd, eventResultsTotalRecords)

        then: "the amount of generated links is correct"
        paginationListing.isEmpty() == false
        paginationListing.getRows().size() == eventResultsTotalRecords / maxRowsPerPage
    }

    def "test correct amount of pagination links for job list"() {
        given: "a given amount of event results and a maximal amount of rows per page"
        int eventResultsTotalRecords = 100
        int maxRowsPerPage = 20
        TabularResultListResultsForSpecificJobCommand cmd = new TabularResultListResultsForSpecificJobCommand(
                job: Job.build(),
                max: maxRowsPerPage
        )

        stubbedLinkGeneratorFromService(maxRowsPerPage)

        when: "the pagination links are generated"
        PaginationListing paginationListing = serviceUnderTest.buildListResultsForJobPagination(cmd, eventResultsTotalRecords)

        then: "the amount of generated links is correct"
        paginationListing.isEmpty() == false
        paginationListing.getRows().size() == eventResultsTotalRecords / maxRowsPerPage
    }

    private void stubbedLinkGeneratorFromService(int maxRowsPerPage) {
        serviceUnderTest.grailsLinkGenerator = Stub(LinkGenerator) {
            link(_) >> "http://example.com/eventResultList?max=${maxRowsPerPage}"
        }
    }
}
