//= require bower_components/vue/dist/vue.js
//= require job/thresholdTabThresholdRow.js
"use strict"

Vue.component('threshold-row-button', {
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
    watch: {
        saved: function (newValue) {
        },
        editable: function (newValue) {
            console.log("editable changed")
        }
    },
    template: '#threshold-tab-threshold-row-button-vue',
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