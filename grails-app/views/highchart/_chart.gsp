%{--TODO merge the single- and multiple-axis-chart-function of highchart--}%
<g:if test="${singleYAxis}">
	<iteratec:singleYAxisChart
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
        annotations="${annotations}"/>
</g:if>
<g:else>
	<iteratec:multipleAxisChart
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
		annotations="${annotations}"/>
</g:else>
<div class="row">
	<g:render template="/highchart/adjustChartAccordion" model="${model}" />
    <button class="span btn btn-primary" id="dia-save-chart-as-png" style="vertical-align: top;"><g:message code="de.iteratec.ism.ui.button.save.name"/></button>
</div>