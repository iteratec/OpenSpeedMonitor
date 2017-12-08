//= require bower_components/vue/dist/vue.js
//= require job/threshold/measuredEventVue.js
//= require job/threshold/thresholdComponents/labelVue.js
//= require job/threshold/thresholdComponents/inputVue.js
//= require job/threshold/thresholdComponents/buttonVue.js
//= require job/threshold/thresholdComponents/measurandSelect.js

"use strict";

Vue.component('threshold', {
    props: ['threshold-item', 'available-measurands'],
    data: function() {
        return {
            tmpThreshold: {},
            upperField: "upperField",
            lowerField: "lowerField",
            avaMeasurands: this.availableMeasurands
        }
    },
    template: '#threshold-vue',
    watch: {
        availableMeasurands: function () {
            this.avaMeasurands = this.availableMeasurands.slice();
            if(this.thresholdItem.threshold.measurand){
                this.avaMeasurands.push(this.thresholdItem.threshold.measurand)
            }

        }
    },
    created: function () {
        if(this.thresholdItem.threshold.measurand === {}){
            this.thresholdItem.threshold.measurand = this.avaMeasurands[0];
        }
    },
    methods: {
        changeEditMode: function (threshold, state) {
            if (state) {
                //shadow copy
                this.tmpThreshold = Object.assign({}, threshold.threshold);
            } else {
                threshold.threshold = this.tmpThreshold;
                this.tmpThreshold = {};
            }

            this.thresholdItem.edit = state;
        },
        updateMeasurand: function (newMeasurand) {
            this.thresholdItem.threshold.measurand = newMeasurand;
        },
        updateThresholdBoundary: function (message) {
            if(message.fieldName  === this.upperField){
                this.thresholdItem.threshold.upperBoundary = message.value;
            }else{
                this.thresholdItem.threshold.lowerBoundary = message.value;
            }
        },
        buttonClicked: function (message) {
            if(message.isPositiveButton){
                if(!this.thresholdItem.saved){
                    this.$emit('create-threshold', this.thresholdItem);
                } else if(!message.editMode) {
                    this.$emit('update-threshold', this.thresholdItem);
                    this.thresholdItem.edit = message.editMode;
                } else {
                    this.changeEditMode(this.thresholdItem, message.editMode);
                }
            }else{
                if(!this.thresholdItem.saved){
                    this.$emit('remove-new-threshold', this.thresholdItem);
                } else if(message.editMode) {
                    this.$emit('delete-threshold', this.thresholdItem);
                } else {
                    this.changeEditMode(this.thresholdItem, message.editMode);
                }
            }
        }
    }
});