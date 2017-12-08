//= require bower_components/vue/dist/vue.js
//= require job/thresholdTabThreshold.js
"use strict"

Vue.component('threshold-label', {
    props: ['name'],
    template: '#threshold-tab-threshold-label-vue',
    computed: {
        classObject: function () {
            return 'label'+this.name;
        },
        showFront: function () {
            return this.name!=="Good";
        },
        showBehind: function () {
            return this.name!=="Bad";
        }
    }
});