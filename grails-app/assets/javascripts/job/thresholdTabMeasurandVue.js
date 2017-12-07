//= require bower_components/vue/dist/vue.js

"use strict"

Vue.component('threshold-measurand', {
    props: ['availableMeasurands', 'thresholdMeasurand'],
    data: function () {
        return {
            avaMeasurands: this.availableMeasurands,
            selectedMeasurand: this.thresholdMeasurand
        }
    },
    created: function () {
        if(this.avaMeasurands.indexOf(this.selectedMeasurand) === -1){
            this.avaMeasurands.push(this.selectedMeasurand)
        }
    },
    watch: {
        availableMeasurands: function () {
            this.avaMeasurands = this.availableMeasurands.slice();
            this.avaMeasurands.push(this.thresholdMeasurand)
        },
        selectedMeasurand: function (newMeausrand) {
            this.$emit('update-measurand', newMeausrand);
        },
        thresholdMeasurand: function (newMeausrand) {
            this.selectedMeasurand = newMeausrand;
        }
    },
    template: '#threshold-tab-measurand-vue'
});