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

class HighchartHtmlCreater {

	private String createChartHtml(
			List<OsmChartGraph> data, String title, String lineType, String yType,
			String width,
			String yAxisMin, String yAxisMax, String divId, boolean useParseDateFunction,
			String measurementUnit,
			Long xAxisMin, Long xAxisMax,
			boolean markerEnabled,
			boolean dataLabelsActivated,
			boolean yAxisScalable,
			String lineWidthGlobal,
			boolean optimizeForExport,
			String heightOfChart,
			Boolean openDatapointLinksInNewWindow,
			String exportUrl) {
		def sw = new StringWriter()
		//		sw << """
		//			<script src="${resource(dir: 'js', file: 'highcharts.js')}" type="text/javascript"></script>
		//			<script src="${resource(dir: 'js', file: 'exporting.js')}" type="text/javascript"></script>
		//				"""

		sw << """<div id="${divId}" style="width: ${width};"></div>
			<script type="text/javascript">
				\$(document).ready(function() { 
					window.CHARTLIB = "HIGHCHARTS";
					var data = ["""

		for (OsmChartGraph eachGraph : data) {
			sw << """{name: "${eachGraph.getLabel()}",data:["""
			for (OsmChartPoint eachPoint : eachGraph.getPoints()) {
				if (useParseDateFunction) {
					sw << "[parseDate(\"" + eachPoint.time + "\")," + eachPoint.measuredValue + "],"
				}
				else {
					if( eachPoint.hasAnSourceURL() && openDatapointLinksInNewWindow )
					{
						sw << '{x:' + eachPoint.time + ',y:' + eachPoint.measuredValue + ',events:{click:function(e){window.open(\'' + eachPoint.sourceURL + '\');}}' + '},'
					}else if( eachPoint.hasAnSourceURL() && !openDatapointLinksInNewWindow ){
						sw << '{x:' + eachPoint.time + ',y:' + eachPoint.measuredValue + ',events:{click:function(e){window.location.href = \'' + eachPoint.sourceURL + '\';}}' + '},'
					}else {
						sw << '{x:' + eachPoint.time + ',y:' + eachPoint.measuredValue + '},'
					}
				}
			}
			sw << "]},";
		}

		if ((xAxisMin && xAxisMax)) {
			sw << """];
		createLineChart("${divId}", "${title}", "${yType}", data, ${yAxisMax}, "${lineType}",
			"${measurementUnit}", ${xAxisMin}, ${xAxisMax}, ${markerEnabled}, ${dataLabelsActivated}, ${yAxisScalable}, ${yAxisMin}, ${lineWidthGlobal}, ${optimizeForExport}, ${heightOfChart}, "${exportUrl}");
			});
		</script>"""
		}
		else {
			sw << """];
		createSplineChart("${divId}", "${title}", "${yType}", data, ${yAxisMax}, "${lineType}", "${measurementUnit}", "${exportUrl}");
			});
		</script>"""
		}

		if (log.debugEnabled) {
			log.debug "OsmChartTagLib\n${sw}"
		}
		
		return sw.toString()
	}

	/**
	 * TODO: Optimieren?
	 *
	 * @param mg
	 * @param highChartLabels
	 * @return
	 */
	private Integer getDataTypePos(MeasurandGroup mg, List<OsmChartAxis> highChartLabels) {

		if(mg==null) {
			return 0;
		}

		Integer i=0;
		for(OsmChartAxis entry: highChartLabels) {
			if(mg.equals(entry.getGroup())) {
				return i;
			}
			i++;
		}
		return 0;
	}

	/**
	 *
	 * @param labelToDataType
	 * @return
	 */
	private String getYTypesString(List <OsmChartAxis> labelToDataType) {
		String returner="[";
		labelToDataType.each { OsmChartAxis entry->
			returner+="{ \"label\": \""+entry.getLabelI18NIdentifier()+"\", \"labelPosition\": "+entry.getLabelPosition()+", \"unit\": \""+entry.getUnit()+"\", \"divisor\": "+entry.getDivisor()+", \"color\": \""+entry.getColor()+"\" },"
		}
		returner+="]"
		return returner;
	}

	/**
	 * Creates HTML/Javascript code to display values in a graph
	 */
	private String createChartHtmlMultipleYAxis(
			List<OsmChartGraph> data, String title, String lineType,
			String width,  String yAxisMin, String yAxisMaxs, String divId, boolean useParseDateFunction,
			String measurementUnits,
			Long xAxisMin, Long xAxisMax,
			boolean markerEnabled,
			boolean dataLabelsActivated,
			boolean yAxisScalable,
			String lineWidthGlobal,
			boolean optimizeForExport,
			List<OsmChartAxis> highChartLabels,
			Long highChartsTurboThreshold,
			Boolean openDatapointLinksInNewWindow,
			String exportUrl,
			String heightOfChart) {
		def sw = new StringWriter()

		boolean hasTimeType = getHasTimeType(highChartLabels)
		highChartLabels=getCleanedHighChartMap(data, highChartLabels);

		if (heightOfChart == null) {
			heightOfChart = '600px'
		}

		sw << """<div id="${divId}" style="width: ${width}; height: ${heightOfChart};"></div>
		<script type="text/javascript">
			\$(document).ready(function() {
				window.CHARTLIB = "HIGHCHARTS";
				var data = ["""

		Integer labelPos=0;
		for (OsmChartGraph eachGraph : data) {

			if(eachGraph.measurandGroup) {
				labelPos=getDataTypePos(eachGraph.measurandGroup, highChartLabels);
			}

			sw << """{name: "${eachGraph.label}",yAxis: ${labelPos}, data:["""
			for (OsmChartPoint eachPoint : eachGraph.points) {
				if (useParseDateFunction) {
					sw << "[parseDate(\"" + eachPoint.time + "\")," + eachPoint.measuredValue + "],"
				}
				else {
					if( eachPoint.hasAnSourceURL() && openDatapointLinksInNewWindow ) {
						sw << '{x:' + eachPoint.time + ',y:' + eachPoint.measuredValue + ',events:{click:function(e){window.open(\'' + eachPoint.sourceURL + '\');}}' + '},'
					}else if( eachPoint.hasAnSourceURL() && !openDatapointLinksInNewWindow ){
						sw << '{x:' + eachPoint.time + ',y:' + eachPoint.measuredValue + ',events:{click:function(e){window.location.href = \'' + eachPoint.sourceURL + '\';}}' + '},'
					}else {
						sw << '{x:' + eachPoint.time + ',y:' + eachPoint.measuredValue + '},'
					}
				}
			}
			sw << "]},";
		}

		def axisOpposite = ""
		axisOpposite += String.valueOf(false)

		String yTypes=getYTypesString(highChartLabels);

		//FIXME:
		if (true ||(xAxisMin && xAxisMax)) {
			sw << """];
				createLineChartWithMultipleYAxis("${divId}", "${title}", ${yTypes}, data, "${yAxisMaxs}", "${lineType}",
					"${measurementUnits}", ${xAxisMin}, ${xAxisMax}, ${markerEnabled}, ${dataLabelsActivated}, ${yAxisScalable}, ${yAxisMin}, ${lineWidthGlobal}, ${optimizeForExport}, ${highChartsTurboThreshold},
					"${exportUrl}");
					});
				</script>"""
		}

		if (log.debugEnabled) {
			log.debug "OsmChartTagLib\n${sw}"
		}
		return sw.toString()
	}

	/**
	 *
	 * @param data
	 * @param highChartLabels
	 * @return
	 */
	private List<OsmChartAxis> getCleanedHighChartMap(List<OsmChartGraph> data, List<OsmChartAxis> highChartLabels) {
		return highChartLabels;

		if(data == null || data.isEmpty()) {
			return highChartLabels;
		}

		def distinctTypes = []
		for (String graphLabel : data*.label) {
			if (!distinctTypes.contains(highChartLabels.get(graphLabel))) {
				distinctTypes.add(highChartLabels.get(graphLabel))
			}
		}
		return distinctTypes.size()
	}

	/**
	 *
	 * @param labelToDataType
	 * @return
	 */
	private boolean getHasTimeType(List<OsmChartAxis> labelToDataType) {
		return false;

		for (OsmChartAxis label: labelToDataType) {
			if (label == DataType.TIME) {
				return true;
			}
		}
		return false;
	}
}

