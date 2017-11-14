//= require bower_components/vue/dist/vue.js
"use strict";

    Vue.component('threshold-measured-event', {
        data: function() {
            return{
                newThresholdCount: 0
            }
        },
        props: ['thresholds', 'measurands'],
        template: '#threshold-tab-measured-event-vue',
        methods: {
            addMetric: function () {
                this.newThresholdCount++;
                this.thresholds.thresholdList.push({newThresholdId: this.newThresholdCount,
                                                    edit: false,
                                                    threshold: null})
            },
            removeNewThreshold: function(newThreshold){
                this.thresholds.thresholdList.splice(this.thresholds.thresholdList.indexOf(newThreshold), 1)
            },
            deleteThreshold: function (threshold) {
                this.$emit('delete-threshold', threshold);
            },
            updateThreshold: function (threshold) {
                this.$emit('update-threshold', threshold);
            },
            createThreshold: function (threshold) {
                this.$emit('create-threshold', threshold);
            }
        }
    });