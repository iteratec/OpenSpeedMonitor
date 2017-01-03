/**
 * Created by marko on 03.01.17.
 */

function showArchivedScriptById(scriptId,url) {
    $("input[name='archivedScriptId']").val(scriptId);
    jQuery.ajax({
        type: 'GET',
        url: url,
        data: {
            scriptId: scriptId
        },
        success: function (content) {
            var jsonResponse = JSON.parse(content);
            $("#archivedNavigationScriptColumn .CodeMirror").empty();
            $("#archivedNavigationScript").text(jsonResponse.navigationScript);
            createCodeMirror("archivedNavigationScript",true);
        }
    });
    highlightSelectedRow(scriptId)
}
function highlightSelectedRow(scriptId) {
    $(".highlight").removeClass('highlight');
    var rowId = "#archivedNavigationScriptTableRow-"+scriptId;
    $(rowId).toggleClass('highlight');

}
