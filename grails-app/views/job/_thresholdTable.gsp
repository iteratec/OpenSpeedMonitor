<%@ page import="de.iteratec.osm.result.Threshold" %>

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
</div>

<asset:script type="text/javascript">
    OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="job/thresholdForJob.js"/>');
    $(window).load(function() {
      OpenSpeedMonitor.thresholdMeasuredEventList.init({scriptId: "${job?.script?.id}" , targetUrl:"${createLink(controller: 'script', action: 'getMeasuredEventsForScript')}"});
      OpenSpeedMonitor.thresholdMeasuredEventList.initVue({jobId: "${job?.id}"});
    });
</asset:script>
