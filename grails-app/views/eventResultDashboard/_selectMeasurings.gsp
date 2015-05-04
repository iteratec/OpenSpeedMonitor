<%@page defaultCodec="none" %>
<%-- 
GSP-Template Mappings:

* folders List<JobGroup>: all Folders for selection 
* selectedFolder List<JobGroup>: all selected folders from past call

* pages List<Page>: all Pages for selection
* selectedPages List<Page>: all selected pages from past call

* measuredEvents List<MeasuredEvent>: see other
* selectedMeasuredEventIds List<MeasuredEvent>: see other
* selectedAllMeasuredEvents :

* browsers
* selectedBrowsers
* selectedAllBrowsers

* locations
* selectedLocations
* selectedAllLocations
 --%>

<r:require modules="select-measurings, chosen" />

<div class="row">
	<div class="span4">
		<label for="folderSelectHtmlId"><g:message
				code="de.iteratec.isr.wptrd.labels.filterFolder" default="Folder:" /></label>
	
		<g:select id="folderSelectHtmlId" class="iteratec-element-select"
			name="selectedFolder" from="${folders}" optionKey="id"
			optionValue="name" value="${selectedFolder}"
			multiple="true" />
	</div>
	<div id="page-filter" class="span4 iteratec-grid-border-left">
		<label for="pageSelectHtmlId"><g:message
				code="de.iteratec.isr.wptrd.labels.filterPage" default="Pages:" /></label>
		<g:select id="pageSelectHtmlId" class="iteratec-element-select" name="selectedPages"
			from="${pages}" optionKey="id" optionValue="name" multiple="true"
			value="${selectedPages}" />
	</div>
	<div id="browser-filter" class="span3 iteratec-grid-border-left">
		<label for="selectedBrowsersHtmlId"><g:message
						code="de.iteratec.isr.wptrd.labels.filterBrowser"
						default="Browser:" /></label>
		<g:select id="selectedBrowsersHtmlId"
					class="iteratec-element-select"
					name="selectedBrowsers" from="${browsers}" optionKey="id"
					optionValue="${{it.name + ' (' + it.name + ')'}}" multiple="true"
					value="${selectedBrowsers}" />
		<br>
		<g:checkBox name="selectedAllBrowsers"
					checked="${selectedAllBrowsers}" value="${true}" />
		<label for="selectedAllBrowsers"><g:message
						code="de.iteratec.isr.csi.eventResultDashboard.selectedAllBrowsers.label"
						default="Select all Browsers" /></label>
	</div>
</div>
<div id="advanced-filter-row" class="row">
	<div class="span4">
		<%-- Hack for iteratec grid border --%>
	</div>
	<div id="measured-event-filter" class="span4 iteratec-grid-border-left">
		<label for="selectedMeasuredEventsHtmlId"><g:message
							code="de.iteratec.isr.wptrd.labels.filterMeasuredEvent"
							default="Measured Event:" /></label>
		<g:select id="selectedMeasuredEventsHtmlId"
						class="iteratec-element-select chosen-select"
						data-placeholder="${g.message(code: 'web.gui.jquery.chosen.multiselect.placeholdermessage', 'default':'Bitte ausw&auml;hlen')}"
						name="selectedMeasuredEventIds" from="${measuredEvents}"
						optionKey="id" optionValue="name"
						value="${selectedMeasuredEventIds}" multiple="true" />
		<br>
		<g:checkBox name="selectedAllMeasuredEvents"
						checked="${selectedAllMeasuredEvents}" value="${true}" />
		<label for="selectedAllMeasuredEvents"><g:message
							code="de.iteratec.isr.csi.eventResultDashboard.selectedAllMeasuredEvents.label"
							default="Select all measured steps" /></label>
	</div>
	<div id="location-filter"  class="span3 iteratec-grid-border-left">
		<label for="selectedLocationsHtmlId"><g:message
						code="de.iteratec.isr.wptrd.labels.filterLocation"
						default="Location:" /></label>
		<g:select id="selectedLocationsHtmlId"
					class="iteratec-element-select chosen-select" style="height: 150px; width:90%"
					data-placeholder="${g.message(code: 'web.gui.jquery.chosen.multiselect.placeholdermessage', 'default':'Bitte ausw&auml;hlen')}"
					name="selectedLocations" from="${locations}" optionKey="id"
					optionValue="${it}"
					multiple="true" value="${selectedLocations}" />
		<br>
		<g:checkBox name="selectedAllLocations"
					checked="${selectedAllLocations}" value="${true}" />
		<label for="selectedAllLocations"><g:message
						code="de.iteratec.isr.csi.eventResultDashboard.selectedAllLocations.label"
						default="Select all locations" /></label>
	</div>
</div>
<div class="row">
	<div class="span1">
		<div id="advanced-filter-buttons" class="btn-group" data-toggle="buttons-radio" id="job-filter-toggle">
		 	<button type="button" class="btn btn-small active" id="simple-job-filter"><g:message code="de.iteratec.osm.gui.dashboards.measurementselection.label.simple" default="simple" /></button>
		 	<button type="button" class="btn btn-small" id="advanced-job-filter"><g:message code="de.iteratec.osm.gui.dashboards.measurementselection.label.advanced" default="advanced" /></button>
		</div>
	</div>
</div>
<r:script>
 	var pagesToEvents = [];
    <g:each var="page" in="${pages}">
		<g:if test="${eventsOfPages[page.id]!=null}">
    	pagesToEvents[${page.id}]= [<g:each var="event" in="${eventsOfPages[page.id]}">${event},</g:each> ];
    	</g:if>
	</g:each>
	
	var browserToLocation = [];
	<g:each var="browser" in="${browsers}">
		<g:if test="${locationsOfBrowsers[browser.id]!=null}">
			    	browserToLocation[${browser.id}]=[ <g:each var="location" in="${locationsOfBrowsers[browser.id]}">${location},</g:each> ];
		</g:if>
	</g:each>
</r:script>