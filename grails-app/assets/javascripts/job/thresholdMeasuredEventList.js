
"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.thresholdMeasuredEventList = (function(){
    var initVueComponent = function () {
        new Vue({
            el: '#editor',
            data: {
                foo: '1',
                thresholds: null
            },
            computed: {
                compiledMarkdown: function () {
                    return this.foo.split( '' ).reverse( ).join( '' );
                }
            },
            created: function () {
                this.fetchData()
            },
            methods: {
                update: function (e) {
                    this.foo = e.target.value
                },

                fetchData: function () {
                    var self = this;
                     getThresholdsForJob(277).success(function(result) {
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

    initVueComponent();
    return{
        init: getMeasuredEvents
    }
})();