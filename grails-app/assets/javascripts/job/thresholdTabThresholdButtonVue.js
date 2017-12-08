//= require bower_components/vue/dist/vue.js
//= require job/thresholdTabThreshold.js
"use strict"

Vue.component('threshold-button', {
    props: ['saved', 'editable'],
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
                return "save";
            } else {
                if (this.editable) {
                    return "submit";

                } else {
                    return "edit";
                }
            }
        },
        computedLabelNegative: function () {
            if (!this.saved) {
                return "remove"
            } else {
                if (this.editable) {
                    return "discard";
                } else {
                    return "delete";
                }
            }
        }
    },
    template: '#threshold-tab-threshold-button-vue',
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