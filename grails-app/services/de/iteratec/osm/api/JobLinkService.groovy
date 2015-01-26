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

package de.iteratec.osm.api

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.script.ScriptParser
import de.iteratec.osm.result.PageService
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.joda.time.DateTime

/**
 * JobResultLinkService
 * A service class encapsulates the core business logic of a Grails application
 */
class JobLinkService {

    static transactional = true

    LinkGenerator grailsLinkGenerator
    PageService pageService

    String getLinkToTabularResultsFor(Job job, GString fromFormatted, GString fromHourFormatted, GString toFormatted, GString toHourFormatted) {
        return grailsLinkGenerator.link(
            controller: 'eventResult',
            action: 'showListResultsForJob',
            absolute: true,
            params: [
                'selectedTimeFrameInterval':0,
                'job.id':job.id,
                'from':fromFormatted,
                'fromHour': fromHourFormatted,
                'to':toFormatted,
                'toHour':toHourFormatted,
                'setFromHour': 'on',
                'setToHour': 'on'
            ]
        )
    }

    Map<String,String> getResultVisualizingLinksFor(Job job, DateTime start, DateTime end) {

        ScriptParser parser = new ScriptParser(job.script.getParsedNavigationScript(job))
        List<Long> eventIds = parser.getMeasuredEvents()*.id
        List<Long> pageIds = parser.getTestedPages()*.id

        log.debug("parser.eventNames=$parser.eventNames")
        log.debug("eventIds=$eventIds")
        log.debug("pageIds=$pageIds")

        GString fromFormatted = "${start.getDayOfMonth()}.${start.getMonthOfYear()}.${start.getYear()}"
        GString fromHourFormatted = "${start.getHourOfDay()}:${String.format("%02d", start.getMinuteOfHour())}"
        GString toFormatted = "${end.getDayOfMonth()}.${end.getMonthOfYear()}.${end.getYear()}"
        GString toHourFormatted = "${end.getHourOfDay()}:${String.format("%02d", end.getMinuteOfHour())}"

        String resultsChartUrl = 'Pages and/or MeasuredEvents tested in submitted job couldn\'t be detected by navigation script.'
        String csiChartUrl = 'Pages and/or MeasuredEvents tested in submitted job couldn\'t be detected by navigation script.'

        if (eventIds && pageIds){
            resultsChartUrl = grailsLinkGenerator.link(controller: 'eventResultDashboard', action: 'showAll', absolute: true,
                    params: [
                            'selectedInterval':-1,
                            'selectedTimeFrameInterval': 0,
                            'from': fromFormatted,
                            'fromHour': fromHourFormatted,
                            'to': toFormatted,
                            'toHour': toHourFormatted,
                            'setFromHour': 'on',
                            'setToHour': 'on',
                            'selectedFolder': job.jobGroup.id,
                            'selectedPage': pageIds,
                            'selectedBrowsers': job.location.browser.id,
                            'selectedMeasuredEventIds': eventIds,
                            'selectedLocations': job.location.id,
                            'selectedAggrGroupValuesUnCached': 'docCompleteTimeInMillisecsUncached',
                            '_action_showAll': 'Anzeigen',
                            'selectedChartType': 0
                    ])
            csiChartUrl = grailsLinkGenerator.link(controller: 'csiDashboard', action: 'showAll', absolute: true,
                    params: [
                            'aggrGroup':'measuredEvent',
                            'selectedTimeFrameInterval': 0,
                            'from': fromFormatted,
                            'fromHour': fromHourFormatted,
                            'to': toFormatted,
                            'toHour': toHourFormatted,
                            'setFromHour': 'on',
                            'setToHour': 'on',
                            'selectedFolder': job.jobGroup.id,
                            'selectedPage': pageIds,
                            'selectedBrowsers': job.location.browser.id,
                            'selectedMeasuredEventIds': eventIds,
                            'selectedLocations': job.location.id,
                            '_action_showAll': 'Anzeigen'
                    ])
        }
        return [
                'results-chart-url': resultsChartUrl,
                'csi-chart-url': csiChartUrl,
                'results-tabular-url': getLinkToTabularResultsFor(job, fromFormatted, fromHourFormatted, toFormatted, toHourFormatted)
        ]
    }
}
