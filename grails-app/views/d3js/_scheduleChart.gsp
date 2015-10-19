<%@ page import="grails.converters.JSON" %>
<asset:stylesheet src="d3Charts/scheduleChart.css"/>
<asset:javascript src="d3/scheduleChart.js"/>
%{--Data to inject:
                    data : de.iteratec.osm.d3data.TreemapData as JSON
                    design : one of 'browser', 'rect'
                    id : the id of the barChart on this page (unique)--}%

<div class="row">
    <div class="span12" id="${"ScheduleChart" + id}">
    </div>
</div>
<asset:script type="text/javascript">
    $(document).ready( createScheduleChart(${chartData}, "${"ScheduleChart" + id}") );
</asset:script>
