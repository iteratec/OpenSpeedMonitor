//= require bower_components/vue/dist/vue.js
//= require job/threshold/thresholdTabThreshold.js
"use strict";

Vue.component('threshold-input', {
    props: ['thresholdField', 'thresholdMeasurand','editable', 'fieldName'],
    data: function () {
        return {
            value: this.thresholdField
        }
    },
    computed: {
        computedUnit: function () {
            switch(this.thresholdMeasurand.unit.name){
                case "MILLISECONDS":
                    return "ms";
                case "MEGABYTE":
                    return "mb";
                case "PERCENT":
                    return "%";
                case "NUMBER":
                    return "#";
                default:
                    return "ms";
            }
        }
    },
    template: '#threshold-input-vue',
    watch: {
        value: function (newValue) {
            this.$emit('update-boundary', {value: newValue, fieldName: this.fieldName});
        },
        editable: function () {
            this.value = this.thresholdField;
        },
        thresholdField: function (newValue) {
            this.value = newValue;
        }
    }
});