//= require bower_components/vue/dist/vue.js
//= require job/threshold/thresholdTabThreshold.js

"use strict";

Vue.component('measurand-select', {
    props: ['availableMeasurands', 'thresholdMeasurand'],
    data: function () {
        return {
            avaMeasurands: this.availableMeasurands,
            selectedMeasurand: this.thresholdMeasurand
        }
    },
    created: function () {
        if(this.avaMeasurands.indexOf(this.selectedMeasurand) === -1){
            this.avaMeasurands.unshift(this.selectedMeasurand)
        }
    },
    watch: {
        availableMeasurands: function () {
            this.avaMeasurands = this.availableMeasurands.slice();
            this.avaMeasurands.unshift(this.thresholdMeasurand)
        },
        selectedMeasurand: function (newMeausrand) {
            this.$emit('update-measurand', newMeausrand);
        },
        thresholdMeasurand: function (newMeausrand) {
            this.selectedMeasurand = newMeausrand;
        }
    },
    template: '#measurand-select-vue'
});