/**
 * Refreshes the table data every 5 seconds
 * @param updateTableUrl
 *          url to update complete table (method updateTableMethod within BatchActivityController)
 * @param checkUrl
 *          url to check if active BatchActivities exist (method checkForUpdate within BatchActivityController)
 * @param rowUpdateUrl
 *          url to update all rows (method getUpdate within BatchActivityController)
 */
function updateIfNecessary(updateTableUrl, checkUrl, rowUpdateUrl) {
    setInterval(function () {
        var ids = collectActiveIds();

        if (ids.length == 0 && isPageOne()) {
            jQuery.ajax({
                type: 'GET',
                url: checkUrl,
                success: function (content) {
                    if (content == "true") {
                        updateBatchActivityTable(updateTableUrl);
                    }
                }
            });
        } else {
            updateRows(ids,rowUpdateUrl);
        }
    }, 5000);
}

/**
 * updates the batchActivity table
 * @param updateTableUrl url to updateTableMethod within BatchActivityController
 */
function updateBatchActivityTable(updateTableUrl) {
    jQuery.ajax({
        type: 'GET',
        url: updateTableUrl,
        success: function (content) {
            $("#tabelle").html(content);
        },
        error: function (content) {
        }
    });
}

/**
 * Updates all rows with the given ids
 * @param ids row ids to update
 * @param rowUpdateUrl url to get a row update
 */
function updateRows(ids, rowUpdateUrl) {

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

}
/**
 * Checks if active page is page 1
 * @returns {boolean}
 */
function isPageOne() {
    var content = $(".active").text();
    return content == "1";
}
/**
 * Updates a single row with the given rowObject
 * @param rowObject([activity,endDate,htmlId,lastFailureMessage,lastUpdated,progress,startDate,status])
 */
function updateRow(rowObject) {
    var idxFieldActivity = 1;
    var idxFieldStatus = 2;
    var idxFieldProgress = 3;
    var idxFieldLastFailureMessage = 4;
    var idxFieldLastUpdated = 6;
    var idxFieldEndDate = 7;

    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldActivity+")").html(rowObject.activity);
    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldStatus+")").html(rowObject.status);
    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldProgress+")").html(rowObject.progress);
    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldLastFailureMessage+")").html(rowObject.lastFailureMessage);
    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldLastUpdated+")").html(rowObject.lastUpdated);
    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldEndDate+")").html(rowObject.endDate);
    $("tr#" + rowObject.htmlId).attr("status", rowObject.statusEN)
}
/**
 * Returns an array with all row ids where status = active
 * @returns {Array}
 */
function collectActiveIds() {
    var ids = [];
    $("[status='ACTIVE']").each(function (index, element) {
        ids.push([$(element).attr("id").replace("batchActivity_", "")]);
    });
    return ids;
}
