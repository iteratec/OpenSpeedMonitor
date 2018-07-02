//= require node_modules/jquery/jquery.min.js
//= require node_modules/file-saver/FileSaver.min.js
//= require job/threshold/measuredEventVue.js
//= require job/threshold/thresholdVue.js
//= require job/threshold/thresholdComponents/measurandSelectVue.js
//= require job/threshold/thresholdComponents/labelVue.js
//= require job/threshold/thresholdComponents/inputVue.js
//= require job/threshold/thresholdComponents/buttonVue.js
//= require job/threshold/thresholdComponents/confirmButtonVue.js

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.i18n = OpenSpeedMonitor.i18n || {};
OpenSpeedMonitor.i18n.measurands = OpenSpeedMonitor.i18n.measurands || {};

new Vue({
    el: '#threshold',
    data: {
        activeMeasuredEvents: [],
        measuredEvents: [],
        measurands: [],
        jobId: "",
        scriptId: "",
        copiedMeasuredEvents: []
    },
    computed: {                                                 /*TODO ?*/
        availableMeasuredEvents: function () {
            var self = this;
            self.activeMeasuredEvents.forEach(function (threshold) {
                if (threshold.measuredEvent) {
                    var compareTo = threshold.measuredEvent;
                    self.copiedMeasuredEvents = self.copiedMeasuredEvents.filter(function (element) {
                        return element.id !== compareTo.id;
                    })
                }
            });
            return this.copiedMeasuredEvents;
        }
    },
    beforeMount: function () {                                      /*GUACAMOLE*/
        this.jobId = this.$el.attributes['jobId'].value;
        this.scriptId = this.$el.attributes['scriptId'].value;
    },
    mounted: function () {                                          /*GUACAMOLE*/
        this.fetchData();
    },
    methods: {
        fetchData: function () {                                     /*GUACAMOLE*/
            this.getMeasurands();
            this.getMeasuredEvents(this.scriptId);
        },
        getMeasuredEvents: function (scriptId) {                    /*GUACAMOLE*/
            var self = this;
            if (scriptId) {
                $.ajax({
                    type: 'GET',
                    url: "/script/getMeasuredEventsForScript",
                    data: {scriptId: scriptId},
                    success: function (result) {
                        self.measuredEvents = result;
                        self.copiedMeasuredEvents = self.measuredEvents.slice();
                        self.measuredEventCount = self.measuredEvents.length;
                        self.getThresholds();
                    },
                    error: function () {
                        return ""
                    }
                });
            }
        },
        getMeasurands: function () {                                        /*GUACAMOLE*/
            var self = this;
            $.ajax({
                type: 'GET',
                url: "/job/getAllMeasurands",
                data: {},
                success: function (result) {
                    result.forEach(function (measurand) {
                        measurand.translatedName = OpenSpeedMonitor.i18n.measurands[measurand.name];    //TODO
                    });
                    /*console.log("VUE Measurands result: " + JSON.stringify(result));*/
                    self.measurands = result;
                },
                error: function () {
                    return ""
                }
            });
        },
        getThresholds: function () {                        /*GUACAMOLE*/
            this.activeMeasuredEvents = [];
            var self = this;
            this.getThresholdsForJob(this.jobId).success(function (result) {
                result.forEach(function (resultEvent) {
                    var thresholdsForEvent = [];
                    resultEvent.thresholds.forEach(function (threshold) {
                        thresholdsForEvent.push({
                            threshold: threshold,
                            edit: false,
                            saved: true
                        })
                    });
                    self.activeMeasuredEvents.push({
                        measuredEvent: self.measuredEvents.find(function (element) {
                            return element.id === resultEvent.measuredEvent.id;
                        }),
                        thresholdList: thresholdsForEvent
                    })
                });
                /*console.log("VUE activeMeasuredEvents : " + JSON.stringify(self.activeMeasuredEvents));*/


            }).error(function (e) {
                console.log(e);
            });
        },
        createThreshold: function (newThreshold) {
            var self = this;
            $.ajax({
                type: 'POST',
                data: {
                    job: this.jobId,
                    measurand: newThreshold.threshold.measurand.name,
                    measuredEvent: newThreshold.threshold.measuredEvent.id,
                    lowerBoundary: newThreshold.threshold.lowerBoundary,
                    upperBoundary: newThreshold.threshold.upperBoundary
                },
                url: "/threshold/createThreshold",
                success: function (result) {
                    self.activeMeasuredEvents.forEach(function (measuredEventItem) {
                        if (measuredEventItem.measuredEvent.id === newThreshold.threshold.measuredEvent.id) {
                            //Add id to the threshold and set status to saved
                            var savedThreshold = measuredEventItem.thresholdList[measuredEventItem.thresholdList.indexOf(newThreshold)];

                            savedThreshold.threshold.id = result.thresholdId;
                            savedThreshold.saved = true;
                            savedThreshold.edit = false;
                        }
                    });
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
                url: "/threshold/deleteThreshold",
                success: function () {
                    self.activeMeasuredEvents.forEach(function (measuredEventItem) {
                        //remove threshold from measured event
                        if (measuredEventItem.measuredEvent.id === deletedThreshold.threshold.measuredEvent.id) {
                            measuredEventItem.thresholdList.splice(measuredEventItem.thresholdList.indexOf(deletedThreshold), 1);

                            //remove measured event
                            if (measuredEventItem.thresholdList.length === 0) {
                                self.removeMeasuredEvent(measuredEventItem);
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
                url: "/threshold/updateThreshold",
                success: function () {
                    self.activeMeasuredEvents.forEach(function (measuredEventItem) {
                        if (measuredEventItem.measuredEvent.id === updatedThreshold.threshold.measuredEvent.id) {
                            updatedThreshold.edit = false;
                            measuredEventItem.thresholdList[measuredEventItem.thresholdList.indexOf(updatedThreshold)] = updatedThreshold;
                        }
                    });
                },
                error: function (e) {
                    console.log(e);
                }
            });
        },
        getThresholdsForJob: function (jobId) {             /*GUACAMOLE*/
            var targetUrl = "/job/getThresholdsForJob";
            return $.ajax({
                type: 'GET',
                url: targetUrl,
                data: {jobId: jobId}
            });
        },
        addMeasuredEvent: function () {
            if (this.availableMeasuredEvents.length > 0 &&
                this.activeMeasuredEvents.length < this.measuredEvents.length) {
                this.activeMeasuredEvents.push({
                    measuredEvent: {},
                    thresholdList: [{
                        edit: true,
                        saved: false,
                        threshold: {
                            measuredEvent: {},
                            measurand: this.measurands[0],
                            lowerBoundary: 0,
                            upperBoundary: 0
                        }
                    }]
                })
            }
        },
        removeMeasuredEvent: function (measuredEvent) {
            if (Object.keys(measuredEvent.measuredEvent).length) {
                this.copiedMeasuredEvents.push(measuredEvent.measuredEvent);
            }

            var compareTo = measuredEvent;
            this.activeMeasuredEvents = this.activeMeasuredEvents.filter(function (element) {
                return element.measuredEvent.id !== compareTo.measuredEvent.id;
            });
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
                    var filename = "CI_Script_" + self.jobId + ".groovy";

                    var blob = new Blob(
                        [result],
                        {type: "text/plain;charset=utf-8"});

                    saveAs(blob, filename);
                },
                error: function () {
                    return ""
                }
            });
        }
    }
});