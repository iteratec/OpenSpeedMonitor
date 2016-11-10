<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart_osm" />
	
	<title>openSpeed &mdash; Events from which the value was calculated</title>
	
</head>
<body>
    <%-- main menu --%>
    <g:render template="/layouts/mainMenu"/>

    <h3><g:message code="de.iteratec.result.value.calculated.from.infomessage" /></h3>

    <p>
        <g:message code="de.iteratec.osm.interval.start.label" default="Start"/>: ${measuringOfValueStartedAt}
        <g:if test="${!aggregatedCsiAggregationDocComplete.equals('unassigned')}">
            <br />
            <g:message code="de.iteratec.osm.csByWptDocCompleteInPercent.label" default="Customer Satisfaction (by doc complete)"/> : ${aggregatedCsiAggregationDocComplete}
        </g:if>
        <g:if test="${!aggregatedCsiAggregationVisuallyComplete.equals('unassigned')}">
            <br />
            <g:message code="de.iteratec.osm.csByWptVisuallyCompleteInPercent.label" default="Customer Satisfaction (by visually complete)"/> : ${aggregatedCsiAggregationVisuallyComplete}
        </g:if>
    </p>

    <g:if test="${countOfAggregatedResultsDiffers}">
    <p>
        The value has concurrently changed. The initial count of aggregated 
        results was ${lastKnownCountOfAggregatedResultsOrNull} but currently there
        are ${currentCountOfAggregatedResults} results aggregated in this 
        value.
    </p>
    </g:if>
    
    <g:if test="${someEventResultsMissing}">
    <p>
        Some results of measured steps on which the value based are removed 
        from database and could not be listed here. 
        Information for your system administrator:
        The following database identifier are missing:
        ${(missingEventResultsIds*.toString()).join(', ')}.
    </p>
    </g:if>
    
    <g:render template="/tabularResultPresentation/listResults"
              model="${[model: eventResultListing]}" />
    
	<g:if test="${remainingResultsCount}">
        There are ${remainingResultsCount} more results which were not listed. 
        
        <g:if test="${listAggregatedResultsByQueryParams}">
			<%-- need to send next request to listAggregatedResultsByQueryParams --%>
			<g:form method="GET">
				<g:hiddenField name="from" value="${params.from}" />
				<g:hiddenField name="to" value="${params.to}" />
				<g:hiddenField name="jobGroup" value="${params.jobGroup}" />
				<g:hiddenField name="measuredEvent" value="${params.measuredEvent}" />
				<g:hiddenField name="page" value="${params.page}" />
				<g:hiddenField name="browser" value="${params.browser}" />
				<g:hiddenField name="location" value="${params.location}" />
				<g:hiddenField name="aggregatorTypeName"
					value="${params.aggregatorTypeName}" />
				<g:hiddenField name="lastKnownCountOfAggregatedResultsOrNull"
					value="${params.lastKnownCountOfAggregatedResultsOrNull}" />
				<g:hiddenField name="showAll" value="${true}" />
				<g:if test="${showRemainingResultsCountVeryLargeWaring}">
					<g:actionSubmit action="listAggregatedResultsByQueryParams"
						id="override-long-processing-time"
						value="${g.message(code: 'ui.view.listAggregatedResults.button.showAll.withWarning.title')}"
						class="btn btn-warning" />
				</g:if>
				<g:else>
					<g:actionSubmit action="listAggregatedResultsByQueryParams"
						id="override-long-processing-time"
						value="${g.message(code: 'ui.view.listAggregatedResults.button.showAll.title')}"
						class="btn btn-warning" />
				</g:else>
			</g:form>
		</g:if>
		<g:else>
			<%-- need to send next request to listAggregatedResults --%>
			<g:form method="GET">
				<g:hiddenField name="csiAggregationId"
					value="${params.csiAggregationId}" />
				<g:hiddenField name="lastKnownCountOfAggregatedResultsOrNull"
					value="${params.lastKnownCountOfAggregatedResultsOrNull}" />
				<g:hiddenField name="showAll" value="${true}" />
				<g:if test="${showRemainingResultsCountVeryLargeWaring}">
					<g:actionSubmit action="listAggregatedResults"
						id="override-long-processing-time"
						value="${g.message(code: 'ui.view.listAggregatedResults.button.showAll.withWarning.title')}"
						class="btn btn-warning" />
				</g:if>
				<g:else>
					<g:actionSubmit action="listAggregatedResults"
						id="override-long-processing-time"
						value="${g.message(code: 'ui.view.listAggregatedResults.button.showAll.title')}"
						class="btn btn-warning" />
				</g:else>
			</g:form>
		</g:else>
	</g:if>
</body>
</html>