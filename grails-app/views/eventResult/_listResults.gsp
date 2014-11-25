<%-- 
    Template to list EventResult s.
    
    Required properties:
      model: An instance of EventResultListing, not null. 
             If empty an aproximate message is shown.
--%>
<g:if test="${model.isEmpty()}">
<p>
    <g:message code="de.iteratec.isr.ui.EventResultListing.isEmpty.message" />
</p>
</g:if>
<g:else>
<table class="table table-striped table-bordered table-condensed">
    <thead>
        <tr>
            <th rowspan="2">
               <g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.label" />
               <br />
               <small><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.link.description" /></small>
            </th>
            <th rowspan="2"><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.measuringDate" /></th>
            <th rowspan="2"><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.firstView" /></th>
            <th rowspan="2"><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.timeToFirstByteInMillis" /></th>
            <th rowspan="2"><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.startToRenderInMillis" /></th>
            <th rowspan="2"><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.domTimeInMillis" /></th>
            <th colspan="3" rowspan="1"><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow_docComplete" /></th>
            <th colspan="3" rowspan="1"><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow_fullyLoaded" /></th>
        </tr>
        <tr>
            <th><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.docCompleteTimeInMillis" /></th>
            <th><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.docCompleteRequests" /></th>
            <th><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.docCompleteIncomingBytes" /></th>
            <th><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.fullyLoadedTimeInMillis" /></th>
            <th><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.fullyLoadedRequestCount" /></th>
            <th><g:message code="ui.table.header.de.iteratec.isr.ui.EventResultListingRow.fullyLoadedIncomingBytes" /></th>
        </tr>
    </thead>
    <tbody>
        <g:each var="eachResult" in="${model.rows}">
        <tr>
           <td>
               <g:if test="${eachResult.testsDetailsURL}">
                   <a href="${eachResult.testsDetailsURL.toString()}" target="_blank">${eachResult.label}</a>
               </g:if>
               <g:else>
                   ${eachResult.label}
               </g:else>
           </td>
           <td>${eachResult.measuringDate}</td>
           <td><g:if test="${eachResult.firstView}">&radic;</g:if></td>
           <td>${eachResult.timeToFirstByteInMillis}</td>
           <td>${eachResult.startToRenderInMillis}</td>
           <td>${eachResult.domTimeInMillis}</td>
           <%-- doc complete --%>
           <td>${eachResult.docCompleteTimeInMillis}</td>
           <td>${eachResult.docCompleteRequests}</td>
           <td>${eachResult.docCompleteIncomingBytes}</td>
           <%-- fully loaded --%>
           <td>${eachResult.fullyLoadedTimeInMillis}</td>
           <td>${eachResult.fullyLoadedRequestCount}</td>
           <td>${eachResult.fullyLoadedIncomingBytes}</td>
        </tr>
        </g:each>
    </tbody>
</table>
<p>(All durations in milliseconds. Click on a job name to see further details.)</p>
</g:else>
