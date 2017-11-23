//= require bower_components/vue/dist/vue.js
//= require bower_components/jquery/jquery.min.js
//= require bower_components/file-saver/FileSaver.min.js
//= require job/thresholdTabMeasuredEventVue.js
//= require job/thresholdTabMeasurandVue.js
//= require job/thresholdTabThresholdRowVue.js
//= require job/thresholdTabNewThresholdVue.js
//= require job/thresholdTabThresholdRowLabel.js
//= require job/thresholdTabThresholdRowInput.js

"use strict";

new Vue({
    el: '#threshold',
    data: {
        thresholds: [],
        measuredEvents: [],
        measuredEventCount: 0,
        measurands: [],
        jobId: "",
        scriptId: ""
    },
    computed: {
        availableMeasuredEvents: function () {
            var self = this;
            this.thresholds.forEach(function (threshold) {
                if(self.measuredEvents.indexOf(threshold.measuredEvent) !== -1) {
                    self.measuredEvents.splice(self.measuredEvents.indexOf(threshold.measuredEvent), 1)
                }
            });
            return this.measuredEvents;
        }
    },
    beforeMount: function () {
        this.jobId = this.$el.attributes['jobId'].value;
        this.scriptId = this.$el.attributes['scriptId'].value;
    },
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
                            edit: false,
                            saved: true
                        })
                    });
                    self.thresholds.push({
                        measuredEvent: self.measuredEvents.find(function (element) {
                            return element.id === resultEvent.measuredEvent.id;
                        }),
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
                        self.measuredEventCount = self.measuredEvents.length;
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
                    measurand: newThreshold.threshold.measurand.name,
                    measuredEvent: newThreshold.threshold.measuredEvent.id,
                    lowerBoundary: newThreshold.threshold.lowerBoundary,
                    upperBoundary: newThreshold.threshold.upperBoundary
                },
                url: "/threshold/createAsync",
                success: function (result) {
                    self.thresholds.forEach(function (measuredEventItem) {
                        if (measuredEventItem.measuredEvent.id === newThreshold.threshold.measuredEvent.id) {
                            //Add id to the threshold and set status to saved
                            var savedThreshold = measuredEventItem.thresholdList[measuredEventItem.thresholdList.indexOf(newThreshold)];
                            savedThreshold.threshold.id = result.thresholdId;
                            savedThreshold.saved = true;
                        }
                    });

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
                                self.measuredEvents.push(measuredEventItem.measuredEvent);
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
        addMeasuredEvent: function () {
            if(this.measuredEvents.length > 0 && this.thresholds.length < this.measuredEventCount) {
                this.thresholds.push({
                    measuredEvent: {},
                    thresholdList: [{
                        edit: false,
                        saved: false,
                        threshold: {
                            measuredEvent: {}
                        }
                    }]
                })
            }
        },
        removeMeasuredEvent: function(measuredEvent){
            if(Object.keys(measuredEvent.measuredEvent).length) {
                this.measuredEvents.push(measuredEvent.measuredEvent);
            }
            this.thresholds.splice(this.thresholds.indexOf(measuredEvent), 1)
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