//= require bower_components/vue/dist/vue.js

"use strict"

Vue.component('threshold-measurand', {
    props: ['threshold', 'availableMeasurands'],
    data: function () {
        return {
            avaMeasurands: this.availableMeasurands
        }
    },
    watch: {
        availableMeasurands: function () {
            this.avaMeasurands = this.availableMeasurands.slice();
            this.avaMeasurands.push(this.threshold.threshold.measurand)
        }
    },
    template: '#threshold-tab-measurand-vue',
    methods: {
        deleteThreshold: function (threshold) {
            this.$emit('delete-threshold', threshold);
        },
        updateThreshold: function (threshold) {
            this.$emit('update-threshold', threshold);
        },
        createThreshold: function (threshold) {
            this.$emit('create-threshold', threshold);
        },
        removeNewThreshold: function (threshold) {
            this.$emit('remove-new-threshold', threshold);
        }
    }
});