//= require bower_components/vue/dist/vue.js
//= require bower_components/jquery/jquery.min.js
//= require job/thresholdTabMeasuredEventVue.js
//= require job/thresholdTabMeasurandVue.js
//= require job/thresholdTabThresholdRowVue.js

"use strict";

new Vue({
    el: '#threshold',
    props:['jobId'],
    data: {
        thresholds: [],
        measuredEvents: [],
        measurands: [],
        newThreshold: {},
        tmpThreshold: {},
        newThresholdState: false
    },
    computed: {},
    created: function () {
        this.getMeasurands("/job/getMeasurands");
        this.getMeasuredEvents(148, "/script/getMeasuredEventsForScript");
        this.fetchData();
    },
    methods: {
        fetchData: function () {
            this.thresholds = [];
            var self = this;
            this.getThresholdsForJob(277).success(function (result) {
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
                    var added = false;
                    //Add threshold to measured event
                    self.thresholds.forEach(function (measuredEventItem) {
                        if (measuredEventItem.measuredEvent.id === self.newThreshold.measuredEvent.id) {
                            measuredEventItem.thresholdList.push({
                                threshold: self.newThreshold,
                                edit: false
                            });

                            added = true;
                        }
                    });

                    //Add measured event if it is not existing
                    if (!added) {
                        var list = [{
                            threshold: self.newThreshold,
                            edit: false
                        }]
                        self.thresholds.push({
                            measuredEvent: self.newThreshold.measuredEvent,
                            thresholdList: list
                        })
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
                            measuredEventItem.thresholdList.splice(measuredEventItem.thresholdList.indexOf(deletedThreshold), 1)

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
        }
    }
});