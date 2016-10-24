<%@page defaultCodec="none" %>
<%-- 
This template contains controls to set a time frame in terms of a start and an end date.
These two can either be set manually or via pre-selections like 'last hour', 'last three days' or 'last 4 weeks'.
--%>
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
		<div class="col-md-4">
			<input class="form-control" type="text" id="fromDatepicker" placeholder="start ..." value="${from}" disabled/>
		</div>
		<div class="col-md-4">
			<fieldset id="fldset-startdate-hour">
				<div class="input-group bootstrap-timepicker">
					<span class="input-group-addon">
						<g:checkBox name="setFromHour" id="setFromHour" checked="${setFromHour}"/>
					</span>
					<input id="fromHourTimepicker" type="text" class="form-control"
						   value="${(fromHour=='00:00'||fromHour=='0:00')?'00:001':fromHour}" disabled>
					<span class="input-group-addon"><i class="fa fa-clock-o"></i></span>
				</div>
			</fieldset>
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
		<div class="col-md-4">
			<input class="form-control" type="text" id="toDatepicker" placeholder="end ..." value="${to}" disabled/>
		</div>
		<div class="col-md-4">
			<fieldset id="fldset-enddate-hour">
				<div class="input-group bootstrap-timepicker">
					<span class="input-group-addon">
						<g:checkBox name="setToHour" id="setToHour" checked="${setToHour}"/>
					</span>
					<input id="toHourTimepicker" type="text" class="form-control"
						   value="${(toHour=='00:00'||toHour=='0:00')?'00:001':toHour}" disabled>
					<span class="input-group-addon"><i class="fa fa-clock-o"></i></span>
				</div>
			</fieldset>
		</div>
	</div>
	<input type="hidden" name="to" value="${to}" id="to">
	<input type="hidden" name="toHour" value="${toHour}" id="toHour">
</fieldset>