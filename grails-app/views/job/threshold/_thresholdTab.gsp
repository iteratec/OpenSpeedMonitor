<div id="threshold" jobId="${job?.id}" scriptId="${job?.script?.id}">
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
          class="col-md-offset-5">
        <button type="button" class="btn btn-primary"
                @click="addMeasuredEvent()"
                :disabled="availableMeasuredEvents.length === 0">Add first threshold</button>
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
            <g:message code="job.threshold.createScript" default="Download CI-Script"/>
        </button>
    </span>

</div>

<g:render template="threshold/measuredEventVue"/>

%{--<asset:javascript src="/job/threshold/rootVue.js"/>--}%