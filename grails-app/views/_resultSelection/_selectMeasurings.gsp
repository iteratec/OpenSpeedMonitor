<%@ page defaultCodec="none" %></page>
<%--
Two columns (span 3 and span 5) to with cards selectJobGroupCard and selectPageLocationConnectivityCard


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
<div class="col-md-3" id="filter-navtab-jobGroup">
    <g:render template="/_resultSelection/selectJobGroupCard"
              model="['folders': folders, 'selectedFolder': selectedFolder, 'tagToJobGroupNameMap': tagToJobGroupNameMap]"/>
</div>

%{--the rest----------------------------------------------------------------------------------------------}%
<div id="filter-complete-tabbable" class="col-md-5">
    <g:render template="/_resultSelection/selectPageLocationConnectivityCard"
              model="['locationsOfBrowsers'            : locationsOfBrowsers,
                      'eventsOfPages'                  : eventsOfPages,
                      'pages'                          : pages,
                      'selectedPages'                  : selectedPages,
                      'measuredEvents'                 : measuredEvents,
                      'selectedAllMeasuredEvents'      : selectedAllMeasuredEvents,
                      'selectedMeasuredEvents'         : selectedMeasuredEvents,
                      'browsers'                       : browsers,
                      'selectedBrowsers'               : selectedBrowsers,
                      'selectedAllBrowsers'            : selectedAllBrowsers,
                      'locations'                      : locations,
                      'selectedLocations'              : selectedLocations,
                      'selectedAllLocations'           : selectedAllLocations,
                      avaiableConnectivities           : avaiableConnectivities,
                      'selectedConnectivityProfiles'   : selectedConnectivityProfiles,
                      'selectedAllConnectivityProfiles': selectedAllConnectivityProfiles]"/>
</div>
