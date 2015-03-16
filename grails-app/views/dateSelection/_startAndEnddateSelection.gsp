<%@page defaultCodec="none" %>
<%-- 
This template contains controls to set a time frame in terms of a start and an end date.
These two can either be set manually or via pre-selections like 'last hour', 'last three days' or 'last 4 weeks'.
--%>
<legend><g:message code="de.iteratec.isocsi.csi.timeframe.heading" default="Zeitraum" /></legend>
<div class="row">	<%--------------------------------------------------------------------- pre-selections --%>
	<div class="span3">
		<g:select id="timeframeSelect" class="input-large"
				name="selectedTimeFrameInterval" from="${[0, 3600, 43200, 86400, 259200, 604800, 1209600, 2419200]}"
				valueMessagePrefix="de.iteratec.isr.wptrd.timeframes"
				value="${selectedTimeFrameInterval}" />
	</div>
</div>
<fieldset id="fldset-startdate">	<%--------------------------------------------------------------------- manual start date --%>
	<div class="row">
		<div class="span1 text-right">
			<g:message
					code="de.iteratec.isr.wptrd.labels.timeframes.start"
					default="Start" />:
		</div>
		<div class="span2">
			<input type="text" class="input-medium-height30" type="text" id="fromDatepicker" placeholder="start ..." value="${from}" disabled/>
		</div>
	</div>
	<div class="row">
		<div class="span1">&nbsp;</div>
			<div class="span2">
				<label class="checkbox inline">
					<g:checkBox name="setFromHour" id="setFromHour" checked="${setFromHour}"/>
					&nbsp;<g:message code="de.iteratec.osm.report.ui.labels.set-time-manually" default="Uhrzeit&nbsp;manuell&nbsp;setzen" />
				</label>
			</div>
			<fieldset id="fldset-startdate-hour">
				<div class="span2">
					<div class="input-append bootstrap-timepicker">
						<input id="fromHourTimepicker" type="text" class="input-small content-box" value="${(fromHour=='00:00'||fromHour=='0:00')?'00:001':fromHour}" disabled>
						<span class="add-on"><i class="icon-time"></i></span>
					</div>
				</div>
			</fieldset>
	</div>
</fieldset>

<input type="hidden" name="from" id="from" value="${from}">
<input type="hidden" name="fromHour" id="fromHour" value="${fromHour}">
<fieldset id="fldset-enddate">	<%--------------------------------------------------------------------- manual end date --%>
	<div class="row">
		<div class="span1 text-right">
			<g:message
					code="de.iteratec.isr.wptrd.labels.timeframes.end"
					default="End" />:
		</div>
		<div class="span2">
			<input type="text" class=".date input-medium-height30" type="text" id="toDatepicker" placeholder="end..." value="${to}" disabled/>
		</div>
	</div>
	<div class="row">
		<div class="span1">&nbsp;</div>
			<div class="span2">
				<label class="checkbox inline">
					<g:checkBox name="setToHour" id="setToHour" checked="${setFromHour}"/>
					&nbsp;<g:message code="de.iteratec.osm.report.ui.labels.set-time-manually" default="Uhrzeit&nbsp;manuell&nbsp;setzen" />
				</label>
			</div>
			<fieldset id="fldset-enddate-hour">
				<div class="span2">
					<div class="input-append bootstrap-timepicker">
						<input id="toHourTimepicker" type="text" class="input-small content-box" value="${(toHour=='00:00'||toHour=='0:00')?'00:001':toHour}" disabled>
						<span class="add-on"><i class="icon-time"></i></span>
					</div>
				</div>
			</fieldset>
	</div>		
</fieldset>
<input type="hidden" name="to" value="${to}" id="to">
<input type="hidden" name="toHour" value="${toHour}" id="toHour">