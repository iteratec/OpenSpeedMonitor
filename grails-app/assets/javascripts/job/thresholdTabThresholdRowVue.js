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
        },
        buttonClicked: function (message) {
            if(message.isPositiveButton){
                if(this.threshold.edit === true) {
                    this.$emit('update-threshold', this.threshold);
                    this.threshold.edit = message.editMode;
                } else {
                    this.changeEditMode(this.threshold, message.editMode);
                }
            }else{
                if(this.threshold.edit === false) {
                    this.$emit('delete-threshold', this.threshold);
                    this.threshold.edit = message.editMode;
                } else {
                    this.changeEditMode(this.threshold, message.editMode);
                }
            }
        }
    }
});