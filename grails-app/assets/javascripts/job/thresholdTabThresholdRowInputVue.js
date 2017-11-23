//= require bower_components/vue/dist/vue.js
//= require job/thresholdTabThresholdRow.js
"use strict"

Vue.component('threshold-row-input', {
    props: ['thresholdField', 'editable', 'isUpperBoundary'],
    data: function() {
        return {
            value: this.thresholdField
        }
    },
    template: '#threshold-tab-threshold-row-input-vue',
    watch: {
        value: function (newValue) {
            this.$emit('update-boundary', {value: newValue, isUpper:this.isUpperBoundary});
        },
        editable: function () {
            this.value = this.thresholdField;
        }
    }
});