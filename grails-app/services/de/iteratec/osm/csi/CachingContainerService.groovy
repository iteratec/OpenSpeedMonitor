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

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.CsiAggregationTagService
import org.joda.time.DateTime

/**
 * CachingContainerService
 * A service class encapsulates the core business logic of a Grails application
 */
class CachingContainerService {
    CsiAggregationTagService csiAggregationTagService
    PageCsiAggregationService pageCsiAggregationService

    CsiAggregationCachingContainer createContainerFor(CsiAggregation mv, Map<Long, JobGroup> cachedJobGroups, Map<Long, Page> cachedPages, Map<String, List<CsiAggregation>> hemvs) {
        return new CsiAggregationCachingContainer(
                csiGroupToCalcCsiAggregationFor: cachedJobGroups[csiAggregationTagService.getJobGroupIdFromWeeklyOrDailyPageTag(mv.tag)],
                pageToCalcCsiAggregationFor: cachedPages[csiAggregationTagService.getPageIdFromWeeklyOrDailyPageTag(mv.tag)],
                hCsiAggregationsByCsiGroupPageCombination: hemvs)
    }

    Map<String, Map<String, List<CsiAggregation>>> getDailyHeCsiAggregationMapByStartDate(List<CsiAggregation> dailyPageCsiAggregationsToCalculate,
                                                                                          Map<String, List<JobGroup>> dailyJobGroupsByStartDate, Map<String, List<Page>> dailyPagesByStartDate) {
        Map<String, Map<String, List<CsiAggregation>>> result = [:].withDefault {[:]}

        dailyPageCsiAggregationsToCalculate*.started.unique().each { Date uniqueStartDate ->

            DateTime startForGettingHemv = new DateTime(uniqueStartDate)
            String uniqueStartDateAsString = uniqueStartDate.toString()

            Map<String, List<CsiAggregation>> hemv = pageCsiAggregationService.getHmvsByCsiGroupPageCombinationMap(
                    dailyJobGroupsByStartDate[uniqueStartDateAsString].unique(),
                    dailyPagesByStartDate[uniqueStartDateAsString].unique(),
                    startForGettingHemv,
                    startForGettingHemv.plusMinutes(CsiAggregationInterval.DAILY))

            result.put(uniqueStartDateAsString, hemv)
        }

        return result
    }

    Map<String, List<JobGroup>> getDailyJobGroupsByStartDate(List<CsiAggregation> dailyPageMvsToCalculate, Map<Long, JobGroup> cachedJobGroups) {
        Map<String, List<JobGroup>> result = [:].withDefault { key -> [] }

        dailyPageMvsToCalculate.each { dpmv ->
            Long jobGroupID = csiAggregationTagService.getJobGroupIdFromWeeklyOrDailyPageTag(dpmv.tag)

            JobGroup jobGroup = cachedJobGroups[jobGroupID]

            result[dpmv.started.toString()].add(jobGroup)
        }

        return result
    }

    Map<String, List<Page>> getDailyPagesByStartDate(List<CsiAggregation> dailyPageMvsToCalculate, Map<Long, Page> cachedPages) {
        Map<String, List<Page>> result = [:].withDefault { key -> [] }

        dailyPageMvsToCalculate.each { dpmv ->
            Long pageID = csiAggregationTagService.getPageIdFromWeeklyOrDailyPageTag(dpmv.tag)

            Page page = cachedPages[pageID]

            result[dpmv.started.toString()].add(page)
        }

        return result
    }

    Map<String, List<JobGroup>> getWeeklyJobGroupsByStartDate(List<CsiAggregation> weeklyPageMvsToCalculate, Map<Long, JobGroup> cachedJobGroups) {
        Map<String, List<JobGroup>> result = [:].withDefault { key -> [] }
        weeklyPageMvsToCalculate.each { wpmv ->

            JobGroup jobGroup = cachedJobGroups[csiAggregationTagService.getJobGroupIdFromWeeklyOrDailyPageTag(wpmv.tag)]

            result[wpmv.started.toString()].add(jobGroup)
        }

        return result
    }

    Map<String, List<Page>> getWeeklyPagesByStartDate(List<CsiAggregation> weeklyPageMvsToCalculate, Map<Long, Page> cachedPages) {
        Map<String, List<Page>> result = [:].withDefault { key -> [] }
        weeklyPageMvsToCalculate.each { wpmv ->
            Page page = cachedPages[csiAggregationTagService.getPageIdFromWeeklyOrDailyPageTag(wpmv.tag)]

            result[wpmv.started.toString()].add(page)
        }
        return result
    }

    Map<String, Map<String, List<CsiAggregation>>> getWeeklyHeCsiAggregationMapByStartDate(List<CsiAggregation> weeklyPageCsiAggregationsToCalculate,
                                                                                           Map<String, List<JobGroup>> weeklyJobGroupsByStartDate, Map<String, List<Page>> weeklyPagesByStartDate) {
        Map<String, Map<String, List<CsiAggregation>>> result = [:]

        weeklyPageCsiAggregationsToCalculate*.started.unique().each { Date uniqueStartDate ->

            DateTime startForGettingHemv = new DateTime(uniqueStartDate)
            String uniqueStartDateAsString = uniqueStartDate.toString()

            result[uniqueStartDateAsString] = pageCsiAggregationService.getHmvsByCsiGroupPageCombinationMap(
                    weeklyJobGroupsByStartDate[uniqueStartDateAsString].unique(),
                    weeklyPagesByStartDate[uniqueStartDateAsString].unique(),
                    startForGettingHemv,
                    startForGettingHemv.plusMinutes(CsiAggregationInterval.WEEKLY))
        }

        return result
    }
}
