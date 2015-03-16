<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
	<meta name="layout" content="kickstart_osm" />
	<title><g:message code="de.iteratec.isocsi.eventResult"/></title>
	<r:require modules="eventresult" />
	<style type="text/css">
	/* css for timepicker */
	.ui-timepicker-div .ui-widget-header {
		margin-bottom: 8px;
	}
	
	.ui-timepicker-div dl {
		text-align: left;
	}
	
	.ui-timepicker-div dl dt {
		height: 25px;
		margin-bottom: -25px;
	}
	
	.ui-timepicker-div dl dd {
		margin: 0 10px 10px 65px;
	}
	
	.ui-timepicker-div td {
		font-size: 90%;
	}
	
	.ui-tpicker-grid-label {
		background: none;
		border: none;
		margin: 0;
		padding: 0;
	}
	
	/* css for select boxes */
	.element-select {
		height: 150px;
		width: 100%;
	}
	</style>
</head>
<body>

    <%-- main menu --%>
    <g:render template="/layouts/mainMenu"/>

	<g:if test="${command}">
		<g:hasErrors bean="${command}">
			<div class="alert alert-error">
				<strong><g:message
						code="de.iteratec.isocsi.TabularResultPresentationController.selectionErrors.title"
						default="You missed something on selection" /></strong>
				<ul>
					<g:eachError var="eachError" bean="${command}">
						<li><g:message error="${eachError}" /></li>
					</g:eachError>
				</ul>
			</div>
		</g:hasErrors>
	</g:if>

	<form method="get" action="">
		<div class="row">
			<div class="span5">
				<g:render template="/dateSelection/startAndEnddateSelection"
					model="${['selectedTimeFrameInterval':selectedTimeFrameInterval, 'from':from, 'fromHour':fromHour, 'to':to, 'toHour':toHour]}"/>
			</div>
			<div class="span5"></div>
		</div>
		<div class="row">
			<div class="span12">
				<legend>
					<g:message code="de.iteratec.isocsi.csi.filter.heading"
						default="Filter" />
				</legend>
			</div>
		</div>
		<g:if test="${showSpecificJob}">
			<g:render template="filterJob" model="${['job': job]}" />
			<input type="hidden" name="job.id" value="${job?.id}" />
		</g:if>
		<g:else>
			<g:render 
			      template="../eventResultDashboard/selectMeasurings" 
			      model="${['locationsOfBrowsers':locationsOfBrowsers, 'eventsOfPages':eventsOfPages,'folders':csiGroups,'selectedFolder':selectedFolder, 'pages':pages,'selectedPage':selectedPage,'measuredEvents':measuredEvents,'selectedAllMeasuredEvents':selectedAllMeasuredEvents,'selectedMeasuredEvents':selectedMeasuredEvents,'browsers':browsers,'selectedBrowsers':selectedBrowsers,'selectedAllBrowsers':selectedAllBrowsers,'locations':locations,'selectedLocations':selectedLocations,'selectedAllLocations':selectedAllLocations]}"/>
		</g:else>
		<p>
			<g:actionSubmit
				value="${g.message(code: 'de.iteratec.ism.ui.labels.show.graph', 'default':'Show')}"
				action="${ showSpecificJob ? 'listResultsForJob' : 'listResults'}" class="btn btn-primary"
				style="margin-top: 16px;" />
		</p>
	</form>
	
	<g:if test="showEventResultsListing">
		
		<g:render template="listResults" 
              model="${[model: eventResultsListing]}" />
              
   	  	<g:render template="pagination"
   	 		model="${[model: paginationListing] }" />
		
	</g:if>
	<r:script>
		$(document).ready(doOnDomReady(
			'${dateFormat}', 
			${weekStart}, 
			'${g.message(code: 'web.gui.jquery.chosen.multiselect.noresultstext', 'default':'Keine Eintr&auml;ge gefunden f&uuml;r ')}'));
	</r:script>
</body>
</html>