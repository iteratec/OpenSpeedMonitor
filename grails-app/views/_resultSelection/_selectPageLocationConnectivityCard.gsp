<%@ page defaultCodec="none" %></page>
<%--
A card to select page & measured step, browser & location, and the connectivity
--%>

<div class="card" id="select-page-location-connectivity">
    <g:if test="${showOnlyPage}">
        <legend>
            <button class="reset-selection soft-button" type="button" title="Reset"><i class="fa fa-undo"></i></button>
            <g:message code="de.iteratec.osm.result.page.label" default="Page"/>
            <g:if test="${!hideMeasuredEventForm}">
                &nbsp;|&nbsp;<g:message code="de.iteratec.osm.result.measured-event.label" default="Measured step"/>
            </g:if>
        </legend>
    </g:if>
    <g:else>
    <button class="reset-selection soft-button" type="button" title="Reset"><i class="fa fa-undo"></i></button>
    <ul class="nav nav-tabs">
        <li class="active" id="filter-navtab-page">
            <a href="#page-tab" data-toggle="tab">
                <g:message code="de.iteratec.osm.result.page.label" default="Page"/>&nbsp;|&nbsp;<g:message code="de.iteratec.osm.result.measured-event.label" default="Measured step"/>
            </a>
        </li>
        <li id="filter-navtab-browser-and-location">
            <a href="#browser-tab" data-toggle="tab">
                <g:message code="browser.label" default="Browser"/>&nbsp;|&nbsp;<g:message code="job.location.label" default="Location"/>
            </a>
        </li>
        <li id="filter-navtab-connectivityprofile">
            <a href="#connectivity-tab" data-toggle="tab">
                <g:message code="de.iteratec.osm.result.connectivity.label" default="Connectivity"/>
            </a>
        </li>
    </ul>
    </g:else>

    <div class="tab-content">
        <div class="tab-pane active" id="page-tab">
            <g:render template="/_resultSelection/selectPageContent" model="[
                    hideMeasuredEventForm: hideMeasuredEventForm,
                    pages: pages,
                    selectedPages: selectedPages,
                    measuredEvents: measuredEvents,
                    selectedMeasuredEventIds: selectedMeasuredEventIds,
                    selectedAllMeasuredEvents: selectedAllMeasuredEvents,
                    eventsOfPages: eventsOfPages,
            ]" />
        </div>
        <g:if test="${!showOnlyPage}">
            <div class="tab-pane" id="browser-tab">
                <div id="filter-browser-and-location">
                    <g:select id="selectedBrowsersHtmlId"
                              class="form-control"
                              name="selectedBrowsers" from="${browsers}" optionKey="id"
                              optionValue="${{ it.name + ' (' + it.name + ')' }}" multiple="true"
                              value="${selectedBrowsers}"
                              title="${message(code:'de.iteratec.isr.wptrd.labels.filterBrowser')}" />
                    <label class="checkbox-inline">
                        <g:checkBox name="selectedAllBrowsers"
                                    checked="${selectedAllBrowsers}" value="${true}"/>
                        <g:message code="de.iteratec.isr.csi.eventResultDashboard.selectedAllBrowsers.label"
                                   default="Select all Browsers"/>
                    </label>

                    <br>
                    <label for="selectedLocationsHtmlId">
                        <strong>
                            <g:message code="de.iteratec.isr.wptrd.labels.filterLocation"
                                       default="Location:"/>
                        </strong>
                    </label>
                    <g:select id="selectedLocationsHtmlId"
                              class="chosen"
                              data-parent-child-mapping='${locationsOfBrowsers as grails.converters.JSON}'
                              data-placeholder="${g.message(code: 'web.gui.jquery.chosen.multiselect.placeholdermessage', 'default': 'Bitte ausw&auml;hlen')}"
                              name="selectedLocations" from="${locations}" optionKey="id"
                              optionValue="${it}"
                              multiple="true" value="${selectedLocations}"/>
                    <br>
                    <label class="checkbox-inline">
                        <g:checkBox name="selectedAllLocations"
                                    checked="${selectedAllLocations}" value="${true}"/>
                        <g:message code="de.iteratec.isr.csi.eventResultDashboard.selectedAllLocations.label"
                                   default="Select all locations"/>
                    </label>
                </div>
            </div>
            <div class="tab-pane" id="connectivity-tab">
                <div id="filter-connectivityprofile">
                    <g:select id="selectedConnectivityProfilesHtmlId"
                              class="form-control"
                              name="selectedConnectivityProfiles" from="${connectivityProfiles}" optionKey="id"
                              multiple="true"
                              value="${selectedConnectivityProfiles}"
                              title="${message(code:'de.iteratec.isr.wptrd.labels.filterConnectivityProfile')}" />
                    <label class="checkbox-inline">
                        <g:checkBox name="selectedAllConnectivityProfiles" id="selectedAllConnectivityProfiles"
                                    checked="${selectedAllConnectivityProfiles}" value="${true}"/>
                        <g:message code="de.iteratec.isr.csi.eventResultDashboard.selectedAllConnectivityProfiles.label"
                                   default="Select all Connectivity Profiles"/>
                    </label>
                    <input type="hidden" name="includeNativeConnectivity" id="includeNativeConnectivity"
                           value="${includeCustomConnectivity ? true : false}" />
                    <input type="hidden" name="includeCustomConnectivity" id="includeCustomConnectivity"
                           value="${includeCustomConnectivity ? true : false}" />
                    <input type="hidden" name="customConnectivityName" id="customConnectivityName"
                           value="${customConnectivityName}" />
                </div>
            </div>
        </g:if>
    </div>
</div>
<asset:script type="text/javascript">
    $(window).load(function() {
        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/selectPageLocationConnectivityCard.js" absolute="true"/>');
    });
</asset:script>
