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

    def "test build list results pagination"() {
        given: "a command for the tabular result list"
        TabularResultListResultsCommand cmd = new TabularResultListResultsCommand()
        cmd.setFrom(new DateTime(2014, 8, 8, 4, 0))
        cmd.setTo(new DateTime(2014, 8, 8, 6, 0))
        cmd.setSelectedFolder([2L])
        cmd.setSelectedPages([1L])
        cmd.setSelectedBrowsers([3L])
        cmd.setSelectedMeasuredEventIds([])

        stubbedLinkFromService("http://example.com/eventResult/listResults?selectedTimeFrameInterval=0&from=2014-08-08T04%3A00%3A00.000Z&to=2014-08-08T06%3A00%3A00.000Z&selectedFolder=2&selectedPages=1&selectedBrowsers=3&_action_listResults=Show")

        when: "buildListResultsPagination is called"
        PaginationListing paginationListing = serviceUnderTest.buildListResultsPagination(cmd, 100)


        then: "one gets the correct parameters to build the pagination for result list table"
        Map<String, List<String>> queryParams = getQueryParams(paginationListing.getRows().get(0).pageLink)
        paginationListing.getRows().size() == 2
        paginationListing.getRows().get(0).pageNumber == 1
        queryParams.get("from").get(0) == "2014-08-08T04:00:00.000Z"
        queryParams.get("to").get(0) == "2014-08-08T06:00:00.000Z"
        queryParams.get("selectedFolder").toString() == cmd.getSelectedFolder().toString()
        queryParams.get("selectedPages").toString() == cmd.getSelectedPages().toString()
        queryParams.get("selectedBrowsers").toString() == cmd.getSelectedBrowsers().toString()
    }

    def "test build list results for job pagination"() {
        given: "a command for the job list"
        TabularResultListResultsForSpecificJobCommand cmd = new TabularResultListResultsForSpecificJobCommand()

        Job job = Job.build()
        cmd.setJob(job)
        cmd.setFrom(new DateTime(2014, 8, 7, 4, 0))
        cmd.setTo(new DateTime(2014, 8, 8, 4, 0))
        cmd.setMax(50)
        cmd.setOffset(0)

        stubbedLinkFromService("http://example.com/eventResult/showListResultsForJob?selectedTimeFrameInterval=0&job.id=1&from=2014-08-07T04%3A00%3A00.000Z&to=2014-08-08T04%3A00%3A00.000Z")

        when:"when buildListResultsForJobPagination is called"
        PaginationListing paginationListing = serviceUnderTest.buildListResultsForJobPagination(cmd, 100)


        then: "one gets the correct parameters to build the pagination for job list table"
        Map<String, List<String>> queryParams = getQueryParams(paginationListing.getRows().get(0).pageLink)

        paginationListing.getRows().size() == 2
        paginationListing.getRows().get(0).pageNumber == 1
        queryParams.get("from").get(0) == "2014-08-07T04:00:00.000Z"
        queryParams.get("to").get(0) == "2014-08-08T04:00:00.000Z"
        queryParams.get("job.id").get(0).toString() == cmd.getJob().getId().toString()
    }

    private void stubbedLinkFromService(String linkAsString) {
        serviceUnderTest.grailsLinkGenerator = Stub(LinkGenerator) {
            link(_) >> linkAsString
        }
    }

    /**
     * Gives params from given url {@link String}.
     * @param url
     * 		URL as {@link String}
     * @return
     */
    private static Map<String, List<String>> getQueryParams(String url) {
        try {
            Map<String, List<String>> params = new HashMap<String, List<String>>()
            String[] urlParts = url.split("\\?")
            if (urlParts.length > 1) {
                String query = urlParts[1]
                for (String param : query.split("&")) {
                    String[] pair = param.split("=")
                    String key = URLDecoder.decode(pair[0], "UTF-8")
                    String value = ""
                    if (pair.length > 1) {
                        value = URLDecoder.decode(pair[1], "UTF-8")
                    }

                    List<String> values = params.get(key)
                    if (values == null) {
                        values = new ArrayList<String>()
                        params.put(key, values)
                    }
                    values.add(value)
                }
            }

            return params
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex)
        }
    }
}
