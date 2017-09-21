
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
                    this.thresholds = getThresholdsForJob("277");
                }
            }
        });
    };

    var getThresholdsForJob = function (jobId) {
        var targetUrl = "/job/getThresholdsForJob";

        if(jobId && targetUrl){
            $.ajax({
                type: 'GET',
                url: targetUrl,
                data: { jobId: jobId },
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