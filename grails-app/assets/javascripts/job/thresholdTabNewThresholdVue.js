//= require bower_components/vue/dist/vue.js
"use strict";

Vue.component('threshold-new-threshold', {
    props: ['threshold', 'measurands'],
    template: '#threshold-tab-new-threshold-vue',
    methods: {
        createThreshold: function (threshold) {
            this.$emit('create-threshold', threshold);
        },
        removeThreshold: function (threshold) {
            this.$emit('remove-new-threshold', threshold);
        }
    }
});