//= require job/threshold/thresholdTabThreshold.js
"use strict";

Vue.component('threshold-label', {
    props: ['name'],
    template: '#threshold-label-vue',
    computed: {
        classObject: function () {
            return 'thresholdLabel label'+this.name;
        },
        showFront: function () {
            return this.name!=="Good";
        },
        showBehind: function () {
            return this.name!=="Bad";
        }
    }
});