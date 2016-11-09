<%@page defaultCodec="none" %>
<%-- 
This template contains controls to set a time frame in terms of a start and an end date.
These two can either be set manually or via pre-selections like 'last hour', 'last three days' or 'last 4 weeks'.
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
	<fieldset id="fldset-startdate">
		<div class="form-group">
			<label class="col-md-4 control-label" for="fromDatepicker">
				<g:message
						code="de.iteratec.isr.wptrd.labels.timeframes.start"
						default="Start" />:
			</label>
			<div class="col-md-8">
				<fieldset id="fldset-startdate-hour" class="time-control">
					<div class="input-group bootstrap-timepicker">
						<span class="input-group-addon">
							<g:checkBox name="setFromHour" id="setFromHour" checked="${setFromHour}"/>
						</span>
						<input id="fromHourTimepicker" type="text" class="form-control"
							   value="${(fromHour=='00:00'||fromHour=='0:00')?'00:001':fromHour}" disabled/>
						<span class="input-group-addon"><i class="fa fa-clock-o"></i></span>
					</div>
				</fieldset>
				<div class="date-control">
					<input class="form-control" type="text" id="fromDatepicker" placeholder="start ..." value="${from}" />
				</div>
			</div>
		</div>
		<input type="hidden" name="from" id="from" value="${from}">
		<input type="hidden" name="fromHour" id="fromHour" value="${fromHour}">
	</fieldset>

	<%--------------------------------------------------------------------- manual end date --%>
	<fieldset id="fldset-enddate">
		<div class="form-group">
			<label class="col-md-4 control-label" for="toDatepicker">
				<g:message
						code="de.iteratec.isr.wptrd.labels.timeframes.end"
						default="End" />:
			</label>
			<div class="col-md-8">
				<fieldset id="fldset-enddate-hour" class="time-control">
					<div class="input-group bootstrap-timepicker">
						<span class="input-group-addon">
							<g:checkBox name="setToHour" id="setToHour" checked="${setToHour}"/>
						</span>
						<input id="toHourTimepicker" type="text" class="form-control"
							   value="${(toHour=='00:00'||toHour=='0:00')?'00:001':toHour}" disabled/>
						<span class="input-group-addon"><i class="fa fa-clock-o"></i></span>
					</div>
				</fieldset>
				<div class="date-control">
					<input class="form-control" type="text" id="toDatepicker" placeholder="end ..." value="${to}" />
				</div>
			</div>
		</div>
		<input type="hidden" name="to" value="${to}" id="to">
		<input type="hidden" name="toHour" value="${toHour}" id="toHour">
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