//= require bower_components/vue/dist/vue.js
//= require job/thresholdTabThresholdRow.js
"use strict"

Vue.component('threshold-row-input', {
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
    template: '#threshold-tab-threshold-row-input-vue',
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