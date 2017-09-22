<%@ page import="de.iteratec.osm.result.Threshold" %>
<%@ page import="de.iteratec.osm.result.Measurand" %>
<%@ page import="de.iteratec.osm.result.MeasuredEvent" %>
<div id="thresholdList">
    <div>
    <ul>
        <li v-for="threshold in thresholds">
        <div>Die {{ threshold.measurand.name }} Messung von {{ threshold.measuredEvent.name }}
        sollte schneller als {{ threshold.lowerBoundary }} ms, aber nicht langsamer als  {{ threshold.upperBoundary }} ms sein. <a>edit</a>  <a>delete</a></div>
        </li>
    </ul>
    </div>
    <div>Die
    <g:select id="measurand" name="measurand"
              from="${Measurand.values()}"/>
    Messung von
    <g:select id="measuredEvent" name="measuredEvent"
              from="[]"
              class="measured-event-select"/>
    sollte schneller als
    <g:field id="lowerBoundary" class="" type="number" min="1" name="lowerBoundary" cols="40" rows="5" maxlength="255"/>
    ms, aber nicht langsamer als
    <g:field id="lowerBoundary" class="" type="number" min="1" name="lowerBoundary" cols="40" rows="5" maxlength="255"/>
    ms sein. <a>add</a>  <a>clear</a></div>
</div>

<asset:script type="text/javascript">
    OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="job/thresholdMeasuredEventList.js"/>');
    $(window).load(function() {
      OpenSpeedMonitor.thresholdMeasuredEventList.init({scriptId: "${job?.script?.id}" , targetUrl:"${createLink(controller: 'script', action: 'getMeasuredEventsForScript')}"});
      OpenSpeedMonitor.thresholdMeasuredEventList.initVue({jobId: "${job?.id}"});
    });
</asset:script>
