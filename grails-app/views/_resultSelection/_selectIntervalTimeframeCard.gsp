<%@page defaultCodec="none" %>
<%-- 
A card with controls to select a time frame (manual, or presets like 'last 12 hours') and optionally
a control to select an aggregation interval (if csiAggregationIntervals is set)
--%>
<div class="card form-horizontal" id="select-timeframe">
    <legend>
		<g:if test="${csiAggregationIntervals}">
			<g:message code="de.iteratec.isocsi.csi.aggreator.heading"
					   default="Aggregation"/>
			&amp;
		</g:if>
		<g:message code="de.iteratec.isocsi.csi.timeframe.heading" default="Zeitraum" />
	</legend>

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
			<div class="col-md-8" id="startDateTimePicker">
				<div class="input-group bootstrap-timepicker time-control">
					<span class="input-group-addon">
						<g:checkBox name="setFromHour" id="setFromHour" checked="${setFromHour}"/>
					</span>
					<input id="fromHourTimepicker" type="text" class="form-control"
						   value="${(fromHour=='00:00'||fromHour=='0:00')?'00:001':fromHour}" disabled/>
					<span class="input-group-addon"><i class="fa fa-clock-o"></i></span>
				</div>
				<div class="date-control">
					<input class="form-control" type="text" id="fromDatepicker"
						data-date-format="${dateFormat}" data-date-week-start="${weekStart}"
					    placeholder="start date" value="${from}" />
				</div>
				<input type="hidden" name="from" id="from" class="date-hidden" value="${from}" />
				<input type="hidden" name="fromHour" id="fromHour" class="time-hidden" value="${fromHour}" />
			</div>
		</div>
	<%--------------------------------------------------------------------- manual end date --%>
		<div class="form-group">
			<label class="col-md-4 control-label" for="toDatepicker">
				<g:message
						code="de.iteratec.isr.wptrd.labels.timeframes.end"
						default="End" />:
			</label>
			<div class="col-md-8" id="endDateTimePicker">
				<div class="input-group bootstrap-timepicker time-control">
					<span class="input-group-addon">
						<g:checkBox name="setToHour" id="setToHour" checked="${setToHour}"/>
					</span>
					<input id="toHourTimepicker" type="text" class="form-control"
						   value="${(toHour=='00:00'||toHour=='0:00')?'00:001':toHour}" disabled/>
					<span class="input-group-addon"><i class="fa fa-clock-o"></i></span>
				</div>
				<div class="date-control">
					<input class="form-control" type="text" id="toDatepicker" placeholder="end date" value="${to}"
						   data-date-format="${dateFormat}" data-date-week-start="${weekStart}" />
				</div>
				<input type="hidden" name="to" value="${to}" id="to" class="date-hidden" />
				<input type="hidden" name="toHour" value="${toHour}" id="toHour" class="time-hidden" />
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
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/_resultSelection/_selectIntervalTimeframeCard.js" absolute="true"/>')
    });
</asset:script>