<%@ page contentType="text/html;charset=UTF-8"%>
<html>
    <head>
        <meta name="layout" content="kickstart_osm" />
        <title><g:message code="de.iteratec.isocsi.eventResult"/></title>
        <asset:stylesheet src="tabularResultPresentation/listResults.css"/>
    </head>
    <body>

        <%-- main menu --%>
        <g:render template="/layouts/mainMenu"/>

        <g:if test="${command}">
            <g:hasErrors bean="${command}">
                <div class="alert alert-danger">
                    <strong><g:message
                            code="de.iteratec.isocsi.CsiDashboardController.selectionErrors.title"
                            default="You missed something on selection" /></strong>
                    <ul>
                        <g:eachError var="eachError" bean="${command}">
                            <li><g:message error="${eachError}" /></li>
                        </g:eachError>
                    </ul>
                </div>
            </g:hasErrors>
        </g:if>
        <form method="get" action="">
            <div class="action-row">
                <div class="col-md-12">
                    <g:actionSubmit class="btn btn-primary pull-right" id="show-button"
                                    value="${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default':'Show')}"
                                    action="${ showSpecificJob ? 'listResultsForJob' : 'listResults'}" />
                    <g:render template="/_resultSelection/hiddenWarnings" />
                </div>
            </div>
            <div class="row card-well">
                <div class="col-md-4">
                    <g:render template="/_resultSelection/selectIntervalTimeframeCard"
                        model="${['selectedTimeFrameInterval':selectedTimeFrameInterval, 'from':from,
                                  'fromHour':fromHour, 'to':to, 'toHour':toHour, 'dateFormat': dateFormat,
                                  'weekStart': weekStart]}"/>
                </div>
                <g:if test="${showSpecificJob}">
                    <g:render template="filterJob" model="${['job': job]}" />
                    <input type="hidden" name="job.id" value="${job?.id}" />
                </g:if>
                <g:else>
                    <g:render
                          template="/_resultSelection/selectMeasurings"
                          model="${['locationsOfBrowsers'               :locationsOfBrowsers,
                                    'eventsOfPages'                     :eventsOfPages,
                                    'folders'                           :csiGroups,
                                    'selectedFolder'                    :selectedFolder,
                                    'pages'                             :pages,
                                    'selectedPage'                      :selectedPage,
                                    'measuredEvents'                    :measuredEvents,
                                    'selectedAllMeasuredEvents'         :selectedAllMeasuredEvents,
                                    'selectedMeasuredEvents'            :selectedMeasuredEvents,
                                    'browsers'                          :browsers,
                                    'selectedBrowsers'                  :selectedBrowsers,
                                    'selectedAllBrowsers'               :selectedAllBrowsers,
                                    'locations'                         :locations,
                                    'selectedLocations'                 :selectedLocations,
                                    'selectedAllLocations'              :selectedAllLocations,
                                    'connectivityProfiles'              :connectivityProfiles,
                                    'selectedConnectivityProfiles'      :selectedConnectivityProfiles,
                                    'selectedAllConnectivityProfiles'   :selectedAllConnectivityProfiles]}"/>
                </g:else>
            </div>
        </form>

        <g:if test="showEventResultsListing">

            <g:render template="listResults"
                  model="${[model: eventResultsListing]}" />

            <g:render template="pagination"
                model="${[model: paginationListing] }" />

        </g:if>
        <content tag="include.bottom">
            <asset:javascript src="eventresult/eventResult.js"/>
            <asset:script type="text/javascript">
                $(window).load(function() {
                   OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/resultSelection.js" absolute="true"/>')
                });
            </asset:script>
        </content>
    </body>
</html>
