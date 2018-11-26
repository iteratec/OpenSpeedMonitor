<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="layoutOsm"/>
    <title><g:message code="de.iteratec.isocsi.pageComparision.title" default="Page Comparison"/></title>
    <asset:stylesheet src="/d3Charts/barChartHorizontal.less"/>
    <asset:stylesheet src="/csiBenchmark/show.less"/>

</head>

<body>
<g:render template="/chart/chartSwitchButtons" model="['currentChartName': 'pageComparison']"/>
<p>
    <g:message code="de.iteratec.isocsi.pageComparison.description"
               default="The displayed chart shows the differences between two pages. Usually used to compare pages as 'entry page' and as 'follow page'."/>
</p>

<div class="card hidden" id="chart-card">
    <div id="error-div" class="hidden">
        <div class="alert alert-danger">
            <div id="error-message"></div>
        </div>
    </div>
    <g:render template="barChart"/>
</div>

<div class="row">
    <div class="col-md-12">
        <form id="dashBoardParamsForm">
            <div class="action-row">
                <div class="col-md-12">
                    <div class="btn-group pull-right" id="show-button-group">
                        <button type="button" id="graphButtonHtmlId"
                                class="btn btn-primary">
                            ${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default': 'Show')}</button>
                    </div>
                    <g:render template="/_resultSelection/hiddenWarnings"/>
                </div>
            </div>

            <div class="card-well">
                <div class="row">
                    <div class="col-md-7">
                        <sitemesh:parameter name="needsAngular" value="true"/>
                        <osm-page-comparison
                                data-module-path="src/app/modules/page-comparison/page-comparison.module#PageComparisonModule"></osm-page-comparison>
                    </div>

                    <div class="col-md-5">
                        <g:render template="/_resultSelection/selectBarchartMeasurings" model="[
                                aggrGroupValuesUnCached          : aggrGroupValuesUnCached,
                                multipleMeasurands               : false,
                                multipleSeries                   : false,
                                'selectedAggrGroupValuesUnCached': selectedAggrGroupValuesUnCached
                        ]"/>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-4">
                        <g:render template="/_resultSelection/selectIntervalTimeframeCard"
                                  model="${['selectedTimeFrameInterval': selectedTimeFrameInterval, 'from': from,
                                            'fromHour'                 : fromHour, 'to': to, 'toHour': toHour, 'showIncludeInterval': false,
                                            'includeInterval'          : includeInterval,
                                            hideHourSelection          : true]}"/>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>
<g:render template="/_common/modals/downloadAsPngDialog" model="['chartContainerID': 'svg-container']"/>

<content tag="include.bottom">
    <asset:javascript src="chartSwitch"/>
    <asset:script type="text/javascript">
        $(window).on('load', function() {
             OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="pageComparison/pageComparison.js"/>', "pageComparison");
             OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/resultSelection.js"/>', "resultSelection");
        });
    </asset:script>
</content>

</body>
</html>
