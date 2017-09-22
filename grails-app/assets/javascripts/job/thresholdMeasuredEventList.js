
"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.thresholdMeasuredEventList = (function(){

    var initVueComponent = function (data) {
        var jobId = data.jobId
        new Vue({
            el: '#thresholdList',
            data: {
                thresholds: null
            },
            computed: {
            },
            created: function () {
                this.fetchData()
            },
            methods: {
                fetchData: function () {
                    var self = this;
                     getThresholdsForJob(jobId).success(function(result) {
                         self.thresholds = result;
                     }).error(function(e) {
                         console.log(e);
                     });
                }
            }
        });
    };

    var getThresholdsForJob = function (jobId) {
        var targetUrl = "/job/getThresholdsForJob";
           return $.ajax({
                type: 'GET',
                url: targetUrl,
                data: { jobId: jobId }
            });
    };

    var getMeasuredEvents = function (data) {
        var scriptId = data.scriptId;
        var targetUrl = data.targetUrl;

        if(scriptId && targetUrl){
            $.ajax({
                type: 'POST',
                url: targetUrl,
                data: { scriptId: scriptId },
                success : function(result) {
                    OpenSpeedMonitor.domUtils.updateSelectOptions($('.measured-event-select'), result, null);
                }
                ,
                error : function() {
                    return ""
                }
            });
        }
    };
    return{
        init: getMeasuredEvents,
        initVue: initVueComponent
    }
})();