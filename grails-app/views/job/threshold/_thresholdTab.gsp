<app-job-threshold data-job-id="${job?.id}"
                   data-job-scriptId="${job?.script?.id}"
                   data-module-path="src/app/job-threshold/job-threshold.module#ThresholdModule"></app-job-threshold>
<div id="threshold" jobId="${job?.id}" scriptId="${job?.script?.id}">
    <h4>VUE Component</h4>
    <hr>
    <div v-if="activeMeasuredEvents.length !== 0"
         class="container">

        <div id="thresholdList" class="col-md-offset-1">
            <div v-for="measuredEvent in activeMeasuredEvents">
                <threshold-measured-event
                        :measured-event-item="measuredEvent"
                        :available-measured-events="availableMeasuredEvents"
                        :measurands="measurands"
                        v-on:create-threshold="createThreshold"
                        v-on:delete-threshold="deleteThreshold"
                        v-on:update-threshold="updateThreshold"
                        v-on:create-threshold="createThreshold"
                        v-on:remove-measured-event="removeMeasuredEvent">
                </threshold-measured-event>
                <br>
            </div>
        </div>
    </div>
    <br>

    <span v-if="activeMeasuredEvents.length === 0"
          class="">
        <div>
            <g:message code="job.threshold.description"/>
        </div>
        <br>
        <button type="button" class="btn btn-primary"
                @click="addMeasuredEvent()"
                :disabled="availableMeasuredEvents.length === 0">
            <g:message code="job.threshold.addFirstThreshold" default="Add first measured Event"/>
        </button>
    </span>
    <span v-else class="col-sm-offset-1">
        <button type="button"
                class="btn btn-primary"
                @click="addMeasuredEvent()"
                :disabled="availableMeasuredEvents.length === 0">
            <g:message code="job.threshold.addMeasuredEvent" default="Add measured Event"/>
        </button>

        <button class="btn btn-default"
                type="button"
                @click="createScript()">
            <g:message code="job.threshold.createScript" default="Create CI-Script"/>
        </button>
    </span>
</div>

<g:render template="threshold/measuredEventVue"/>