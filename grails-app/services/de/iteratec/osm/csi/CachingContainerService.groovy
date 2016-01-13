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
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.result.MeasuredValueTagService
import org.joda.time.DateTime

/**
 * CachingContainerService
 * A service class encapsulates the core business logic of a Grails application
 */
class CachingContainerService {
    MeasuredValueTagService measuredValueTagService
    PageMeasuredValueService pageMeasuredValueService

    MvCachingContainer createContainerFor(MeasuredValue mv, Map<Long, JobGroup> cachedJobGroups, Map<Long, Page> cachedPages, Map<String, List<MeasuredValue>> hemvs) {
        return new MvCachingContainer(
                csiGroupToCalcMvFor: cachedJobGroups[measuredValueTagService.getJobGroupIdFromWeeklyOrDailyPageTag(mv.tag)],
                pageToCalcMvFor: cachedPages[measuredValueTagService.getPageIdFromWeeklyOrDailyPageTag(mv.tag)],
                hmvsByCsiGroupPageCombination: hemvs)
    }

    Map<String, Map<String, List<MeasuredValue>>> getDailyHemvMapByStartDate(List<MeasuredValue> dailyPageMeasuredValuesToCalculate,
                                                                             Map<String, List<JobGroup>> dailyJobGroupsByStartDate, Map<String, List<Page>> dailyPagesByStartDate) {
        Map<String, Map<String, List<MeasuredValue>>> result = [:].withDefault {[:]}

        dailyPageMeasuredValuesToCalculate*.started.unique().each { Date uniqueStartDate ->

            DateTime startForGettingHemv = new DateTime(uniqueStartDate)
            String uniqueStartDateAsString = uniqueStartDate.toString()

            Map<String, List<MeasuredValue>> hemv = pageMeasuredValueService.getHmvsByCsiGroupPageCombinationMap(
                    dailyJobGroupsByStartDate[uniqueStartDateAsString].unique(),
                    dailyPagesByStartDate[uniqueStartDateAsString].unique(),
                    startForGettingHemv,
                    startForGettingHemv.plusMinutes(MeasuredValueInterval.DAILY))

            result.put(uniqueStartDateAsString, hemv)
        }

        return result
    }

    Map<String, List<JobGroup>> getDailyJobGroupsByStartDate(List<MeasuredValue> dailyPageMvsToCalculate, Map<Long, JobGroup> cachedJobGroups) {
        Map<String, List<JobGroup>> result = [:].withDefault { key -> [] }

        dailyPageMvsToCalculate.each { dpmv ->
            Long jobGroupID = measuredValueTagService.getJobGroupIdFromWeeklyOrDailyPageTag(dpmv.tag)

            JobGroup jobGroup = cachedJobGroups[jobGroupID]

            result[dpmv.started.toString()].add(jobGroup)
        }

        return result
    }

    Map<String, List<Page>> getDailyPagesByStartDate(List<MeasuredValue> dailyPageMvsToCalculate, Map<Long, Page> cachedPages) {
        Map<String, List<Page>> result = [:].withDefault { key -> [] }

        dailyPageMvsToCalculate.each { dpmv ->
            Long pageID = measuredValueTagService.getPageIdFromWeeklyOrDailyPageTag(dpmv.tag)

            Page page = cachedPages[pageID]

            result[dpmv.started.toString()].add(page)
        }

        return result
    }

    Map<String, List<JobGroup>> getWeeklyJobGroupsByStartDate(List<MeasuredValue> weeklyPageMvsToCalculate, Map<Long, JobGroup> cachedJobGroups) {
        Map<String, List<JobGroup>> result = [:].withDefault { key -> [] }
        weeklyPageMvsToCalculate.each { wpmv ->

            JobGroup jobGroup = cachedJobGroups[measuredValueTagService.getJobGroupIdFromWeeklyOrDailyPageTag(wpmv.tag)]

            result[wpmv.started.toString()].add(jobGroup)
        }

        return result
    }

    Map<String, List<Page>> getWeeklyPagesByStartDate(List<MeasuredValue> weeklyPageMvsToCalculate, Map<Long, Page> cachedPages) {
        Map<String, List<Page>> result = [:].withDefault { key -> [] }
        weeklyPageMvsToCalculate.each { wpmv ->
            Page page = cachedPages[measuredValueTagService.getPageIdFromWeeklyOrDailyPageTag(wpmv.tag)]

            result[wpmv.started.toString()].add(page)
        }
        return result
    }

    Map<String, Map<String, List<MeasuredValue>>> getWeeklyHemvMapByStartDate(List<MeasuredValue> weeklyPageMeasuredValuesToCalculate,
                                                                              Map<String, List<JobGroup>> weeklyJobGroupsByStartDate, Map<String, List<Page>> weeklyPagesByStartDate) {
        Map<String, Map<String, List<MeasuredValue>>> result = [:]

        weeklyPageMeasuredValuesToCalculate*.started.unique().each { Date uniqueStartDate ->

            DateTime startForGettingHemv = new DateTime(uniqueStartDate)
            String uniqueStartDateAsString = uniqueStartDate.toString()

            result[uniqueStartDateAsString] = pageMeasuredValueService.getHmvsByCsiGroupPageCombinationMap(
                    weeklyJobGroupsByStartDate[uniqueStartDateAsString].unique(),
                    weeklyPagesByStartDate[uniqueStartDateAsString].unique(),
                    startForGettingHemv,
                    startForGettingHemv.plusMinutes(MeasuredValueInterval.WEEKLY))
        }

        return result
    }
}
