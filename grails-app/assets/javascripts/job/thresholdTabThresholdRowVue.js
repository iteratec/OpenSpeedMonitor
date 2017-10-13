//= require bower_components/vue/dist/vue.js
//= require job/thresholdTabMeasurandVue.js
"use strict"

Vue.component('threshold-row', {
    props: ['threshold'],
    template: '#threshold-tab-threshold-row-vue',
    methods: {
        deleteThreshold: function (threshold) {
            this.$emit('delete-threshold', threshold);
        }
    }
});