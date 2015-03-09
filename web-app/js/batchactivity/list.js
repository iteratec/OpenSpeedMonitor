/**
 * Refreshes the table data every 5 seconds
 * @param updateTableUrl url to updateTableMethod within BatchActivityController
 */
function updateBatchActivity(updateTableUrl){
    setInterval(function () {
        jQuery.ajax({
            type : 'GET',
            url : updateTableUrl,
            success: function(content) {
                $("#tabelle").html(content);
            },
            error: function(content) {
                alert(JSON.stringify(content));
            }});
    }, 5000);
}