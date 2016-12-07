<div class="form-group">
    <g:select id="pageSelectHtmlId" class="form-control" name="selectedPages"
              from="${pages}" optionKey="id" optionValue="name" multiple="true"
              value="${selectedPages}"
              title="${message(code: 'de.iteratec.isr.wptrd.labels.filterPage')}"/>
</div>
<g:if test="${showMeasuredEventForm}">
    <div id="filter-measured-event" class="form-group">
        <label for="selectedMeasuredEventsHtmlId">
            <strong>
                <g:message code="de.iteratec.isr.wptrd.labels.filterMeasuredEvent"
                           default="Measured Event:"/>
            </strong>
        </label>
        <g:select id="selectedMeasuredEventsHtmlId"
                  class="form-control chosen"
                  data-placeholder="${g.message(code: 'web.gui.jquery.chosen.multiselect.placeholdermessage', 'default': 'Please chose an option')}"
                  name="selectedMeasuredEventIds"
                  from="${measuredEvents}"
                  optionKey="id"
                  optionValue="name"
                  value="${selectedMeasuredEventIds}"
                  multiple="true"/>
        <label class="checkbox-inline">
            <g:checkBox name="selectedAllMeasuredEvents"
                        checked="${selectedAllMeasuredEvents}" value="${true}"/>
            <g:message code="de.iteratec.isr.csi.eventResultDashboard.selectedAllMeasuredEvents.label"
                       default="Select all measured steps"/>
        </label>
    </div>
</g:if>
