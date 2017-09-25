<%@ page import="de.iteratec.osm.result.Threshold" %>
<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<%@ page import="de.iteratec.osm.result.Measurand" %>
<div class="form-group" id="">
    <label>
        <g:message code="job.Thresholds.label" default="Thresholds"/>
    </label>
</div>
<div id="threshold">
<div>
    <div id="thresholdList">
        <div>
            <ul>
                <li v-for="threshold in thresholds">
                    <div>Die {{ threshold.measurand.name }} Messung von {{ threshold.measuredEvent.name }}
                    sollte schneller als {{ threshold.lowerBoundary }} ms, aber nicht langsamer als  {{ threshold.upperBoundary }} ms sein. <a>edit</a>  <a>delete</a></div>
                </li>
            </ul>
        </div>
    </div>
</div>

    <div id="thresholdCreate">Die
    <g:select id="measurand" name="measurand"
              from="${Measurand.values()}"/>
    Messung von
    <g:select id="measuredEvent" name="measuredEvent"
              from="[]"
              class="measured-event-select"/>
    sollte schneller als
    <g:field id="lowerBoundary" class="" type="number" min="1" name="lowerBoundary" cols="40" rows="5" maxlength="255"/>
    ms, aber nicht langsamer als
    <g:field id="upperBoundary" class="" type="number" min="1" name="upperBoundary" cols="40" rows="5" maxlength="255"/>
    ms sein. <a>add</a>  <a>clear</a></div>
<div>
    <button class="btn btn-default" type="button" id="copyToClipboard">
        <g:message code="job.threshold.copyToClipboard" default="Copy To Clipboard"/>
    </button>

    <button id="threshold_button_create" class="btn btn-default" type="button"
    @click="addThreshold('${job}', '${g.createLink([controller: 'threshold', action: 'createAsync'])}')">
        <g:message code="job.threshold.create.new" default="Add Threshold"/>
    </button>
</div>
</div>

<asset:script type="text/javascript">
    OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="job/thresholdForJob.js"/>');
    $(window).load(function() {
      OpenSpeedMonitor.thresholdforJobs.init({scriptId: "${job?.script?.id}" , targetUrl:"${createLink(controller: 'script', action: 'getMeasuredEventsForScript')}"});
      OpenSpeedMonitor.thresholdforJobs.initVue({jobId: "${job?.id}"});
    });
</asset:script>