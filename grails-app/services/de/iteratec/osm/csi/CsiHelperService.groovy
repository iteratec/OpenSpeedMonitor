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

import java.util.regex.Pattern

import org.joda.time.DateTime

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobGroupType
import de.iteratec.osm.report.chart.MeasuredValueInterval


class CsiHelperService {
	
	TimeToCsMappingService timeToCsMappingService

	/**
	 * Resets actualDateTime to the start of the interval in which it is.
	 * @param actualDateTime
	 * @param intervalInMinutes
	 * @return
	 * @deprecated Use {@link de.iteratec.osm.report.chart.MeasuredValueUtilService#resetToStartOfActualInterval(DateTime, Integer)} instead
	 */
	@Deprecated
    DateTime resetToStartOfActualInterval(DateTime actualDateTime, Integer intervalInMinutes){
		DateTime startOfInterval = actualDateTime
		switch (intervalInMinutes) {
			case MeasuredValueInterval.WEEKLY:									startOfInterval=startOfInterval.withDayOfWeek(1)
			case MeasuredValueInterval.DAILY:
			case MeasuredValueInterval.WEEKLY:									startOfInterval=startOfInterval.withHourOfDay(0)
			default:															startOfInterval=startOfInterval.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
		}
		return startOfInterval
	}
	/**
	 * Resets actualDateTime to the end of the interval in which it is.
	 * @param actualDateTime
	 * @param intervalInMinutes
	 * @return
	 * @deprecated Use {@link de.iteratec.osm.report.chart.MeasuredValueUtilService#resetToEndOfActualInterval(DateTime, Integer)} instead
	 */
	@Deprecated
	DateTime resetToEndOfActualInterval(DateTime actualDateTime, Integer intervalInMinutes){
		DateTime endOfInterval = actualDateTime
		return resetToStartOfActualInterval(actualDateTime, intervalInMinutes).plusMinutes(intervalInMinutes)
	}
	
	JobGroup getCsiJobGroup(){
		return JobGroup.findByName('CSI')
	}
	List<JobGroup> getCsiJobGroups(){
		return JobGroup.findAllByGroupType(JobGroupType.CSI_AGGREGATION)
	}
	
	String calculateCsisForCsv(String pathToSourceCsv){
		File file = new File(pathToSourceCsv)
		String newContent = "job;id;date;time;validationState;resultStatus;resultURL;FV_Doc;customerSatisfaction\n"
		String newLine = ""
		file.eachLine{line ->
			if (!line.startsWith("Job;")) {
				def tokenized = line.tokenize(";")
				if (tokenized[7]) {
					Page page
					switch (tokenized[0].toLowerCase()){
						case {Pattern.matches('.*step01.*', it)}: page = Page.findByName('HP') ; break
						case {Pattern.matches('.*step02.*', it)}: page = Page.findByName('MES') ; break
						case {Pattern.matches('.*step03.*', it)}: page = Page.findByName('SE') ; break
						case {Pattern.matches('.*step04.*', it)}: page = Page.findByName('ADS') ; break
						case {Pattern.matches('.*step05.*', it)}: page = Page.findByName('WKBS') ; break
						case {Pattern.matches('.*step06.*', it)}: page = Page.findByName('WK') ; break
					}
					if (page) {
						newLine = "${tokenized[0]};${tokenized[1]};${tokenized[2]};${tokenized[3]};${tokenized[4]};${tokenized[5]};${tokenized[6]};${tokenized[7]};"+
								timeToCsMappingService.getCustomerSatisfactionInPercent(Integer.valueOf(tokenized[7]), page) + "\n"
						log.info("newLine=${newLine}")
						newContent += newLine
					}else{
						newLine = "${line}\n"
						log.info("newLine=${newLine}")
						newContent += newLine
					}
				}else{
					newLine = "${line}\n"
					log.info("newLine=${newLine}")
					newContent += newLine
				}
			}
		}
		File out = new File('/home/nkuhn/tempOutOfTruecrypt/weekly_new.csv')
		out << newContent
		return newContent
	}
	
}
