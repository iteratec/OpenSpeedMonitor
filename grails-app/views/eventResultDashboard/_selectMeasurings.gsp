<%@ page defaultCodec="none" %></page>
<%-- 
GSP-Template Mappings:

* folders List<JobGroup>: all Folders for selection 
* selectedFolder List<JobGroup>: all selected folders from past call

* pages List<Page>: all Pages for selection
* selectedPages List<Page>: all selected pages from past call

* measuredEvents List<MeasuredEvent>: see other
* selectedMeasuredEventIds List<MeasuredEvent>: see other
* selectedAllMeasuredEvents :

* browsers
* selectedBrowsers
* selectedAllBrowsers

* locations
* selectedLocations
* selectedAllLocations
 --%>
<div class="row">

    %{--JobGroups----------------------------------------------------------------------------------------------}%
    <div class="span4">
        <div style="padding-top: 60px;"></div>
        <label for="folderSelectHtmlId">
            <strong>
                <g:message code="de.iteratec.isr.wptrd.labels.filterFolder" default="Folder:"/>
            </strong>
        </label>
        <g:select id="folderSelectHtmlId" class="iteratec-element-select"
            name="selectedFolder" from="${folders}" optionKey="id"
            optionValue="name" value="${selectedFolder}"
            multiple="true"/>
        <div class="btn-group" data-toggle="buttons-radio">
                <div class="btn-group">
                    <button type="button" class="btn btn-mini" onclick="filterJobGroupSelect('')">
                        <i class="fa fa-remove"></i>&nbsp;<g:message code="de.iteratec.osm.ui.filter.clear" default="Clear filter"/>
                    </button>
                </div>
                <g:each in="${tagToJobGroupNameMap.keySet().collate(5)}" var="tagSubset">
                    <div class="btn-group-justified">
                        <div class="btn-group">
                            <g:each in="${tagSubset}" var="tag">
                                <button type="button" class="btn btn-mini" onclick="filterJobGroupSelect('${tag}')">
                                    <i class="fa fa-filter"></i>&nbsp;${tag}
                                </button>
                            </g:each>
                        </div>
                    </div>
                </g:each>
        </div>
    </div>

    %{--the rest----------------------------------------------------------------------------------------------}%
    <div id="filter-complete-tabbable">

        <div class="span7 tabbable">
            <ul class="nav nav-tabs">
                <li class="active" id="filter-navtab-page">
                    <a href="#tab1" data-toggle="tab">
                        <g:message code="de.iteratec.osm.result.page.label" default="Page"/>&nbsp;|&nbsp;<g:message code="de.iteratec.osm.result.measured-event.label" default="Measured step"/>
                    </a>
                </li>
                <li id="filter-navtab-browser-and-location">
                    <a href="#tab2" data-toggle="tab">
                        <g:message code="browser.label" default="Browser"/>&nbsp;|&nbsp;<g:message code="job.location.label" default="Location"/>
                    </a>
                </li>
                <g:if test="${showConnectivitySettings}">
                    <li>
                        <a href="#tab3" data-toggle="tab">
                            <g:message code="de.iteratec.osm.result.connectivity.label" default="Connectivity"/>
                        </a>
                    </li>
                </g:if>
            </ul>

            <div class="tab-content">
                <div class="tab-pane active" id="tab1">
                    <div class="span6">
                        <label for="pageSelectHtmlId">
                            <strong>
                                <g:message code="de.iteratec.isr.wptrd.labels.filterPage" default="Pages:"/>
                            </strong>
                        </label>
                        <g:select id="pageSelectHtmlId" class="iteratec-element-select" name="selectedPages"
                                  from="${pages}" optionKey="id" optionValue="name" multiple="true"
                                  value="${selectedPages}"/>
                    </div>
                    <div id="filter-measured-event" class="span6">
                        <label for="selectedMeasuredEventsHtmlId">
                            <strong>
                                <g:message code="de.iteratec.isr.wptrd.labels.filterMeasuredEvent"
                                           default="Measured Event:"/>
                            </strong>
                        </label>
                        <g:select id="selectedMeasuredEventsHtmlId"
                                  class="iteratec-element-select chosen"
                                  data-placeholder="${g.message(code: 'web.gui.jquery.chosen.multiselect.placeholdermessage', 'default': 'Please chose an option')}"
                                  name="selectedMeasuredEventIds"
                                  from="${measuredEvents}"
                                  optionKey="id"
                                  optionValue="name"
                                  value="${selectedMeasuredEventIds}"
                                  multiple="true"/>
                        <label class="checkbox inline">
                            <g:checkBox name="selectedAllMeasuredEvents"
                                        checked="${selectedAllMeasuredEvents}" value="${true}"/>
                            <g:message code="de.iteratec.isr.csi.eventResultDashboard.selectedAllMeasuredEvents.label"
                                       default="Select all measured steps"/>
                        </label>
                    </div>
                </div>

                <div class="tab-pane" id="tab2">
                    <div id="filter-browser-and-location">
                        <div class="span6">
                            <label for="selectedBrowsersHtmlId">
                                <strong>
                                    <g:message code="de.iteratec.isr.wptrd.labels.filterBrowser"
                                               default="Browser:"/>
                                </strong>
                            </label>
                            <g:select id="selectedBrowsersHtmlId"
                                      class="iteratec-element-select"
                                      name="selectedBrowsers" from="${browsers}" optionKey="id"
                                      optionValue="${{ it.name + ' (' + it.name + ')' }}" multiple="true"
                                      value="${selectedBrowsers}"/>
                            <label class="checkbox inline">
                                <g:checkBox name="selectedAllBrowsers"
                                            checked="${selectedAllBrowsers}" value="${true}"/>
                                <g:message code="de.iteratec.isr.csi.eventResultDashboard.selectedAllBrowsers.label"
                                           default="Select all Browsers"/>
                            </label>
                        </div>

                        <div class="span6">
                            <br>
                            <label for="selectedLocationsHtmlId">
                                <strong>
                                    <g:message code="de.iteratec.isr.wptrd.labels.filterLocation"
                                               default="Location:"/>
                                </strong>
                            </label>
                            <g:select id="selectedLocationsHtmlId"
                                      class="iteratec-element-select chosen" style="height: 150px; width:90%"
                                      data-placeholder="${g.message(code: 'web.gui.jquery.chosen.multiselect.placeholdermessage', 'default': 'Bitte ausw&auml;hlen')}"
                                      name="selectedLocations" from="${locations}" optionKey="id"
                                      optionValue="${it}"
                                      multiple="true" value="${selectedLocations}"/>
                            <br>
                            <label class="checkbox inline">
                                <g:checkBox name="selectedAllLocations"
                                            checked="${selectedAllLocations}" value="${true}"/>
                                <g:message code="de.iteratec.isr.csi.eventResultDashboard.selectedAllLocations.label"
                                           default="Select all locations"/>
                            </label>
                        </div>
                    </div>
                </div>
                <g:if test="${showConnectivitySettings}">
                    <div class="tab-pane" id="tab3">
                        <div class="span6">
                            <label for="selectedConnectivityProfilesHtmlId">
                                <strong>
                                    <g:message code="de.iteratec.isr.wptrd.labels.filterConnectivityProfile"
                                               default="Connectivity Profiles:"/>
                                    (<a href="${g.createLink(controller: 'ConnectivityProfile', action: 'list', absolute: true)}">
                                    <g:message code="de.iteratec.osm.result.predefined.linktext"
                                               default="predefined"/>
                                </a>):
                                </strong>
                            </label>
                            <g:select id="selectedConnectivityProfilesHtmlId"
                                      class="iteratec-element-select"
                                      name="selectedConnectivityProfiles" from="${connectivityProfiles}" optionKey="id"
                                      optionValue="name" multiple="true"
                                      value="${selectedConnectivityProfiles}"/>
                            <label class="checkbox inline">
                                <g:checkBox name="selectedAllConnectivityProfiles" id="selectedAllConnectivityProfiles"
                                            checked="${selectedAllConnectivityProfiles}" value="${true}"/>
                                <g:message code="de.iteratec.isr.csi.eventResultDashboard.selectedAllConnectivityProfiles.label"
                                           default="Select all Connectivity Profiles"/>
                            </label>
                            <g:if test="${showExtendedConnectivitySettings}">
                                <br>
                                <label class="checkbox inline">
                                    <g:checkBox name="includeNativeConnectivity" id="includeNativeConnectivity"
                                                checked="${includeNativeConnectivity}" value="${true}"/>
                                    <g:message code="de.iteratec.osm.result.include-native-connectivity.label"
                                               default="Show measurements with native connectivity"/>
                                </label>
                                <label for="customConnectivityName">
                                    <g:checkBox name="includeCustomConnectivity" id="includeCustomConnectivity"
                                                checked="${includeCustomConnectivity}" value="${true}"/>
                                    <g:message code="de.iteratec.osm.result.include-custom-connectivity.label"
                                               default="Show measurements with custom connectivity (filter by regex expression):"/>
                                </label>
                                <g:textField name="customConnectivityName" class="form-control input-xxlarge"
                                             value="${customConnectivityName}" id="customConnectivityName"
                                             placeholder="${g.message(code: 'de.iteratec.osm.result.filter-custom-connectivity.placeholder', default: 'Search via regex')}"
                                             disabled="${!includeCustomConnectivity}">
                                </g:textField>
                                <br>
                                <a href="${g.message(code: 'de.iteratec.osm.result.connectivity.regex.link.href', default: 'https://en.wikipedia.org/wiki/Regular_expression#Syntax')}"
                                   target="_blank">
                                    <g:message code="de.iteratec.osm.result.connectivity.regex.link.label" default="https://en.wikipedia.org/wiki/Regular_expression#Syntax" />
                                </a>
                            </g:if>
                        </div>
                    </div>
                </g:if>
            </div>
        </div>

    </div>

</div>
<asset:script type="text/javascript">

    var tagToJobGroupNameMap = ${tagToJobGroupNameMap as grails.converters.JSON};
    var jobGroupOptions = $('#folderSelectHtmlId option').clone();

    var filterJobGroupSelect = function(filterText){

        $('#folderSelectHtmlId').empty();

        var jobGroupNamesToShow = tagToJobGroupNameMap[filterText];
        jobGroupOptions.filter(function (idx, jobGroupOption) {
            return (filterText === '' || $.inArray($(jobGroupOption).text(), jobGroupNamesToShow) > -1);
        }).appendTo('#folderSelectHtmlId');

    }
</asset:script>