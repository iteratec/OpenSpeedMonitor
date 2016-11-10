<%--
A row with the three cards: selectIntervalTimeframeCard, selectJobGroupCard, and selectPageLocationConnectivityCard
--%>
<div class="row">
    <div class="col-md-4">
        <g:render template="/_resultSelection/selectIntervalTimeframeCard"
                  model="${['selectedTimeFrameInterval': selectedTimeFrameInterval, 'from': from,
                            'fromHour': fromHour, 'to': to, 'toHour': toHour,
                            'csiAggregationIntervals': csiAggregationIntervals,
                            'selectedInterval': selectedInterval]}"/>
    </div>
    <g:render template="/_resultSelection/selectMeasurings"
              model="${['locationsOfBrowsers'             : locationsOfBrowsers,
                        'eventsOfPages'                   : eventsOfPages,
                        'folders'                         : folders,
                        'selectedFolder'                  : selectedFolder,
                        'pages'                           : pages,
                        'selectedPage'                    : selectedPage,
                        'measuredEvents'                  : measuredEvents,
                        'selectedAllMeasuredEvents'       : selectedAllMeasuredEvents,
                        'selectedMeasuredEvents'          : selectedMeasuredEvents,
                        'browsers'                        : browsers,
                        'selectedBrowsers'                : selectedBrowsers,
                        'selectedAllBrowsers'             : selectedAllBrowsers,
                        'locations'                       : locations,
                        'selectedLocations'               : selectedLocations,
                        'selectedAllLocations'            : selectedAllLocations,
                        'connectivityProfiles'            : connectivityProfiles,
                        'selectedConnectivityProfiles'    : selectedConnectivityProfiles,
                        'selectedAllConnectivityProfiles' : selectedAllConnectivityProfiles,
                        'showExtendedConnectivitySettings': true]}"/>
</div>