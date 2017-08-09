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

<div class="card" id="chart-card">
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
                        <a href="#" type="button" onClick="drawGraph()" id="graphButtonHtmlId"
                           class="btn btn-primary show-button">
                            ${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default': 'Show')}</a>
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
                            <i class="fa fa-undo"></i> Reset
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>

<g:render template="/_common/modals/downloadAsPngDialog" model="['chartContainerID': 'svg-container']"/>

<content tag="include.bottom">
    <asset:javascript src="/pageAggregation/pageAggregation.js"/>
    <asset:javascript src="chartSwitch"/>
    <asset:script type="text/javascript">

        OpenSpeedMonitor.ChartModules.UrlHandling.PageAggregation().init();

        $(window).load(function() {
            OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/resultSelection.js"/>')
            OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="pageAggregation/pageAggregationGuiHandling.js"/>')
            OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="pageAggregation/pageAggregationChart.js" />',true,'pageAggregationChart');
        });

        var pageAggregationChart = null;
        $(window).on('pageAggregationChartLoaded', function () {
            pageAggregationChart = OpenSpeedMonitor.ChartModules.PageAggregation("#page-aggregation-svg");
            $(window).on('resize', function() {
                pageAggregationChart.setData({autoWidth: true});
                pageAggregationChart.render();
            });
            $("#inFrontButton").click(function() {
                pageAggregationChart.setData({stackBars: true});
                pageAggregationChart.render();
            });
            $("#besideButton").click(function() {
                pageAggregationChart.setData({stackBars: false});
                pageAggregationChart.render();
            });
            $(".chart-filter").click(onFilterClick);
        });

        function onFilterClick() {
            pageAggregationChart.setData({activeFilter: $(this).data("filter")});
            pageAggregationChart.render();
            $(".chart-filter i").toggleClass('filterInactive', true).toggleClass('filterActive', false);
            $("i", $(this)).toggleClass('filterActive', true);
        }

        function addFiltersToGui(filterRules) {
            var $filterDropdownGroup = $("#filter-dropdown-group");
            var $customerJourneyHeader = $filterDropdownGroup.find("#customer-journey-header");
            $filterDropdownGroup.find('.filterRule').remove();
            if ($filterDropdownGroup.hasClass("hidden"))
                $filterDropdownGroup.removeClass("hidden");

            Object.keys(filterRules).forEach(function(filterRuleKey) {
                var link = $("<li class='filterRule'><a href='#'><i class='fa fa-check filterInactive' aria-hidden='true'></i>" + filterRuleKey + "</a></li>");
                link.data('filter', filterRuleKey);
                link.click(onFilterClick);
                link.insertAfter($customerJourneyHeader);
            });
        }

        // declare the spinner outside of the drawGraph function to prevent creation of multiple spinnerContainer
        var spinner = OpenSpeedMonitor.Spinner("#chart-container");

        function drawGraph() {

            var selectedTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getTimeFrame();
            var comparativeTimeFrame = OpenSpeedMonitor.selectIntervalTimeframeCard.getComparativeTimeFrame();
            var selectedSeries = OpenSpeedMonitor.BarchartMeasurings.getValues();

            var data = {
                    from: selectedTimeFrame[0].toISOString(),
                    to: selectedTimeFrame[1].toISOString(),
                    selectedJobGroups: JSON.stringify($.map($("#folderSelectHtmlId option:selected"), function (e) {
                        return $(e).text()
                    })),
                    selectedPages: JSON.stringify($.map($("#pageSelectHtmlId option:selected"), function (e) {
                        return $(e).text()
                    })),
                    selectedSeries: JSON.stringify(selectedSeries)
                };

            if (comparativeTimeFrame) {
                data.fromComparative = comparativeTimeFrame[0].toISOString();
                data.toComparative = comparativeTimeFrame[1].toISOString();
            }

            spinner.start();
            $.ajax({
                type: 'POST',
                data: data,
                url: "${createLink(controller: 'pageAggregation', action: 'getBarchartData')}",
                dataType: "json",
                success: function (data) {
                    spinner.stop();
                    $("#chart-card").removeClass("hidden");
                    if (!$("#error-div").hasClass("hidden"))
                        $("#error-div").addClass("hidden");

                    if (!$.isEmptyObject(data)) {
                        $('#warning-no-data').hide();
                        data.width = -1;
                        if (data.filterRules) {
                            addFiltersToGui(data.filterRules);
                        }
                        pageAggregationChart.setData(data);
                        pageAggregationChart.render();
                        OpenSpeedMonitor.ChartModules.UrlHandling.ChartSwitch.updateUrls(true);
                        $("#dia-save-chart-as-png").removeClass("disabled");
                    } else {
                        $('#warning-no-data').show();
                    }
                },
                error: function (e) {
                    spinner.stop();
                    if (e.responseText == "no data") {
                        $("#error-div").addClass("hidden");
                        $("#chart-card").removeClass("hidden");
                        $('#warning-no-data').show();
                    }
                    else {
                        $("#error-div").removeClass("hidden");
                        $("#chart-card").removeClass("hidden");
                        $("#error-message").html(e.responseText);
                    }
                }
            });
        }
        OpenSpeedMonitor.ChartModules.UrlHandling.ChartSwitch.updateUrls(true);

    </asset:script>
</content>

</body>
</html>
