<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.detailAnalysis"/></title>
    <asset:javascript src="chartSwitch"/>
</head>

<body>
<h1><g:message code="de.iteratec.isocsi.detailAnalysis"/></h1>

<div class="row">
    <div class="col-md-12">
        <g:if test="${errorList && !errorList.empty}">
            <div class="alert alert-danger">
                <strong><g:message code="de.iteratec.isocsi.CsiDashboardController.selectionErrors.title"/></strong>
                <ul>
                    <g:each in="${errorList}">
                        <li><g:message error="${it}"/></li>
                    </g:each>
                </ul>
            </div>
        </g:if>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <g:if test="${startedBatchActivity != null}">
            <g:if test="${startedBatchActivity == true}">
                <div class="alert alert-info">
                    <g:message code="default.microService.osmDetailAnalysis.batchCreated"/>
                    <g:link controller="batchActivity">Batch Activity</g:link>
                </div>
            </g:if>
            <g:if test="${startedBatchActivity == false}">
                <div class="alert alert-danger">
                    <g:message code="default.microService.osmDetailAnalysis.batchNotCreated"/>
                </div>
            </g:if>
        </g:if>
    </div>
</div>

<g:if test="${osmDetailAnalysisRequest}">
    <div id="detailDatenContainer">${osmDetailAnalysisRequest}</div>
</g:if>

<form method="get">
    %{--show button--}%
    <div class="action-row">
        <div class="col-md-12">
            <div class="btn-group pull-right" id="show-button-group">
                <g:actionSubmit value="${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default': 'Show')}"
                                action="show" id="showDetailDashboardButton" class="btn btn-primary show-button"/>
                <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_SUPER_ADMIN">
                    <button type="button" class="btn btn-primary show-button dropdown-toggle" data-toggle="dropdown"
                            aria-haspopup="true" aria-expanded="false">
                        <span class="caret"></span>
                        <span class="sr-only">Toggle Dropdown</span>
                    </button>
                    <ul class="dropdown-menu">
                        <li>
                            <g:actionSubmit
                                    value="${g.message(code: 'de.iteratec.ism.ui.labels.show.loadAssets', 'default': 'Load Assets')}"
                                    action="sendFetchAssetsAsBatchCommand"
                                    class="show-button"/>
                        </li>
                    </ul>
                </sec:ifAnyGranted>
            </div>
            <g:render template="/_resultSelection/hiddenWarnings" />
        </div>
    </div>

    <div class="row card-well">
        <div class="col-md-4">
            <g:render template="/_resultSelection/selectIntervalTimeframeCard"
                      model="${['selectedTimeFrameInterval': selectedTimeFrameInterval, 'from': from,
                                'fromHour'                 : fromHour, 'to': to, 'toHour': toHour, 'showIncludeInterval': false,
                                'includeInterval'          : includeInterval]}"/>
        </div>

        <div class="col-md-4">

            <div id="filter-navtab-jobGroup">
                <g:render template="/_resultSelection/selectJobGroupCard"
                          model="['folders'             : folders, 'selectedFolder': selectedFolder,
                                  'tagToJobGroupNameMap': tagToJobGroupNameMap]"/>
            </div>
        </div>
        %{--the rest----------------------------------------------------------------------------------------------}%
        <div id="filter-complete-tabbable" class="col-md-4">
            <g:render template="/_resultSelection/selectPageLocationConnectivityCard" model="[
                    'showOnlyPage'         : true,
                    'hideMeasuredEventForm': true,
                    'pages'                : pages,
                    'selectedPages'        : selectedPages
            ]"/>
        </div>
    </div>
    <button class="reset-result-selection btn btn-default btn-sm" type="button" title="Reset">
        <i class="fa fa-undo"></i> Reset
    </button>
</form>

<content tag="include.bottom">
    <asset:javascript src="eventresultdashboard/showAll.js"/>

    <asset:script type="text/javascript">
        $(window).load(function() {
          OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/resultSelection.js"
                                                                   />')
        });
        OpenSpeedMonitor.ChartModules.UrlHandling.ChartSwitch("${createLink(action: 'showAll', controller: 'eventResultDashboard')}",
            "${createLink(action: 'show', controller: 'pageAggregation')}",
            "${createLink(action: 'listResults', controller: 'tabularResultPresentation')}",
            "${createLink(action: 'getPagesForMeasuredEvents', controller: 'page')}",
            "${createLink(action: 'show', controller: 'detailAnalysis')}").init();
    </asset:script>
</content>

</body>
</html>
