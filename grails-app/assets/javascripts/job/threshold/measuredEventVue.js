//= require bower_components/vue/dist/vue.js
//= require job/threshold/thresholdVue.js
//= require job/threshold/labelVue.js
//= require job/threshold/inputVue.js
//= require job/threshold/buttonVue.js
"use strict";

Vue.component('threshold-measured-event', {
    props: [
        'measuredEventItem',
        'measurands',
        'availableMeasuredEvents'
    ],
    data: function () {
        return {
            copiedMeasurands: this.measurands.slice(),
            selectedMeasuredEvent: this.availableMeasuredEvents[0]
        }
    },
    computed: {
        availableMeasurands: function () {
            var self = this;
            self.copiedMeasurands = this.measurands.slice();
            self.measuredEventItem.thresholdList.forEach(function (thresholdItem) {
                if (thresholdItem.threshold.measurand) {
                    var compareTo = thresholdItem.threshold.measurand;
                    self.copiedMeasurands = self.copiedMeasurands.filter(function (element) {
                        return element.name !== compareTo.name;
                    })
                }
            });
            return self.copiedMeasurands;
        }
    },
    template: '#threshold-measured-event-vue',
    methods: {
        addMetric: function () {
            if (this.availableMeasurands.length > 0) {
                this.measuredEventItem.thresholdList.push({
                    edit: true,
                    saved: false,
                    threshold: {
                        measuredEvent: this.measuredEventItem.measuredEvent,
                        measurand: this.availableMeasurands[0],
                        lowerBoundary: 0,
                        upperBoundary: 0
                    }
                })
            }
        },
        removeThreshold: function (thresholdItem) {
            this.measuredEventItem.thresholdList.splice(this.measuredEventItem.thresholdList.indexOf(thresholdItem), 1);

            if (this.measuredEventItem.thresholdList.length === 0) {
                this.$emit('remove-measured-event', this.measuredEventItem);
            }
        },
        deleteThreshold: function (thresholdItem) {
            this.$emit('delete-threshold', thresholdItem);
        },
        updateThreshold: function (thresholdItem) {
            this.$emit('update-threshold', thresholdItem);
        },
        createThreshold: function (thresholdItem) {
            if (!Object.keys(thresholdItem.threshold.measuredEvent).length) {
                this.measuredEventItem.measuredEvent = this.selectedMeasuredEvent;
                thresholdItem.threshold.measuredEvent = this.measuredEventItem.measuredEvent;
            }
            this.$emit('create-threshold', thresholdItem);
        }
    }
});
