<r:require modules="iteratec-chart" />


%{--TODO merge the single- and multiple-axis-chart-function of highchart--}%
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
		exportUrl="${exportUrl}"
    annotations="${annotations}"/>
</g:if>
<g:else>
	<iteratec:multipleAxisChart data="${eventResultValues}" title="${chartTitle}"
		lineType="${selectedCharttypeForHighchart}" measurementUnit="s"
		lineWidthGlobal="2" xAxisMin="${fromTimestampForHighChart}"
		xAxisMax="${toTimestampForHighChart}"
		markerEnabled="${markerShouldBeEnabled}"
		dataLabelsActivated="${labelShouldBeEnabled}"
		yAxisScalable="false" optimizeForExport="false"
		highChartLabels="${highChartLabels}"
		highChartsTurboThreshold="${highChartsTurboThreshold}"
		exportUrl="${exportUrl}"
		heightOfChart="${initialChartHeight}"
		width="${initialChartWidth}"
		annotations="${annotations}"/>
</g:else>
<div class="row">
	<g:render template="/highchart/adjustChartAccordion" model="${model}" />

<script type="text/javascript">
$(document).ready(function () {
  if (navigator.userAgent.indexOf('MSIE') !== -1 || navigator.appVersion.indexOf('Trident/') > 0 || navigator.appVersion.indexOf('Edge/') > 0) {
  //if ($.browser.mozilla) {
    $("#dia-save-chart-as-png").removeClass("btn-primary");
    $("#dia-save-chart-as-png").addClass("btn-primary.disabled");
    $("#dia-save-chart-as-png").attr( "disabled", "disabled" );
    $("#dia-save-chart-as-png").attr( "title", "<g:message code="de.iteratec.ism.ui.button.save.disabled.tooltip"/>" );
   }
});
</script>
  <button class="span btn btn-primary" id="dia-save-chart-as-png" style="vertical-align: top;"><g:message code="de.iteratec.ism.ui.button.save.name"/></button>
</div>