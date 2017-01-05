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
            $("#archivedNavigationScriptColumn .CodeMirror").remove();
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
function saveVersionDescription(archivedScriptId,updateVersionDescriptionUrl) {
    var versionDescriptionTextAreaId = "#versionDescriptionTextArea-"+archivedScriptId;
    var newVersionDescription = $(versionDescriptionTextAreaId).val();
    jQuery.ajax({
        type: 'GET',
        url: updateVersionDescriptionUrl,
        data: {
            archivedScriptId: archivedScriptId,
            newVersionDescription: newVersionDescription
        },
        success: function (content) {
            hideSaveButton(archivedScriptId);
            $(versionDescriptionTextAreaId).prop("defaultValue",newVersionDescription);
        }
    });
}
function abortVersionDescription(archivedScriptId) {
    var versionDescriptionTextAreaId = "#versionDescriptionTextArea-"+archivedScriptId;
    $(versionDescriptionTextAreaId).val($(versionDescriptionTextAreaId).prop("defaultValue"));
    hideSaveButton(archivedScriptId);
}

function showSaveButton(archivedScriptId) {
    var saveIconId = "#versionDescriptionSaveButton-"+archivedScriptId;
    $(saveIconId).css('visibility', 'visible');
    var abortIconId = "#versionDescriptionAbortButton-"+archivedScriptId;
    $(abortIconId).css('visibility', 'visible');
}
function hideSaveButton(archivedScriptId) {
    var saveIconId = "#versionDescriptionSaveButton-"+archivedScriptId;
    $(saveIconId).css('visibility', 'hidden');
    var abortIconId = "#versionDescriptionAbortButton-"+archivedScriptId;
    $(abortIconId).css('visibility', 'hidden');
}

function initVersionControl(archivedScriptIds, getArchivedNavigationScriptUrl, updateVersionDescriptionUrl) {
    archivedScriptIds.forEach(function (archivedScriptId) {
        var rowId = "#archivedNavigationScriptTableRow-"+archivedScriptId;
        $(rowId).on("click", function () {
            showArchivedScriptById(archivedScriptId,getArchivedNavigationScriptUrl);
        } );
        var versionDescriptionTextAreaId = "#versionDescriptionTextArea-"+archivedScriptId;
        $('#versionControlModal').on('shown.bs.modal', function() {
            initAutoResize($(versionDescriptionTextAreaId)[0]);
        });
        $(versionDescriptionTextAreaId).keyup(function(){
            showSaveButton(archivedScriptId);
            resizeTextArea(this)
        });
        $(versionDescriptionTextAreaId).change(function(){
            showSaveButton(archivedScriptId);
            resizeTextArea(this)
        });
        var saveIconId = "#versionDescriptionSaveButton-"+archivedScriptId;
        $(saveIconId).on("click",function () {
            saveVersionDescription(archivedScriptId, updateVersionDescriptionUrl);
        });
        var abortIconId = "#versionDescriptionAbortButton-"+archivedScriptId;
        $(abortIconId).on("click",function () {
            abortVersionDescription(archivedScriptId);
            resizeTextArea($(versionDescriptionTextAreaId)[0])
        });
    });
    $('#versionControlModal').on('shown.bs.modal', function() {
        if(!$('#versionControlModal')[0].alreadyInitialized) {
            $('#archivedNavigationScriptTableBody').children('tr:first').click();
            $('#versionControlModal')[0].alreadyInitialized = true;
        }
    })
}
function initAutoResize(textAreaObject){
    if(!textAreaObject.alreadyInitialized) {
        var savedValue = textAreaObject.value;
        textAreaObject.value = '';
        textAreaObject.baseScrollHeight = textAreaObject.scrollHeight;
        textAreaObject.value = savedValue;
        textAreaObject.alreadyInitialized = true;

    }
    resizeTextArea(textAreaObject)
}
function resizeTextArea(textAreaObject){
    var minRows = textAreaObject.getAttribute('data-min-rows')|0, rows;
    textAreaObject.rows = minRows;
    rows = Math.ceil((textAreaObject.scrollHeight - textAreaObject.baseScrollHeight) / 17);
    textAreaObject.rows = minRows + rows;
}