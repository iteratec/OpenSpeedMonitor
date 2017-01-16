<%@ page defaultCodec="none" %></page>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isocsi.detailAnalysis"/></title>
</head>

<body>

<%-- main menu --%>
<g:render template="/layouts/mainMenu"/>
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
    <div class="row">
        <div class="col-md-12">
            %{--show button--}%
            <div class="btn-group pull-right" id="show-button-group">
                <g:actionSubmit value="${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default': 'Show')}"
                                action="show" id="showDetailDashboardButton" class="btn btn-primary"/>
                <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_SUPER_ADMIN">
                    <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown"
                            aria-haspopup="true" aria-expanded="false">
                        <span class="caret"></span>
                        <span class="sr-only">Toggle Dropdown</span>
                    </button>
                    <ul class="dropdown-menu">
                        <li>
                            <g:actionSubmit
                                    value="${g.message(code: 'de.iteratec.ism.ui.labels.show.loadAssets', 'default': 'Load Assets')}"
                                    action="sendFetchAssetsAsBatchCommand"/>
                        </li>
                    </ul>
                </sec:ifAnyGranted>
            </div>
            <!-- Actual tabs -->
            <ul class="nav nav-tabs card-well-tabs" id="erd-card-tabs">
                <li class="active">
                    <a data-toggle="tab" href="#tabJobSelection" id="tabJobSelectionElement">
                        <g:message code="de.iteratec.sri.wptrd.time.filter.heading"
                                   default="Zeitraum ausw&auml;hlen"/>
                        &amp;
                        <g:message code="de.iteratec.sri.wptrd.jobs.filter.heading"
                                   default="Jobs filtern"/>
                    </a>
                </li>
            </ul>

            <div class="tab-content card-well">
                <div class="tab-pane in active" id="tabJobSelection">
                    <g:render template="/_resultSelection/selectMeasuringsAndTimeFrame"
                              model="${['from'                           : from,
                                        'fromHour'                       : fromHour,
                                        'to'                             : to,
                                        'toHour'                         : toHour,
                                        'dateFormat'                     : dateFormat,
                                        'weekStart'                      : weekStart,
                                        'locationsOfBrowsers'            : locationsOfBrowsers,
                                        'eventsOfPages'                  : eventsOfPages,
                                        'folders'                        : folders,
                                        'selectedFolder'                 : selectedFolder,
                                        'pages'                          : pages,
                                        'selectedPage'                   : selectedPage,
                                        'measuredEvents'                 : measuredEvents,
                                        'selectedAllMeasuredEvents'      : selectedAllMeasuredEvents,
                                        'selectedMeasuredEvents'         : selectedMeasuredEvents,
                                        'browsers'                       : browsers,
                                        'selectedBrowsers'               : selectedBrowsers,
                                        'selectedAllBrowsers'            : selectedAllBrowsers,
                                        'locations'                      : locations,
                                        'selectedLocations'              : selectedLocations,
                                        'selectedAllLocations'           : selectedAllLocations,
                                        'connectivityProfiles'           : connectivityProfiles,
                                        'selectedConnectivityProfiles'   : selectedConnectivityProfiles,
                                        'selectedAllConnectivityProfiles': selectedAllConnectivityProfiles]}"/>
                </div>
            </div>
        </div>
    </div>
</form>

<content tag="include.bottom">
    <asset:javascript src="eventresultdashboard/showAll.js"/>

    <asset:script type="text/javascript">
        $(document).ready(function () {

            doOnDomReady(
                    '${g.message(code: 'web.gui.jquery.chosen.multiselect.noresultstext', 'default': 'Keine Eintr&auml;ge gefunden f&uuml;r ')}'
            );
        });
    </asset:script>
</content>

</body>
</html>
