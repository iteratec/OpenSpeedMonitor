//= require bower_components/vue/dist/vue.js
"use strict";

    Vue.component('threshold-measured-event', {
        props: ['measured-event-item', 'measurands', 'available-measured-events'],
        data: function() {
            return {
                copiedMeasurands: this.measurands.slice()
            }
        },
        computed: {
            availableMeasurands: function () {
                var self = this;
                self.copiedMeasurands = this.measurands.slice();
                self.measuredEventItem.thresholdList.forEach(function (thresholdItem) {
                    if(thresholdItem.threshold.measurand) {
                        var compareTo = thresholdItem.threshold.measurand;
                        self.copiedMeasurands = self.copiedMeasurands.filter(function (element) {
                            return element.name !== compareTo.name;
                        })
                    }
                });
                return self.copiedMeasurands;
            }
        },
        template: '#threshold-tab-measured-event-vue',
        methods: {
            addMetric: function () {
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
            },
            removeNewThreshold: function (newThresholdItem) {
                this.measuredEventItem.thresholdList.splice(this.measuredEventItem.thresholdList.indexOf(newThresholdItem), 1);

                if(this.measuredEventItem.thresholdList.length === 0){
                    console.log("remove")
                    this.$emit('remove-measured-event', this.measuredEventItem);
                }
            },
            deleteThreshold: function (newThresholdItem) {
                this.$emit('delete-threshold', newThresholdItem);
            },
            updateThreshold: function (newThresholdItem) {
                this.$emit('update-threshold', newThresholdItem);
            },
            createThreshold: function (newThresholdItem) {
                if(newThresholdItem.threshold.measuredEventItem) {
                    newThresholdItem.threshold.measuredEventItem = this.measuredEventItem.measuredEvent;
                }
                this.$emit('create-threshold', newThresholdItem);
            }
        }
    });
