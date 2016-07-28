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

package de.iteratec.osm.report.chart

import grails.transaction.Transactional

import java.util.Map.Entry;

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 * 
 * 
 * <p>
 * TODO mze-2013-07-15: This service seems to translate Model-data to view-data. Move to controller? 
 * </p>
 */
@Transactional
class CsiAggregationToHighChartService {

	static final String HIGHCHART_LABEL_MESSAGE_CODE = "de.iteratec.isocsi.csi.highchart.label"

	/** injected by grails */
	def messageSource
	def nameMap = [:]

	/**
	 * Converts List of {@ CsiAggregation}s to Map in format
	 *
	 * [
	 *   label1: [timestamp1:customerSatisfaction1, ..., timestampN:customerSatisfactionN,],
	 *   ...,
	 *   labelN: [...]
	 * ]
	 * 
	 * Parameter aggregationIdToLabel is a Map which contains the MapLabel mapped to JobId, PageID or ResultId depending on given AggregationType
	 * 
	 * @param csiValues
	 * @param aggregationspecificIdToLabel
	 * @return
	 */
	Map convertToHighChartMap(List<CsiAggregation> mesValues, AggregatorType aggregator, Map<String,String> aggregationspecificIdToLabelMap, Map<String,DataType> labelToDataTypeMap) {
		def resultMap = [:]

		addAggregationspecificLabelsToNameMaps(aggregator, aggregationspecificIdToLabelMap)
		
		mesValues.each { curMesVal ->
			def jobLabel = getMapLabel(curMesVal)

			if (!resultMap.containsKey(jobLabel)) {
				resultMap.put(jobLabel, [:])
			}

			Map mesValueMap = resultMap.get(jobLabel)
			curMesVal.started.getTime()
			Long curTimestamp = getHighchartCompatibleTimestampFrom(curMesVal.started)
			if (curMesVal.csByWptDocCompleteInPercent) {
				if (aggregator.name.equals(AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME)) {
					/*Result Dashboard*/
					DataType dataType = labelToDataTypeMap.get(jobLabel)
					if (dataType == DataType.TIME) {
						mesValueMap.put(curTimestamp, (curMesVal.csByWptDocCompleteInPercent / 1000))
					} else {
						mesValueMap.put(curTimestamp, curMesVal.csByWptDocCompleteInPercent)
					}
				} else {
					/*CSI Dashboard*/
					// TODO mal 100 wegen Anzeige in Sekunden?
					def val = curMesVal.csByWptDocCompleteInPercent * 100
					/*
					 * round to 2 decimal places
					 */
					BigDecimal bd = new BigDecimal(val)
					BigDecimal rounded = bd.setScale(2, BigDecimal.ROUND_HALF_UP)
					def roundedVal = rounded.doubleValue()
					mesValueMap.put(curTimestamp, roundedVal)
				}
			}
			resultMap.put(jobLabel, mesValueMap.sort())
		}


		resultMap = resultMap.sort()

		return resultMap
	}
	
	private void addAggregationspecificLabelsToNameMaps(AggregatorType aggregator, Map<String,String> aggregationIdToLabel) {
		def labelMap = [:]
		if (!nameMap.containsKey(aggregator.name)) {
			nameMap.put(aggregator.name, labelMap)
		} else {
			labelMap = nameMap.get(aggregator.name)
		}
		for (Entry<String, String> entry : aggregationIdToLabel.entrySet()) {
			if (!labelMap.containsKey(entry.key)) {
				labelMap.put(entry.key, entry.value)
			}
		}
		
	}

	/**
	 * Get label for Map of {@link CustomerSatisfactionHighChartService#getOrCalculateCustomerSatisfactionCsiAggregationsAsHighChartMap}
	 * for given {@link CsiAggregation} and {@link AggregatorType}
	 * 
	 * @param mv
	 * @param aggregator
	 * @return Label for Map of {@link CustomerSatisfactionHighChartService#getOrCalculateCustomerSatisfactionCsiAggregationsAsHighChartMap}
	 */
	private String getMapLabel(CsiAggregation mv) {

		def labelMap = nameMap.get(mv.getAggregator().name)
		
		switch (mv.getAggregator().name) {
			case AggregatorType.MEASURED_EVENT:
				def jobId = Long.valueOf(mv.tag.tokenize(";")[0])

				def jobLabel = labelMap.get(jobId)

				return jobLabel
				break
			case AggregatorType.PAGE:
				def pageId = Long.valueOf(mv.tag)

				def pageLabel = labelMap.get(pageId)

				return pageLabel
				break
			case AggregatorType.SHOP:
				Object[] emptyArgs = {}
				String mailSubject =
						messageSource.resolveCode(HIGHCHART_LABEL_MESSAGE_CODE, new java.util.Locale("EN")).format(emptyArgs)
				return mailSubject
				break
			case AggregatorType.RESULT_UNCACHED_DOC_COMPLETE_TIME:
				def resultLabel = labelMap.get(mv.tag)
				return resultLabel
				break				
		}
	}

	private Long getHighchartCompatibleTimestampFrom(Date date){
		return new DateTime(date, DateTimeZone.forID('MET')).getMillis()
	}
}
