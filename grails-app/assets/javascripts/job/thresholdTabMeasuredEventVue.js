//= require bower_components/vue/dist/vue.js
"use strict";

    Vue.component('threshold-measured-event', {
        props: [
            'thresholds',
            'measurands',
            'test'
        ],
        data: function() {
            return {
                copiedMeasurands: this.measurands.slice()
            }
        },
        computed: {
            availableMeasurands: function () {
                var self = this;
                self.copiedMeasurands = this.measurands.slice();
                self.thresholds.thresholdList.forEach(function (threshold) {
                    if(threshold.threshold.measurand) {
                        var compareTo = threshold.threshold.measurand;
                        self.copiedMeasurands = self.copiedMeasurands.filter(function (element) {
                            return element.name !== compareTo.name;
                        })
                    }
                });
                return this.copiedMeasurands;
            }
        },
        template: '#threshold-tab-measured-event-vue',
        create:{
        },
        methods: {
            addMetric: function () {
                this.thresholds.thresholdList.push({
                    edit: true,
                    saved: false,
                    threshold: {
                        measuredEvent: this.thresholds.measuredEvent,
                        measurand: this.availableMeasurands[0]
                    }
                })
            },
            removeNewThreshold: function (newThreshold) {
                this.thresholds.thresholdList.splice(this.thresholds.thresholdList.indexOf(newThreshold), 1)

                if(this.thresholds.thresholdList.length === 0){
                    this.$emit('remove-measured-event', this.thresholds);
                }
            },
            deleteThreshold: function (threshold) {
                this.$emit('delete-threshold', threshold);
            },
            updateThreshold: function (threshold) {
                this.$emit('update-threshold', threshold);
            },
            createThreshold: function (threshold) {
                if(threshold.threshold.measuredEvent) {
                    threshold.threshold.measuredEvent = this.thresholds.measuredEvent;
                }

                this.$emit('create-threshold', threshold);
            }
        }
    });
