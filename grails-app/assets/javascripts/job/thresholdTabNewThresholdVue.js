//= require bower_components/vue/dist/vue.js
"use strict";

Vue.component('threshold-new-threshold', {
    props: ['threshold', 'measured-events', 'measurands'],
    data: function() {
        return {
            newThreshold: {}
        }
    },
    template: '#threshold-tab-new-threshold-vue',
    methods: {
        createThreshold: function (threshold) {
            this.$emit('create-threshold', threshold);
        },
        removeThreshold: function () {
            this.$emit('remove-new-threshold', this.threshold);
        }
    }
});