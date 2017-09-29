<%@ page import="de.iteratec.osm.result.Threshold" %>
<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<%@ page import="de.iteratec.osm.result.Measurand" %>

<div id="threshold">
    <div>
        <div id="thresholdList">
            <div>
                <div v-for="threshold in thresholds">
                    <div v-if="threshold.edit">Die
                        <select id="measurandEdit" name="measurand" class="measurand-event-select"
                                v-model="threshold.threshold.measurand">
                            <option v-for="measurand in measurands" :value="measurand">{{measurand.name}}</option>
                        </select>
                        Messung von
                        <select id="measuredEventEdit" name="measuredEvent" class="measured-event-select"
                                v-model="threshold.threshold.measuredEvent">
                            <option v-for="measuredEvent in measuredEvents"
                                    :value="measuredEvent">{{measuredEvent.name}}</option>
                        </select>
                        sollte schneller als
                        <input id="lowerBoundaryEdit" class="" type="number" min="1" name="lowerBoundary" cols="40"
                               rows="5"
                               maxlength="255"
                               v-model="threshold.threshold.lowerBoundary"/>
                        ms, aber nicht langsamer als
                        <input id="upperBoundaryEdit" class="" type="number" min="1" name="upperBoundary" cols="40"
                               rows="5"
                               maxlength="255"
                               v-model="threshold.threshold.upperBoundary"/>
                        ms sein. <button type="button"
                                         class="thresholdButton"
                                         @click="updateThreshold(threshold, '${g.createLink([controller: 'threshold', action: 'updateAsync'])}')">submit</button>
                        <button type="button"
                                class="thresholdButton"
                                @click="changeEditMode(threshold, false)">discard</button>
                    </div>

                    <div v-else class="thresholdMeasurand">{{ threshold.threshold.measurand.name }}<br>
                        <div class="thresholdBoundaries">Good < {{ threshold.threshold.lowerBoundary }} ms < OK < {{ threshold.threshold.upperBoundary }} ms < Bad
                        <button type="button"
                                class="thresholdButton"
                                @click="changeEditMode(threshold, true)">edit</button>
                        <button type="button"
                                class="thresholdButton"
                                @click="deleteThreshold(threshold, '${g.createLink([controller: 'threshold', action: 'deleteAsync'])}')">delete</button>
                    </div>
                    </div>
                </div>
            </div>
        </div>
    </div>


    <div>Die
        <select id="measurand" name="measurand" class="measurand-event-select" v-model="newThreshold.measurand">
            <option v-for="measurand in measurands" :value="measurand">{{measurand.name}}</option>
        </select>
        Messung von
        <select id="measuredEvent" name="measuredEvent" class="measured-event-select"
                v-model="newThreshold.measuredEvent">
            <option v-for="measuredEvent in measuredEvents" :value="measuredEvent">{{measuredEvent.name}}</option>
        </select>
        sollte schneller als
        <input id="lowerBoundary" class="" type="number" min="1" name="lowerBoundary" cols="30" rows="5"
               maxlength="150"
               v-model="newThreshold.lowerBoundary"/>
        ms, aber nicht langsamer als
        <input id="upperBoundary" class="" type="number" min="1" name="upperBoundary" cols="40" rows="5"
               maxlength="150"
               v-model="newThreshold.upperBoundary"/>
        ms sein.
        <button type="button" class="thresholdButton"
                @click="addThreshold('${job}', '${g.createLink([controller: 'threshold', action: 'createAsync'])}')">add</button>
        <button type="button" class="thresholdButton">clear</button>
    </div>


    <div class="ciButton">
        <button class="btn btn-default" type="button" id="copyToClipboard">
            <g:message code="job.threshold.copyToClipboard" default="Copy To Clipboard"/>
        </button>
    </div>
</div>


<asset:script type="text/javascript">
    OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="job/thresholdForJob.js"/>');
    $(window).load(function() {
      OpenSpeedMonitor.thresholdforJobs.initVue({jobId: "${job?.id}", scriptId: "${job?.script?.id}"});
    });
</asset:script>