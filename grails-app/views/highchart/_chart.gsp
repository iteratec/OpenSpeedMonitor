<iteratec:timeSeriesChart
	divId="graph_container"
	isAggregatedData="${isAggregatedData}"
	data="${chartData}"
	title="${chartTitle}"
	labelSummary="${labelSummary}"
	markerEnabled="${showDataMarkers}"
	dataLabelsActivated="${showDataLabels}"
	highChartLabels="${highChartLabels}"
	heightOfChart="${initialChartHeight}"
	width="${initialChartWidth}"
	annotations="${annotations}"
	yAxisMax="${yAxisMax}"
	yAxisMin="${yAxisMin}"
	downloadPngLabel="${downloadPngLabel}" />
<g:render template="/highchart/adjustChartModal" model="${model}" />
