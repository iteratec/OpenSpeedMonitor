
"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.thresholdMeasuredEventList = (function(){
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
        init: getMeasuredEvents
    }
})();