//= require bower_components/vue/dist/vue.js
"use strict"

Vue.component('threshold-generate-script', {
    data: function() {
        return {
            tmpThreshold: {}
        }
    },
    props: ['jobId'],
    template: '#threshold-tab-generate-script-vue',
    methods: {
    }
});