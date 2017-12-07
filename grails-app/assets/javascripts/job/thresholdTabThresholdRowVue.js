//= require bower_components/vue/dist/vue.js
//= require job/thresholdTabMeasurandVue.js
"use strict"

Vue.component('threshold-row', {
    data: function() {
        return {
            tmpThreshold: {},
            upperField: "upperField",
            lowerField: "lowerField",
            avaMeasurands: this.availableMeasurands
        }
    },
    props: ['threshold', 'availableMeasurands'],
    template: '#threshold-tab-threshold-row-vue',
    watch: {
        availableMeasurands: function () {
            this.avaMeasurands = this.availableMeasurands.slice();
            if(this.threshold.threshold.measurand){
                this.avaMeasurands.push(this.threshold.threshold.measurand)
            }

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

            threshold.edit = state;
        },
        updateThresholdBoundary: function (message) {
            if(message.fieldName  === this.upperField){
                this.threshold.threshold.upperBoundary = message.value;
            }else{
                this.threshold.threshold.lowerBoundary = message.value;
            }
        },
        buttonClicked: function (message) {
            if(message.isPositiveButton){
                if(!this.threshold.saved){
                    this.$emit('create-threshold', this.threshold);
                } else if(!message.editMode) {
                    this.$emit('update-threshold', this.threshold);
                    this.threshold.edit = message.editMode;
                } else {
                    this.changeEditMode(this.threshold, message.editMode);
                }
            }else{
                if(!this.threshold.saved){
                    this.$emit('remove-new-threshold', this.threshold);
                } else if(message.editMode) {
                    this.$emit('delete-threshold', this.threshold);
                } else {
                    this.changeEditMode(this.threshold, message.editMode);
                }
            }
        }
    }
});