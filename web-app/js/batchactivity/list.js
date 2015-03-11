/**
 * Refreshes the table data every 5 seconds
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

function updateIfNecessary(updateTableUrl, checkUrl) {
    setInterval(function () {
        var check = "false";
        jQuery.ajax({
            type: 'GET',
            url: checkUrl,
            async: false,
            success: function (content) {
                check = content;
                console.log(check);
            },
            error: function (content) {
                check = "false";
            }
        });
        if (check == "true") {
            console.log("aye");
            updateBatchActivity(updateTableUrl);
        }

    }, 5000);
}