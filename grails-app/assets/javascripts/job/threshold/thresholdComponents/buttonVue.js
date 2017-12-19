//= require bower_components/vue/dist/vue.js
//= require job/threshold/thresholdTabThreshold.js
"use strict";
var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.i18n = OpenSpeedMonitor.i18n || {};
OpenSpeedMonitor.i18n.thresholdButtons = OpenSpeedMonitor.i18n.thresholdButtons || {};

Vue.component('threshold-button', {
    props: ['saved', 'editable', 'valid'],
    computed: {
        computedClass: function () {
            var baseClasses = "thresholdButton margins btn btn-xs ";
            if (!this.saved) {
                return baseClasses + "btn-success";
            } else {
                return baseClasses + "btn-primary";
            }
        },
        computedLabelPositive: function () {
            if (!this.saved) {
                return OpenSpeedMonitor.i18n.thresholdButtons["save"];
            } else {
                if (this.editable) {
                    return OpenSpeedMonitor.i18n.thresholdButtons["submit"];

                } else {
                    return OpenSpeedMonitor.i18n.thresholdButtons["edit"];
                }
            }
        },
        computedLabelNegative: function () {
            if (!this.saved) {
                return OpenSpeedMonitor.i18n.thresholdButtons["remove"];
            } else {
                if (this.editable) {
                    return OpenSpeedMonitor.i18n.thresholdButtons["discard"];
                } else {
                    return OpenSpeedMonitor.i18n.thresholdButtons["delete"];
                }
            }
        }
    },
    template: '#threshold-button-vue',
    methods: {
        onClick: function (isPositiveButton) {
            this.$emit('button-clicked', {
                editMode: !this.editable,
                saved: this.saved,
                isPositiveButton: isPositiveButton
            });
        }
    }
});