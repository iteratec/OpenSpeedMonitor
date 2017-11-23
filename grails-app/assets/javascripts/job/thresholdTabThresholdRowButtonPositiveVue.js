//= require bower_components/vue/dist/vue.js
//= require job/thresholdTabThresholdRow.js
"use strict"

Vue.component('threshold-row-button-positive', {
    props: ['thresholdId'],
    data: function () {
        return {editMode: this.thresholdId === null};
    },
    computed: {
      computedClass: function () {
            var baseClasses = "thresholdButton margins btn btn-xs ";
                if(this.thresholdId === null){
                    return  baseClasses + "btn-success";
                } else {
                    return baseClasses + "btn-primary";
                }


      },
        computedLabelPositive: function () {
            if(!this.thresholdId){
                    return  "save";
            } else {
                if(this.editMode){
                        return "submit";

                }else{
                        return "edit";
                    }

                }

        },
        computedLabelNegative: function () {
            if(!this.thresholdId){
                return "remove"

            } else {
                if(this.editMode){
                    return "discard";

                }else{
                    return "delete";
                }

            }

        }
    },
    template: '#threshold-tab-threshold-row-button-positive-vue',
    methods: {
        onClick: function (isPositiveButton) {
            this.editMode = !this.editMode;
            this.$emit('button-clicked', {editMode: this.editMode, isPositiveButton:isPositiveButton});
        }
    }
});