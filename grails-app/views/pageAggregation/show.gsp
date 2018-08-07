<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.pageAggregation" default="Page Aggregation"/></title>
    <asset:stylesheet src="/pageAggregation/show.css"/>
</head>

<body>
<g:render template="/chart/chartSwitchButtons" model="['currentChartName': 'pageAggregation']"/>
<p>
    <g:message code="de.iteratec.isocsi.pageAggregation.description.short"
               default="The webpagetest raw data of the respective interval is the basis for the displayed mean values."/>
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
            <!-- show button -->
            <div class="action-row">
                <div class="col-md-12">

                    <div class="btn-group pull-right" id="show-button-group">
                        <button type="button" id="graphButtonHtmlId" class="btn btn-primary show-button">
                            ${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default': 'Show')}
                        </button>
                    </div>
                    <g:render template="/_resultSelection/hiddenWarnings"/>
                </div>
            </div>

            <div class="row card-well">
                <div class="col-md-4">
                    <g:render template="/_resultSelection/selectIntervalTimeframeCard"
                              model="${[
                                'selectedTimeFrameInterval': selectedTimeFrameInterval, 'from': from, 'to': to,
                                'showIncludeInterval': false,
                                'showComparisonInterval': true
                              ]}"/>

                    <g:render template="/_resultSelection/selectBarchartMeasurings" model="[
                            aggrGroupValuesUnCached: aggrGroupValuesUnCached,
                            multipleMeasurands     : true
                    ]"/>
                </div>

                <div class="col-md-3">

                    <div id="filter-navtab-jobGroup">
                        <g:render template="/_resultSelection/selectJobGroupCard"
                                  model="['folders'             : folders, 'selectedFolder': selectedFolder,
                                          'tagToJobGroupNameMap': tagToJobGroupNameMap]"/>
                    </div>
                </div>
                %{--the rest----------------------------------------------------------------------------------------------}%
                <div id="filter-complete-tabbable" class="col-md-5">
                    <g:render template="/_resultSelection/selectPageLocationConnectivityCard" model="[
                            'showOnlyPage'         : true,
                            'hideMeasuredEventForm': true,
                            'pages'                : pages,
                            'selectedPages'        : selectedPages
                    ]"/>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <button class="reset-result-selection btn btn-default btn-sm" type="button" title="Reset">
                            <i class="fas fa-undo"></i> Reset
                        </button>
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
            OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="pageAggregation/pageAggregation.js"/>', "pageAggregation");
            OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/resultSelection.js"/>', "resultSelection");
        });
    </asset:script>
</content>

</body>
</html>
