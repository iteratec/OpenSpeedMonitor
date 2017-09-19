
"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.thresholdMeasuredEventList = (function(){
    var init = function (data) {

        if(navigationScript && targetUrl){
            $.ajax({
                type: 'POST',
                url: data.targetUrl,
                data: { scriptId: data.scriptId },
                success : function(result) {
                    OpenSpeedMonitor.domUtils.updateSelectOptions($('.measured-event-select'), result, null);
                }
                ,
                error : function(XMLHttpRequest, textStatus, errorThrown) {
                    return ""
                }
            });
        }

    };

    return{
        init: init
    }
})();