/**
 * Created by marko on 03.01.17.
 */

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.script = OpenSpeedMonitor.script ||{};
OpenSpeedMonitor.script.versionControl = OpenSpeedMonitor.script.versionControl || (function(){
    var _showArchivedScriptById= function (scriptId, url) {
        $("input[name='archivedScriptId']").val(scriptId);
        jQuery.ajax({
            type: 'GET',
            url: url,
            data: {
                scriptId: scriptId
            },
            success: function (jsonResponse) {
                $("#archivedNavigationScriptColumn .CodeMirror").remove();
                $("#archivedNavigationScript").text(jsonResponse.navigationScript);
                createCodeMirror("archivedNavigationScript", true);
            }
        });
        _highlightSelectedRow(scriptId)
    };
    var _highlightSelectedRow= function (scriptId) {
        $(".highlight").removeClass('highlight');
        var rowId = "#archivedNavigationScriptTableRow-" + scriptId;
        $(rowId).toggleClass('highlight');

    };
    var _saveVersionDescription= function (archivedScriptId, updateVersionDescriptionUrl) {
        var versionDescriptionTextAreaId = "#versionDescriptionTextArea-" + archivedScriptId;
        var newVersionDescription = $(versionDescriptionTextAreaId).val();
        jQuery.ajax({
            type: 'GET',
            url: updateVersionDescriptionUrl,
            data: {
                archivedScriptId: archivedScriptId,
                newVersionDescription: newVersionDescription
            },
            success: function (content) {
                _hideSaveButton(archivedScriptId);
                $(versionDescriptionTextAreaId).prop("defaultValue", newVersionDescription);
            }
        });
    };
    var _abortVersionDescription= function (archivedScriptId) {
        var versionDescriptionTextAreaId = "#versionDescriptionTextArea-" + archivedScriptId;
        $(versionDescriptionTextAreaId).val($(versionDescriptionTextAreaId).prop("defaultValue"));
        _hideSaveButton(archivedScriptId);
    };

    var _showSaveButton= function (archivedScriptId) {
        var saveIconId = "#versionDescriptionSaveButton-" + archivedScriptId;
        $(saveIconId).css('visibility', 'visible');
        var abortIconId = "#versionDescriptionAbortButton-" + archivedScriptId;
        $(abortIconId).css('visibility', 'visible');
    };
    var _hideSaveButton = function (archivedScriptId) {
        var saveIconId = "#versionDescriptionSaveButton-" + archivedScriptId;
        $(saveIconId).css('visibility', 'hidden');
        var abortIconId = "#versionDescriptionAbortButton-" + archivedScriptId;
        $(abortIconId).css('visibility', 'hidden');
    };
    var _initAutoResize = function (textAreaObject) {
        if (!textAreaObject.alreadyInitialized) {
            var savedValue = textAreaObject.value;
            textAreaObject.value = '';
            textAreaObject.baseScrollHeight = textAreaObject.scrollHeight;
            textAreaObject.value = savedValue;
            textAreaObject.alreadyInitialized = true;

        }
        _resizeTextArea(textAreaObject)
    };
    var _resizeTextArea = function (textAreaObject) {
            var minRows = textAreaObject.getAttribute('data-min-rows') | 0, rows;
            textAreaObject.rows = minRows;
            rows = Math.ceil((textAreaObject.scrollHeight - textAreaObject.baseScrollHeight) / 17);
            textAreaObject.rows = minRows + rows;
    };

    var initVersionControl = function (archivedScriptIds, getArchivedNavigationScriptUrl, updateVersionDescriptionUrl) {
        archivedScriptIds.forEach(function (archivedScriptId) {
            var rowId = "#archivedNavigationScriptTableRow-" + archivedScriptId;
            $(rowId).on("click", function () {
                _showArchivedScriptById(archivedScriptId, getArchivedNavigationScriptUrl);
            });
            var versionDescriptionTextAreaId = "#versionDescriptionTextArea-" + archivedScriptId;
            $('#versionControlModal').on('shown.bs.modal', function () {
                _initAutoResize($(versionDescriptionTextAreaId)[0]);
            });
            $(versionDescriptionTextAreaId).keyup(function () {
                _showSaveButton(archivedScriptId);
                _resizeTextArea(this)
            });
            $(versionDescriptionTextAreaId).change(function () {
                _showSaveButton(archivedScriptId);
                _resizeTextArea(this)
            });
            var saveIconId = "#versionDescriptionSaveButton-" + archivedScriptId;
            $(saveIconId).on("click", function () {
                _saveVersionDescription(archivedScriptId, updateVersionDescriptionUrl);
            });
            var abortIconId = "#versionDescriptionAbortButton-" + archivedScriptId;
            $(abortIconId).on("click", function () {
                _abortVersionDescription(archivedScriptId);
                _resizeTextArea($(versionDescriptionTextAreaId)[0])
            });
        });
        $('#versionControlModal').on('shown.bs.modal', function () {
            if (!$('#versionControlModal')[0].alreadyInitialized) {
                $('#archivedNavigationScriptTableBody').children('tr:first').click();
                $('#versionControlModal')[0].alreadyInitialized = true;
            }
        })
    };
    return {
        initVersionControl:initVersionControl
    }
})();
