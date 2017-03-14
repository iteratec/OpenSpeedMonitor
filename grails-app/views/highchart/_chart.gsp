%{--TODO merge the single- and multiple-axis-chart-function of highchart--}%
<g:if test="${singleYAxis}">
	<iteratec:singleYAxisChart
		isAggregatedData="${isAggregatedData}"
        data="${chartData}"
        yType="${yAxisLabel}"
		title="${chartTitle}"
        width="${initialChartWidth}"
        labelSummary="${labelSummary}"
		heightOfChart="${initialChartHeight}"
        measurementUnit="${chartUnit}"
		xAxisMin="${xAxisMin}"
        xAxisMax="${xAxisMax}"
		markerEnabled="${markerEnabled}"
		dataLabelsActivated="${dataLabelsActivated}"
		yAxisScalable="${yAxisScalable}"
        yAxisMin="${yAxisMin}"
		yAxisMax="${yAxisMax}"
        lineWidthGlobal="${lineWidthGlobal}"
		optimizeForExport="${optimizeForExport}"
		openDataPointLinksInNewWindow="${openDataPointLinksInNewWindow}"
		exportUrl="${exportUrl}"
        annotations="${annotations}"
		downloadPngLabel="${downloadPngLabel}" />
</g:if>
<g:else>
	<iteratec:multipleAxisChart
		isAggregatedData="${isAggregatedData}"
		data="${chartData}"
        title="${chartTitle}"
		labelSummary="${labelSummary}"
		lineType="${selectedCharttypeForHighchart}"
        measurementUnit="s"
		lineWidthGlobal="2"
        xAxisMin="${fromTimestampForHighChart}"
		xAxisMax="${toTimestampForHighChart}"
		markerEnabled="${markerShouldBeEnabled}"
		dataLabelsActivated="${labelShouldBeEnabled}"
		yAxisScalable="false"
		yAxisMin="${yAxisMin}"
		yAxisMax="${yAxisMax}"
        optimizeForExport="false"
		highChartLabels="${highChartLabels}"
		highChartsTurboThreshold="${highChartsTurboThreshold}"
		exportUrl="${exportUrl}"
		heightOfChart="${initialChartHeight}"
		width="${initialChartWidth}"
		annotations="${annotations}"
		downloadPngLabel="${downloadPngLabel}" />
</g:else>
<g:render template="/highchart/adjustChartModal" model="${model}" />
