<div class="input-group">
    <input type="text" class="form-control timerange-userinput-from" data-date-format="${dateFormat}">
    <span class="input-group-addon"><g:message code="de.iteratec.isr.wptrd.labels.timeframes.to" default="to"/> </span>
    <input type="text" class="form-control timerange-userinput-to" data-date-format="${dateFormat}">
</div>
<input type="hidden" id="fromDatepicker" name="${nameFrom}" class="timerange-value-from" value="${valueFrom}" />
<input type="hidden" id="toDatepicker" name="${nameTo}" class="timerange-value-to" value="${valueTo}" />
