//= require bower_components/vue/dist/vue.js
//= require bower_components/jquery/jquery.min.js
//= require bower_components/file-saver/FileSaver.min.js
//= require job/thresholdTabMeasuredEventVue.js
//= require job/thresholdTabMeasurandVue.js
//= require job/thresholdTabThresholdRowVue.js
//= require job/thresholdTabNewThresholdVue.js

"use strict";

new Vue({
    el: '#threshold',
    data: {
        thresholds: [],
        measuredEvents: [],
        measurands: [],
        tmpThreshold: {},
        newThresholdState: false,
        jobId: "",
        scriptId: ""
    },
    beforeMount: function () {
        this.jobId = this.$el.attributes['jobId'].value;
        this.scriptId = this.$el.attributes['scriptId'].value;
    },
    computed: {},
    mounted: function () {
        this.getMeasurands("/job/getMeasurands");
        this.getMeasuredEvents(this.scriptId, "/script/getMeasuredEventsForScript");
        this.fetchData();
    },
    methods: {
        fetchData: function () {
            this.thresholds = [];
            var self = this;
            this.getThresholdsForJob(this.jobId).success(function (result) {
                result.forEach(function (resultEvent) {
                    var thresholdsForEvent = [];
                    resultEvent.thresholds.forEach(function (threshold) {
                        thresholdsForEvent.push({
                            threshold: threshold,
                            edit: false
                        })
                    });
                    self.thresholds.push({
                        measuredEvent: resultEvent.measuredEvent,
                        thresholdList: thresholdsForEvent
                    })
                })
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
                    },
                    error: function () {
                        return ""
                    }
                });
            }
        },
        createThreshold: function (newThreshold) {
            var self = this;
            $.ajax({
                type: 'POST',
                data: {
                    job: this.jobId,
                    measurand: newThreshold.measurand.name,
                    measuredEvent: newThreshold.measuredEvent.id,
                    lowerBoundary: newThreshold.lowerBoundary,
                    upperBoundary: newThreshold.upperBoundary
                },
                url: "/threshold/createAsync",
                success: function (result) {
                    newThreshold.id = result.thresholdId;

                    var added = false;
                    //Add threshold to measured event
                    self.thresholds.forEach(function (measuredEventItem) {
                        if (measuredEventItem.measuredEvent.id === newThreshold.measuredEvent.id) {
                            measuredEventItem.thresholdList.push({
                                threshold: newThreshold,
                                edit: false
                            });

                            added = true;
                        }
                    });

                    //Add measured event if it is not existing
                    if (!added) {
                        var list = [{
                            threshold: newThreshold,
                            edit: false
                        }];
                        self.thresholds.push({
                            measuredEvent: newThreshold.measuredEvent,
                            thresholdList: list
                        });
                    }

                    self.newThreshold = {};
                    self.changeNewThresholdState();
                    console.log("success");
                },
                error: function (e) {
                    console.log(e);
                }
            });
        },
        deleteThreshold: function (threshold) {
            var self = this;
            var deletedThreshold = threshold;
            $.ajax({
                type: 'POST',
                data: {
                    thresholdId: deletedThreshold.threshold.id
                },
                url: "/threshold/deleteAsync",
                success: function () {
                    self.thresholds.forEach(function (measuredEventItem) {
                        //remove threshold from measured event
                        if (measuredEventItem.measuredEvent.id === deletedThreshold.threshold.measuredEvent.id) {
                            measuredEventItem.thresholdList.splice(measuredEventItem.thresholdList.indexOf(deletedThreshold), 1);

                            //remove measured event
                            if (measuredEventItem.thresholdList.length === 0) {
                                self.thresholds.splice(self.thresholds.indexOf(measuredEventItem), 1);
                            }
                        }
                    });
                },
                error: function (e) {
                    console.log(e);
                }
            });
        },
        updateThreshold: function (threshold) {
            if (threshold.threshold.lowerBoundary < threshold.threshold.upperBoundary) {
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
                    url: "/threshold/updateAsync",
                    success: function () {
                        self.thresholds.forEach(function (measuredEventItem) {
                            if (measuredEventItem.measuredEvent.id === updatedThreshold.threshold.measuredEvent.id) {
                                updatedThreshold.edit = false;
                                self.tmpThreshold = {};
                                measuredEventItem.thresholdList[measuredEventItem.thresholdList.indexOf(updatedThreshold)] = updatedThreshold;
                            }
                        });
                    },
                    error: function (e) {
                        console.log(e);
                    }
                });
            } else {
                alert("Die obere Grenze muss größer als die untere Grenze sein!")
            }
        },
        getThresholdsForJob: function (jobId) {
            var targetUrl = "/job/getThresholdsForJob";
            return $.ajax({
                type: 'GET',
                url: targetUrl,
                data: {jobId: jobId}
            });
        },
        changeNewThresholdState: function () {
            this.newThresholdState = !this.newThresholdState
        },
        createScript: function () {
            var self = this;
            $.ajax({
                type: 'GET',
                url: "/job/getCiScript",
                data: {
                    jobId: this.jobId
                },
                success: function (result) {
                    console.log(result)
                    var filename = "CI_Script_" + self.jobId + ".groovy";

                    var blob = new Blob([result], {
                        type: "text/plain;charset=utf-8"
                    });

                    saveAs(blob, filename);
                },
                error: function () {
                    return ""
                }
            });
        }
    }
});