<%@ page import="de.iteratec.osm.result.Threshold" %>
<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<%@ page import="de.iteratec.osm.result.Measurand" %>

<div id="threshold">
    <div>
        <div id="thresholdList">
            <div>
                <div v-for="thresholdItem in thresholds">{{ thresholdItem.measuredEvent.name }}
                    <div v-for="threshold in thresholdItem.thresholdList">
                        <div v-if="threshold.edit">
                            <div class="thresholdMeasurand">{{ threshold.threshold.measurand.name }}</div><br>
                            <div class="thresholdBoundaries row">
                                <div class="col-md-2">
                                    <label class="text-success">
                                        Good
                                    </label>
                                    <label><</label>
                                </div>
                                <div class="col-md-2">
                                <input id="lowerBoundaryEdit" class="form-control" type="number" min="1"
                                       name="lowerBoundary" cols="30"
                                       rows="5"
                                       maxlength="150"
                                       v-model="threshold.threshold.lowerBoundary"/>
                                </div>
                                <div class="col-md-2">
                                    <label>
                                        <
                                    </label>
                                    <label class="text-warning">
                                        OK
                                    </label>
                                    <label><</label>
                                </div>
                                <div class="col-md-2">
                                <input id="upperBoundaryEdit" class="form-control" type="number" min="1"
                                       name="upperBoundary" cols="40"
                                       rows="5"
                                       maxlength="150"
                                       v-model="threshold.threshold.upperBoundary"/>
                                </div>
                                <div class="col-md-2">
                                    <label>
                                        <
                                    </label>
                                    <label class="text-danger">
                                        Bad
                                    </label>
                                </div>
                                <button type="button"
                                        class="btn btn-warning btn-xs"
                                        @click="updateThreshold(threshold, '${g.createLink([controller: 'threshold', action: 'updateAsync'])}')">submit</button>
                                <button type="button"
                                        class="margins btn btn-danger btn-xs"
                                        @click="changeEditMode(threshold, false)">discard</button>
                            </div>
                        </div>

                        <div v-else class="thresholdMeasurand">{{ threshold.threshold.measurand.name }}<br>

                            <div class="thresholdBoundaries row">
                                <div class="col-md-2">
                                    <label class="text-success">
                                        Good
                                    </label>
                                    <label><</label>
                                </div>
                                <label class="col-md-2">
                                    {{ threshold.threshold.lowerBoundary }}
                                </label>
                                <div class="col-md-2">
                                    <label>
                                        <
                                    </label>
                                    <label class="text-warning">
                                        OK
                                    </label>
                                    <label><</label>
                                </div>
                                <label class="col-md-2">
                                    {{ threshold.threshold.upperBoundary }}
                                </label>

                                <div class="col-md-2">
                                    <label>
                                        <
                                    </label>
                                    <label class="text-danger">
                                        Bad
                                    </label>
                                </div>
                                <div class="col-md-2">
                                <button type="button"
                                        class="btn btn-warning btn-xs"
                                        @click="changeEditMode(threshold, true)">edit</button>
                                <button type="button"
                                        class="btn btn-danger btn-xs"
                                        @click="deleteThreshold(threshold, '${g.createLink([controller: 'threshold', action: 'deleteAsync'])}')">delete</button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <br>
                </div>
            </div>
        </div>
    </div>


    <br> <br>

    <div>New threshold: <br>
        <select id="measuredEvent" name="measuredEvent" class="measured-event-select"
                v-model="newThreshold.measuredEvent">
            <option v-for="measuredEvent in measuredEvents" :value="measuredEvent">{{measuredEvent.name}}</option>
        </select> <br>

        <select class="thresholdMeasurand" id="measurand" name="measurand" v-model="newThreshold.measurand">
            <option v-for="measurand in measurands" :value="measurand">{{measurand.name}}</option>
        </select><br>

        <div class="thresholdBoundaries form-inline">
            <label class="text-success">
                Good
            </label>
            <label>
                <
            </label>
            <input id="lowerBoundary" class="form-control" type="number" min="1"
                   name="lowerBoundary" cols="30"
                   rows="5"
                   maxlength="150"
                   v-model="newThreshold.lowerBoundary"/>
            <label>
                <
            </label>
            <label class="text-warning">
                OK
            </label>
            <label>
                <
            </label>
            <input id="upperBoundary" class="form-control" type="number" min="1"
                   name="upperBoundary" cols="40"
                   rows="5"
                   maxlength="150"
                   v-model="newThreshold.upperBoundary"/>
            <label>
                <
            </label>
            <label class="text-danger">
                Bad
            </label>
            <button type="button" class="margins btn btn-default"
                    @click="addThreshold('${job}', '${g.createLink([controller: 'threshold', action: 'createAsync'])}')">
                <i class="fa text-success fa-lg fa-plus"></i>
            </button>
        </div>
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