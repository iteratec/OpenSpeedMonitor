//= require bower_components/vue/dist/vue.js
//= require job/thresholdTabThresholdRow.js
"use strict"

Vue.component('threshold-row-label', {
    props: ['name'],
    template: '#threshold-tab-threshold-row-label-vue',
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