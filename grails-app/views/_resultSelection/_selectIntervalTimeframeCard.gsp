<%@page defaultCodec="none" %>
<%-- 
A card with controls to select a time frame (manual, or presets like 'last 12 hours') and optionally
a control to select an aggregation interval (if aggregationIntervals is set)
--%>
<div class="card form-horizontal" id="select-interval-timeframe-card" data-comparative-enabled="${showComparisonInterval ? 'true' : 'false'}">
    <h2>
		<g:if test="${aggregationIntervals}">
			<g:message code="de.iteratec.isocsi.csi.aggreator.heading"
					   default="Aggregation"/>
			&amp;
		</g:if>
		<g:message code="de.iteratec.isocsi.csi.timeframe.heading" default="Time frame" />
	</h2>

    <%--------------------------------------------------------------------- aggregations --%>
	<g:if test="${aggregationIntervals}">
		<div class="row form-group">
			<label class="col-md-4 control-label" for="selectedIntervalHtmlId">
				<g:message
						code="de.iteratec.isr.wptrd.labels.timeframes.interval"
						default="Interval"/>:
			</label>

			<div class="col-md-8">
				<g:select id="selectedIntervalHtmlId" class="form-control"
						  name="selectedInterval" from="${aggregationIntervals}"
						  valueMessagePrefix="de.iteratec.isr.wptrd.intervals"
						  value="${selectedInterval}"/>
			</div>
		</div>
	</g:if>
	<%--------------------------------------------------------------------- time frame --%>
	<div class="row form-group">
		<label class="control-label col-md-4" for="timeframeSelect">
			<g:message code="de.iteratec.isocsi.csi.timeframe.heading" default="Time frame" />:
		</label>
		<div class="col-md-8">
			<g:select id="timeframeSelect" class="form-control"
					name="selectedTimeFrameInterval" from="${[0, 3600, 43200, 86400, 259200, 604800, 1209600, 2419200]}"
					valueMessagePrefix="de.iteratec.isr.wptrd.timeframes"
					value="${selectedTimeFrameInterval}" />
		</div>
	</div>
	<div class="row form-group">
        <g:if test="${showComparisonInterval}">
            <div class="hidden col-md-4 control-label comparison-initially-hidden">
                <label for="timeframe-picker" class="text-muted sub">
                    <g:message code="de.iteratec.osm.main-timeframe.label" default="Main" />
                </label>
            </div>
        </g:if>
		<div class="col-md-8 col-md-offset-4" id="timeframe-picker">
			<g:render template="/_resultSelection/timeRangePicker" model="${[
					'nameFrom': 'from',
					'nameTo': 'to',
					'valueFrom': from,
					'valueTo': to,
					'dateFormat': dateFormat
			]}" />
		</div>
    </div>
    <%--------------------------------------------------------------------- comparative time frame --%>
    <g:if test="${showComparisonInterval}">
        <%------------ initially visible --%>
        <div class="row form-group" id="comparativeTimeFrameButton">
            <div class="col-md-8 col-md-offset-4">
                <button type="button" id="addComparativeTimeFrame" class="btn btn-default btn-block">
                    <i class="fa fa-plus"></i>
                    <g:message code="de.iteratec.osm.comparative-timeframe.heading" default="Comparative time frame"/>
                </button>
            </div>
        </div>
        <%------------ initially NOT visible --%>
        <div class="hidden comparison-initially-hidden" id="timeframe-picker-previous-container">
            <div class="row form-group">
                <div class="control-label col-md-4">
                    <label for="timeframe-picker-previous" class="text-muted sub">
                        <g:message code="de.iteratec.osm.comparative-timeframe.label" default="Comparison" />
                    </label>
                    <a href="#/" id="removeComparativeTimeFrame"><i class="fa fa-times" aria-hidden="true"></i></a>
                </div>
                <div class="col-md-8" id="timeframe-picker-previous">
                    <g:render template="/_resultSelection/timeRangePicker" model="${[
                            'nameFrom': 'fromComparative',
                            'nameTo': 'toComparative',
                            'valueFrom': from,
                            'valueTo': to,
                            'dateFormat': dateFormat
                    ]}" />
                </div>
            </div>
        </div>
    </g:if>

    <%--------------------------------------------------------------------- include actual interval checkbox (CSI)--%>
	<g:if test="${showIncludeInterval}">
		<div class="row">
			<div class="col-md-8 col-md-offset-4">
				<fieldset id="includeInterval">
					<label class="checkbox-inline">
						<g:checkBox name="includeInterval" id="includeInterval" checked="${includeInterval}"/>
						&nbsp;<g:message code="de.iteratec.isocsi.csi.includeInterval.label"
										 default="auch&nbsp;aktuelles&nbsp;Intervall&nbsp;anzeigen"/>
					</label>
				</fieldset>
			</div>
		</div>
	</g:if>
</div>

<asset:script type="text/javascript">
	$(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/_resultSelection/selectIntervalTimeframeCard.js" />', true, 'selectIntervalTimeframeCard')
    });
</asset:script>
