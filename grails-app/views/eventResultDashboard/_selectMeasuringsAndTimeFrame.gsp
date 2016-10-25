<div class="row">
    <div class="col-md-4">
        <div class="card form-horizontal">
            <legend>
                <g:message code="de.iteratec.isocsi.csi.aggreator.heading"
                           default="Aggregation"/>
                &amp;
                <g:message code="de.iteratec.isocsi.csi.timeframe.heading" default="Zeitraum" />
            </legend>

            <div class="form-group">
                <label class="col-md-4 control-label" for="selectedIntervalHtmlId">
                    <g:message
                            code="de.iteratec.isr.wptrd.labels.timeframes.interval"
                            default="Interval"/>:
                </label>

                <div class="col-md-8">
                    <g:select id="selectedIntervalHtmlId" class="form-control"
                              name="selectedInterval" from="${csiAggregationIntervals}"
                              valueMessagePrefix="de.iteratec.isr.wptrd.intervals"
                              value="${selectedInterval}"/>
                </div>
            </div>
            <g:render template="/dateSelection/startAndEnddateSelection"
                      model="${['selectedTimeFrameInterval': selectedTimeFrameInterval, 'from': from, 'fromHour': fromHour, 'to': to, 'toHour': toHour]}"/>
        </div>
    </div>
    <g:render template="selectMeasurings"
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