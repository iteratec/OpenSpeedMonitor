
"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.thresholdforJobs = (function(){
    
    var initVueComponent = function (data) {
        var jobId = data.jobId;
        var scriptId = data.scriptId;

        new Vue({
            el: '#threshold',
            data: {
                thresholds: null,
                measuredEvents: [],
                measurands: [],
                newThreshold: {}
            },
            computed: {
            },
            created: function () {
                this.getMeasurands("/job/getMeasurands")
                this.getMeasuredEvents(scriptId, "/script/getMeasuredEventsForScript")
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
                getMeasuredEvents: function (scriptId, targetUrl) {
                    var self = this;
                    if(scriptId && targetUrl){
                        $.ajax({
                            type: 'POST',
                            url: targetUrl,
                            data: { scriptId: scriptId },
                            success : function(result) {
                               self.measuredEvents = result;
                            }
                            ,
                            error : function() {
                                return ""
                            }
                        });
                    }
                },
                getMeasurands: function (targetUrl) {
                    var self = this;
                    if(targetUrl){
                        $.ajax({
                            type: 'POST',
                            url: targetUrl,
                            data: {},
                            success : function(result) {
                                self.measurands = result;
                            }
                            ,
                            error : function() {
                                return ""
                            }
                        });
                    }
                },
                addThreshold: function (job, createThresholdUrl) {
                    var self = this;
                    $.ajax({
                        type: 'POST',
                        data: {
                            job: job,
                            measurand: this.newThreshold.measurand.name,
                            measuredEvent: this.newThreshold.measuredEvent.id,
                            lowerBoundary: this.newThreshold.lowerBoundary,
                            upperBoundary: this.newThreshold.upperBoundary
                        },
                        url: createThresholdUrl,
                        success: function () {
                            self.thresholds.push(self.newThreshold);
                            self.newThreshold = {};
                            console.log("success");
                        },
                        error: function (e) {
                            console.log(e);
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

    return{
        initVue: initVueComponent
    }
})();