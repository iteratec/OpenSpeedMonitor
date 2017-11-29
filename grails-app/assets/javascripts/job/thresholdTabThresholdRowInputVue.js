//= require bower_components/vue/dist/vue.js
//= require job/thresholdTabThresholdRow.js
"use strict"

Vue.component('threshold-row-input', {
    props: ['thresholdField', 'editable', 'fieldName'],
    data: function () {
        return {
            value: this.thresholdField
        }
    },
    template: '#threshold-tab-threshold-row-input-vue',
    watch: {
        value: function (newValue) {
            this.$emit('update-boundary', {value: newValue, fieldName: this.fieldName});
        },
        editable: function () {
            this.value = this.thresholdField;
        }
    }
});