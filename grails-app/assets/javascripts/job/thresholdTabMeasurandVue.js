//= require bower_components/vue/dist/vue.js

"use strict"

Vue.component('threshold-measurand', {
    props: ['threshold'],
    template: '#threshold-tab-measurand-vue',
    methods: {
        deleteThreshold: function (threshold) {
            this.$emit('delete-threshold', threshold);
        }
    }
});