//= require job/threshold/thresholdComponents/buttonVue.js
"use strict";
Vue.component('threshold-confirm-button', {
    props: {},
    data: function() {
        return {
            confirm: false
        };
    },
    template: '#threshold-confirm-button-vue',
    methods: {
        confirmDelete: function (confirmed) {
            this.$emit('delete-item', confirmed);
        }
    }
});
