<r:require modules="iteratec-chart" />

<!-- TODO merge the single- and multiple-axis-chart-function of highchart -->
<g:if test="${singleYAxis}">
	<iteratec:singleYAxisChart data="${chartData}" yType="${yAxisLabel}"
		title="${chartTitle}" width="${initialChartWidth}"
		heightOfChart="${initialChartHeight}" measurementUnit="${chartUnit}"
		xAxisMin="${xAxisMin}" xAxisMax="${xAxisMax}"
		markerEnabled="${markerEnabled}"
		dataLabelsActivated="${dataLabelsActivated}"
		yAxisScalable="${yAxisScalable}" yAxisMin="${yAxisMin}"
		yAxisMax="${yAxisMax}" lineWidthGlobal="${lineWidthGlobal}"
		optimizeForExport="${optimizeForExport}"
		openDataPointLinksInNewWindow="${openDataPointLinksInNewWindow}"
		exportUrl="${exportUrl}" />
</g:if>
<g:else>
	<iteratec:multipleAxisChart data="${eventResultValues}" title="${chartTitle}"
		lineType="${selectedCharttypeForHighchart}" measurementUnit="s"
		lineWidthGlobal="2" xAxisMin="${fromTimestampForHighChart}"
		xAxisMax="${toTimestampForHighChart}"
		markerEnabled="${markerShouldBeEnabled}" dataLabelsActivated="false"
		yAxisScalable="false" optimizeForExport="false"
		highChartLabels="${highChartLabels}"
		highChartsTurboThreshold="${highChartsTurboThreshold}"
		exportUrl="${exportUrl}" />
</g:else>
<div class="row">
	<g:render template="/highchart/adjustChartAccordion" model="${model}" />
</div>

