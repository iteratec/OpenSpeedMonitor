"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.thresholdforJobs = (function () {

    var initVueComponent = function (data) {
        var jobId = data.jobId;
        var scriptId = data.scriptId;

        new Vue({
            el: '#threshold',
            data: {
                thresholds: [],
                measuredEvents: [],
                measurands: [],
                newThreshold: {},
                tmpThreshold: {}
            },
            computed: {},
            created: function () {
                this.getMeasurands("/job/getMeasurands")
                this.getMeasuredEvents(scriptId, "/script/getMeasuredEventsForScript")
                this.fetchData()
            },
            methods: {
                fetchData: function () {
                    var self = this;
                    getThresholdsForJob(jobId).success(function (result) {
                        result.forEach(function(resultThreshold) {
                            self.thresholds.push({
                                threshold: resultThreshold,
                                edit: false
                            })
                        });
                    }).error(function (e) {
                        console.log(e);
                    });
                },
                getMeasuredEvents: function (scriptId, targetUrl) {
                    var self = this;
                    if (scriptId && targetUrl) {
                        $.ajax({
                            type: 'GET',
                            url: targetUrl,
                            data: {scriptId: scriptId},
                            success: function (result) {
                                self.measuredEvents = result;
                            },
                            error: function () {
                                return ""
                            }
                        });
                    }
                },
                getMeasurands: function (targetUrl) {
                    var self = this;
                    if (targetUrl) {
                        $.ajax({
                            type: 'GET',
                            url: targetUrl,
                            data: {},
                            success: function (result) {
                                self.measurands = result;
                            }
                            ,
                            error: function () {
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
                        success: function (result) {
                            self.newThreshold.id = result.thresholdId;
                            self.thresholds.push({
                                threshold: self.newThreshold,
                                edit: false
                            });
                            self.newThreshold = {};
                            console.log("success");
                        },
                        error: function (e) {
                            console.log(e);
                        }
                    });
                },
                deleteThreshold: function (threshold, deleteThresholdUrl) {
                    var self = this;
                    var deletedThreshold = threshold;
                    $.ajax({
                        type: 'POST',
                        data: {
                            thresholdId: deletedThreshold.threshold.id
                        },
                        url: deleteThresholdUrl,
                        success: function () {
                            self.thresholds.splice(self.thresholds.indexOf(deletedThreshold), 1)
                        },
                        error: function (e) {
                            console.log(e);
                        }
                    });
                },
                updateThreshold: function (threshold, updateThresholdUrl) {
                    var self = this;
                    var updatedThreshold = threshold;
                    $.ajax({
                        type: 'POST',
                        data: {
                            thresholdId: updatedThreshold.threshold.id,
                            measurand: updatedThreshold.threshold.measurand.name,
                            measuredEvent: updatedThreshold.threshold.measuredEvent.id,
                            lowerBoundary: updatedThreshold.threshold.lowerBoundary,
                            upperBoundary: updatedThreshold.threshold.upperBoundary
                        },
                        url: updateThresholdUrl,
                        success: function () {
                            updatedThreshold.edit = false;
                            self.thresholds[self.thresholds.indexOf(updatedThreshold)] = updatedThreshold;
                        },
                        error: function (e) {
                            console.log(e);
                        }
                    });
                },
                changeEditMode: function (threshold, state) {
                    if(state){
                        //shadow copy
                        this.tmpThreshold = Object.assign({}, threshold.threshold);
                    }else{
                        threshold.threshold = this.tmpThreshold;
                        this.tmpThreshold = {};
                    }
                    threshold.edit = state;
                }
            }
        });
    };

    var getThresholdsForJob = function (jobId) {
        var targetUrl = "/job/getThresholdsForJob";
        return $.ajax({
            type: 'GET',
            url: targetUrl,
            data: {jobId: jobId}
        });
    };

    return {
        initVue: initVueComponent
    }
})();