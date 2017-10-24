<div id="threshold" jobId="${job?.id}" scriptId="${job?.script?.id}">
    <div>
        <div id="thresholdList">
            <div v-for="thresholdItem in thresholds">
                <threshold-measured-event :thresholds="thresholdItem"
                                          v-on:delete-threshold="deleteThreshold"
                                          v-on:update-threshold="updateThreshold"></threshold-measured-event>
                <br>
            </div>
        </div>
    </div>

    <br> <br>

    <div>
        <button type="button" class="btn btn-primary"
                @click="changeNewThresholdState()">
            <span v-if="newThresholdState">Hide new Threshold</span>
            <span v-else>New Threshold</span>
        </button>
        <button class="btn btn-default ciButton" type="button" id="copyToClipboard">
            <g:message code="job.threshold.copyToClipboard" default="Copy To Clipboard"/>
        </button><br>

        <span v-if="newThresholdState">
            <threshold-new-threshold :measured-events="measuredEvents"
                                     :measurands="measurands"
                                     v-on:create-threshold="createThreshold"></threshold-new-threshold>
        </span>
    </div>
</div>

<g:render template="thresholdTabMeasuredEventVue"/>
<g:render template="thresholdTabNewThresholdVue"/>

<asset:javascript src="job/thresholdForJob.js"/>

%{--

<asset:script type="text/javascript">
    OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="job/thresholdForJob.js"/>');
    $(window).load(function() {
      OpenSpeedMonitor.thresholdforJobs.initVue({jobId: "${job?.id}", scriptId: "${job?.script?.id}"});
    });
</asset:script>--}%
