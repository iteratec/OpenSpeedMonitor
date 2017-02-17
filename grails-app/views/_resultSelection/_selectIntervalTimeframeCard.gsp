<%@page defaultCodec="none" %>
<%-- 
A card with controls to select a time frame (manual, or presets like 'last 12 hours') and optionally
a control to select an aggregation interval (if csiAggregationIntervals is set)
--%>
<div class="card form-horizontal" id="select-interval-timeframe-card">
    <h2>
		<g:if test="${csiAggregationIntervals}">
			<g:message code="de.iteratec.isocsi.csi.aggreator.heading"
					   default="Aggregation"/>
			&amp;
		</g:if>
		<g:message code="de.iteratec.isocsi.csi.timeframe.heading" default="Zeitraum" />
	</h2>

	<g:if test="${csiAggregationIntervals}">
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
	</g:if>
	<%--------------------------------------------------------------------- pre-selections --%>
	<div class="form-group">
		<label class="control-label col-md-4" for="timeframeSelect">
			<g:message code="de.iteratec.isocsi.csi.timeframe.heading" default="Zeitraum" />:
		</label>
		<div class="col-md-8">
			<g:select id="timeframeSelect" class="form-control"
					name="selectedTimeFrameInterval" from="${[0, 3600, 43200, 86400, 259200, 604800, 1209600, 2419200]}"
					valueMessagePrefix="de.iteratec.isr.wptrd.timeframes"
					value="${selectedTimeFrameInterval}" />
		</div>
	</div>

	<%--------------------------------------------------------------------- manual start date --%>
	<fieldset id="manual-timeframe-selection">
		<div class="form-group">
			<label class="col-md-4 control-label" for="fromDatepicker">
				<g:message
						code="de.iteratec.isr.wptrd.labels.timeframes.start"
						default="Start" />:
			</label>
			<div class="col-md-8">
				<g:render template="/_resultSelection/dateTimePicker" model="[
				        id: 'startDateTimePicker',
						manualTimeName: 'setFromHour',
						manualTimeValue: setFromHour,
						timeName: 'fromHour',
						time: fromHour,
						dateControlId: 'fromDatepicker',
						dateName: 'from',
						date: from,
						dateFormat: dateFormat,
						weekStart: weekStart
				]" />
			</div>
		</div>
	<%--------------------------------------------------------------------- manual end date --%>
		<div class="form-group">
			<label class="col-md-4 control-label" for="toDatepicker">
				<g:message
						code="de.iteratec.isr.wptrd.labels.timeframes.end"
						default="End" />:
			</label>
			<div class="col-md-8">
				<g:render template="/_resultSelection/dateTimePicker" model="[
						id: 'endDateTimePicker',
						manualTimeName: 'setToHour',
						manualTimeValue: setToHour,
						timeName: 'toHour',
						time: toHour,
						dateControlId: 'toDatepicker',
						dateName: 'to',
						date: to,
						dateFormat: dateFormat,
						weekStart: weekStart
				]" />
			</div>
		</div>
	</fieldset>
	<g:if test="${showIncludeInterval}">
		<div class="row">
			<div class="col-md-offset-4 col-md-8">
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
