//= require bower_components/vue/dist/vue.js
//= require job/thresholdTabMeasurandVue.js
"use strict"

Vue.component('threshold-row', {
    data: function() {
        return {
            tmpThreshold: {}
        }
    },
    props: ['threshold'],
    template: '#threshold-tab-threshold-row-vue',
    methods: {
        deleteThreshold: function (threshold) {
            this.$emit('delete-threshold', threshold);
        },
        updateThreshold: function (threshold) {
            this.$emit('update-threshold', threshold);
        },
        changeEditMode: function (threshold, state) {
            if (state) {
                //shadow copy
                this.tmpThreshold = Object.assign({}, threshold.threshold);
                threshold.edit = state;

            } else {
                threshold.threshold = this.tmpThreshold;
                this.tmpThreshold = {};
                threshold.edit = state;
            }
        },
        updateThresholdBoundary: function (message) {
            if(message.isUpper){
                this.threshold.threshold.upperBoundary = message.value;
            }else{
                this.threshold.threshold.lowerBoundary = message.value;
            }
        }
    }

});