//=require responsiveTable/responsiveTable.js

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.batchActivity = OpenSpeedMonitor.batchActivity || (function(responsiveTable) {

    /**
     * Refreshes the table data every 2 seconds
     * @param updateTableUrl
     *          url to update complete table (method updateTableMethod within BatchActivityController)
     * @param checkUrl
     *          url to check if active BatchActivities exist (method checkForUpdate within BatchActivityController)
     * @param rowUpdateUrl
     *          url to update all rows (method getUpdate within BatchActivityController)
     */
    var initBatchActivity= function(updateTableUrlParameter, checkUrl, rowUpdateUrl,batchActivityCount, i18nParameter, columnToSortByParameter) {
        $('#filterBatchesByActiveCheckbox').change(function () {
            responsiveTable.setOnlyActive($('#filterBatchesByActiveCheckbox').is(":checked"));
        });
        responsiveTable.init(updateTableUrlParameter, i18nParameter, columnToSortByParameter, $('#filterBatchesByActiveCheckbox').is(":checked"));
        updateIfNecessary(updateTableUrlParameter, checkUrl, rowUpdateUrl);
    };


    var updateIfNecessary= function(updateTableUrl, checkUrl, rowUpdateUrl) {
        setInterval(function () {
            var ids = collectActiveIds();
            jQuery.ajax({
                type: 'GET',
                url: checkUrl+"?activeCount="+ids.length,
                success: function (content) {
                    if (content == "true") {
                        responsiveTable.updateElementTable(updateTableUrl);
                    }else {
                        if(ids.length > 0)updateRows(ids,rowUpdateUrl);
                    }
                }
            });
        }, 2000);
    };

    /**
     * Updates all rows with the given ids
     * @param ids row ids to update
     * @param rowUpdateUrl url to get a row update
     */
    var updateRows= function(ids, rowUpdateUrl) {

        jQuery.ajax({
            type: 'GET',
            url: rowUpdateUrl,
            traditional: true,
            data: {activeIds: ids},
            async: false,
            success: function (content) {
                $.each(content, function(i, update){
                    updateRow(update);
                });
            },
            error: function (content) {
            }
        });

    };
    /**
     * Updates a single row with the given rowObject
     * @param rowObject([activity,endDate,htmlId,lastFailureMessage,lastUpdated,progress,startDate,status])
     */
    var updateRow= function(rowObject) {
        var idxFieldActivity = 1;
        var idxFieldStatus = 2;
        var idxFieldStage = 3;
        var idxFieldStageProgress = 4;
        var idxFieldLastFailureMessage = 5;
        var idxFieldLastUpdated = 7;
        var idxFieldEndDate = 8;
        var idxFieldRemainingTime = 9;


        $("tr#" + rowObject.htmlId + " td:eq("+idxFieldActivity+")").html(rowObject.activity);
        $("tr#" + rowObject.htmlId + " td:eq("+idxFieldStatus+")").html(rowObject.status);
        $("tr#" + rowObject.htmlId + " td:eq("+idxFieldStage+")").html(rowObject.stage);
        $("tr#" + rowObject.htmlId + " td:eq("+idxFieldStageProgress+")").html(rowObject.progress);
        $("tr#" + rowObject.htmlId + " td:eq("+idxFieldLastFailureMessage+")").html(rowObject.lastFailureMessage);
        $("tr#" + rowObject.htmlId + " td:eq("+idxFieldLastUpdated+")").html(rowObject.lastUpdate);
        $("tr#" + rowObject.htmlId + " td:eq("+idxFieldEndDate+")").html(rowObject.endDate);
        $("tr#" + rowObject.htmlId + " td:eq("+idxFieldRemainingTime+")").html(rowObject.remainingTime);
        $("tr#" + rowObject.htmlId).attr("status", rowObject.statusEN)
    };
    /**
     * Returns an array with all row ids where status = active
     * @returns {Array}
     */
    var collectActiveIds= function() {
        var ids = [];
        $("[status='ACTIVE']").each(function (index, element) {
            ids.push([$(element).attr("id").replace("batchActivity_", "")]);
        });
        return ids;
    };
    return{
        init:initBatchActivity
    }
})(OpenSpeedMonitor.responsiveTable || {});