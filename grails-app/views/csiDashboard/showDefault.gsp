<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta name="layout" content="kickstart_osm" />
<title>CSI CheckDashboard</title>

<r:require modules="csi-dashboard" />

<style>
	/* css for timepicker */
	.ui-timepicker-div .ui-widget-header { margin-bottom: 8px; }
	.ui-timepicker-div dl { text-align: left; }
	.ui-timepicker-div dl dt { height: 25px; margin-bottom: -25px; }
	.ui-timepicker-div dl dd { margin: 0 10px 10px 65px; }
	.ui-timepicker-div td { font-size: 90%; }
	.ui-tpicker-grid-label { background: none; border: none; margin: 0; padding: 0; }
</style>
</head>
<body>
  <%-- main menu --%>
  <g:render template="/layouts/mainMenu"/>
	<%-- heading --%>
	<div class="row">
		<div class="span12">
			<div>
				<h3><g:message code="de.iteratec.isocsi.csi.heading" default="Kundenzufriedenheit (CSI)"/></h3>
				
				<p>
				    <g:message code="de.iteratec.isocsi.csi.static.measuring.description.short" default="Den dargestellten CSI-Wochenwerten liegen jeweils die Webpagetest-Rohdaten der vorangegangenen Woche zugrunde."/>
				</p>
				
				<form method="get" action ="">
					<div style="clear:both;"></div>
					<p>
						<g:hiddenField name="aggrGroup" value="shop"/>
						<g:hiddenField name="from" value="${fromFormatted}"/>
						<g:hiddenField name="fromHour" value="${fromHour ?: 0}"/>
						<g:hiddenField name="fromMinute" value="${fromMinute ?: 0}"/>
						<g:hiddenField name="to" value="${toFormatted}"/>
						<g:hiddenField name="toHour" value="${toHour ?: 23}"/>
						<g:hiddenField name="toMinute" value="${toMinute ?: 59}"/>
						<g:select
								style="display:none;" 
								id="folderSelectHtmlId" 
								name="selectedFolder" 
								from="${folders}" 
								optionKey="id"
								optionValue="name" 
								value="${folders.collect({it.id})}"
								multiple="true" />
						<g:link action="showDefault" params="[includeInterval: true]" class="btn btn-primary" style="margin-top: 16px;">
							<g:message code="csiDashboard.include-actual-interval.label" default="inclusive actual week"/>
						</g:link>
						<g:actionSubmit value="CSV" action="csiValuesCsv" class="btn btn-primary" style="margin-top: 16px;" />
					</p>
				</form>
			</div>
		</div>
	</div>
	
	<div class="row">
		<div class="span1">
			<div id="chart-table-toggle" class="btn-group" data-toggle="buttons-radio" id="job-filter-toggle">
	 			<button type="button" class="btn btn-small active" id="chart-toggle"><g:message code="de.iteratec.isocsi.csi.button.graphView" default="Kurvendarstellung" /></button>
		 		<button type="button" class="btn btn-small" id="table-toggle"><g:message code="de.iteratec.isocsi.csi.button.tableView" default="Tabellendarstellung" /></button>
			</div>
		</div>
	</div>
	
	<br>
	
	<div class ="row">
		<%-- chart --%>
		<div id="chartbox">
		<div class="span12 well">
			<g:render template="/highchart/chart" 
				model="[
					singleYAxis: 'true',
					chartData: wptCustomerSatisfactionValues,
					chartTitle: defaultChartTitle,
					yAxisLabel: 'Kundenzufriedenheit [%]',
					initialChartWidth: '100%',
					initialChartHeight: '600',
					chartUnit: '%',
					globalLineWidth: '4',
					xAxisMin: fromTimestampForHighChart,
					xAxisMax: toTimestampForHighChart,
					markerEnabled: markerShouldBeEnabled,
					dataLabelsActivated: labelShouldBeEnabled,
					yAxisScalable: 'false',
					optimizeForExport: 'true',
					openDataPointLinksInNewWindow: 'false',
          annotations: annotations]"/>
		</div>
		</div>
		<%-- table --%>
        <div id="csi-table" class="span12" style="display: none;">
        	<g:if test="${flash.tableDataError}">
				<div class="alert alert-error">
					<g:message error="${flash.tableDataError}" />
				</div>
			</g:if>
           <g:if test="${wptCustomerSatisfactionValuesForTable!=null}">
                <table class="table table-bordered">
                    <tbody>
                        <tr>
                            <td class="text-info">
                                ${wptCustomerSatisfactionValuesForTable.replace(';', '</td><td class="text-info">').replace('\n', '</td></tr><tr><td class="text-info">')}
                            </td>
                        </tr>                       
                    </tbody>
                </table>
            </g:if>
             
        </div>
	</div>
	<r:script>
		$(document).ready(function() {
			if(typeof chart != 'undefined'){
				jQuery.each(
					chart.series, 
					function (i, series) { 
						//series.setVisible(series.name=="live");
						series.setVisible($.inArray(series.name, ${namesOfCsiGroupsAndStaticGraphsToShow.collect{'"'+it+'"'}}) != -1);
					});
			}
			// Toggle Buttons
			$("#chart-toggle").click(function() {
				$("#csi-table").hide();
				$("#chartbox").fadeIn();
			});
			$("#table-toggle").click(function() {
				$("#chartbox").hide();
				$("#csi-table").fadeIn();
			});
		});
	</r:script>
</body>
</html>
