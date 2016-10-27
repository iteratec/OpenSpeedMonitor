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
            <div class="row card-well">
                <div class="col-md-4">
                    <g:render template="/dateSelection/startAndEnddateSelection"
                        model="${['selectedTimeFrameInterval':selectedTimeFrameInterval, 'from':from,
                                  'fromHour':fromHour, 'to':to, 'toHour':toHour]}"/>
                </div>
                <g:if test="${showSpecificJob}">
                    <g:render template="filterJob" model="${['job': job]}" />
                    <input type="hidden" name="job.id" value="${job?.id}" />
                </g:if>
                <g:else>
                    <g:render
                          template="/eventResultDashboard/selectMeasurings"
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
                                    'selectedAllConnectivityProfiles'   :selectedAllConnectivityProfiles,
                                    'showExtendedConnectivitySettings'  : true]}"/>
                </g:else>
            </div>
            <div class="row section">
                <div class="col-md-12">
                    <g:actionSubmit
                        value="${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default':'Show')}"
                        action="${ showSpecificJob ? 'listResultsForJob' : 'listResults'}" class="btn btn-primary" />
                </div>
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

                var pagesToEvents = [];
                <g:each var="page" in="${pages}">
                    <g:if test="${eventsOfPages[page.id] != null}">
                        pagesToEvents[${page.id}]= [<g:each var="event" in="${eventsOfPages[page.id]}">${event},</g:each>];
                    </g:if>
                </g:each>

                var browserToLocation = [];
                <g:each var="browser" in="${browsers}">
                    <g:if test="${locationsOfBrowsers[browser.id] != null}">
                        browserToLocation[${browser.id}]=[ <g:each var="location"
                                                                   in="${locationsOfBrowsers[browser.id]}">${location},</g:each> ];
                    </g:if>
                </g:each>

                initSelectMeasuringsControls(pagesToEvents, browserToLocation, allMeasuredEventElements, allBrowsers, allLocations);

                $(document).ready(
                    doOnDomReady(
                        '${dateFormat}',
                        ${weekStart},
                        '${g.message(code: 'web.gui.jquery.chosen.multiselect.noresultstext', 'default':'Keine Eintr&auml;ge gefunden f&uuml;r ')}'
                    )
                );
            </asset:script>
        </content>
    </body>
</html>