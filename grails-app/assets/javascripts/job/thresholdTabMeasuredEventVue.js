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
                selectedEvent: {}
            }
        },
        template: '#threshold-tab-measured-event-vue',
        create:{

        },
        methods: {
            addMetric: function () {
                this.thresholds.thresholdList.push({
                    edit: false,
                    saved: false,
                    threshold: {
                        measuredEvent: this.thresholds.measuredEvent
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
                if(!Object.keys(this.thresholds.measuredEvent).length){
                    this.thresholds.measuredEvent = this.selectedEvent;
                }

                threshold.threshold.measuredEvent = this.thresholds.measuredEvent;

                this.$emit('create-threshold', threshold);
            }
        }
    });