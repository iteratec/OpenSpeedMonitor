//= require bower_components/vue/dist/vue.js
//= require bower_components/jquery/jquery.min.js
"use strict"

Vue.component('threshold-generate-script', {
    data: function() {
        return {
            runtime: 180,
            intervalTime: 30,
            numberOfIntervals: 6,
            picked:"allGood"
        }
    },
    computed: {
        calcIntervals: function (){
            this.numberOfIntervals = Math.round(this.runtime / this.intervalCycle);

            return this.numberOfIntervals
        }
    },
    props: ['jobId'],
    template: '#threshold-tab-generate-script-vue',
    methods: {
        createScript: function () {
            $.ajax({
                type: 'GET',
                url: "job/getCiScript",
                data: {
                    jobId: jobId,
                    intervalTime: this.intervalTime,
                    numberOfIntervals: this.numberOfIntervals,
                    allGood: this.picked === "allGood"
                },
                success: function (result) {
                    self.measuredEvents = result;
                },
                error: function () {
                    return ""
                }
            });
        }
    }
});