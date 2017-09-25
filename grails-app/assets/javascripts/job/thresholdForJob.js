
"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.thresholdforJobs = (function(){
    
    var initVueComponent = function (data) {
        var jobId = data.jobId
        new Vue({
            el: '#threshold',
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
                },
                addThreshold: function (job, createThresholdUrl) {
                    var thresholdTab = $("#thresholdCreate");
                    var measurand = thresholdTab.find("#measurand").val();
                    var measuredEvent = thresholdTab.find("#measuredEvent").val();
                    var lowerBoundary = thresholdTab.find("#lowerBoundary").val();
                    var upperBoundary = thresholdTab.find("#upperBoundary").val();
                    //var errorContainer = $("#jobGroupErrorContainer");

                    //errorContainer.addClass("hidden");

                    $.ajax({
                        type: 'POST',
                        data: {
                            job: job,
                            measurand: measurand,
                            measuredEvent: measuredEvent,
                            lowerBoundary: lowerBoundary,
                            upperBoundary: upperBoundary
                        },
                        url: createThresholdUrl,
                        success: function () {
                            this.fetchData();
                            this.thresholds.push({
                                measurand: measurand,
                                measuredEvent: measuredEvent,
                                lowerBoundary: lowerBoundary,
                                upperBoundary: upperBoundary
                            });
                        },
                        error: function (e) {
                        }
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