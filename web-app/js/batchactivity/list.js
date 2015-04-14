/**
 * updates the batchActivity table
 * @param updateTableUrl url to updateTableMethod within BatchActivityController
 */
function updateBatchActivity(updateTableUrl) {
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
 * Refreshes the table data every 5 seconds
 * @param updateTableUrl url to updateTableMethod within BatchActivityController
 * @param checkUrl url to check if an update is necessary
 */
function updateIfNecessary(updateTableUrl, checkUrl,rowUpdateUrl) {
    setInterval(function () {
        var ids = collectActiveIds();

        if (ids.length == 0 && isPageOne()) {
            jQuery.ajax({
                type: 'GET',
                url: checkUrl,
                success: function (content) {
                    if (content == "true") {
                        updateBatchActivity(updateTableUrl);
                    }
                }
            });
        } else {
            replaceRows(ids,rowUpdateUrl);
        }
    }, 5000);
}

/**
 * Replaces all rows with the given id with a new version
 * @param ids row ids to update
 * @param rowUpdateUrl url to get a row update
 */
function replaceRows(ids,rowUpdateUrl) {
    for (var i in ids) {
        jQuery.ajax({
            type: 'GET',
            url: rowUpdateUrl,
            data: {id: ids[i][0],evenOdd:ids[i][1]},
            async: false,
            success: function (content) {
                //remove unnecessary html tags
                content = content.replace("<html>","").replace("<body>","").replace("</html>","").replace("</body>","");
                replaceRow(ids[i],content);
            },
            error: function (content) {
            }
        });
    }
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
 * replaces a single row with the given id with the content
 * @param id
 * @param content
 */
function replaceRow(id, content) {
    $("tr#batchActivity_" + id).replaceWith(content);
}
/**
 * Returns an array with all row ids where status = active
 * @returns {Array}
 */
function collectActiveIds() {
    var ids = [];
    $("[status='ACTIVE']").each(function (index, element) {
        ids.push([$(element).attr("id").replace("batchActivity_", ""),$(element).attr("class")]);
    });
    return ids;
}
