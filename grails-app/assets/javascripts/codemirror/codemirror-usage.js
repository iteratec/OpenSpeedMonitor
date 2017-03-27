/*
 * OpenSpeedMonitor (OSM)
 * Copyright 2014 iteratec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.script = OpenSpeedMonitor.script ||{};
OpenSpeedMonitor.script.codemirrorEditor = OpenSpeedMonitor.script.codemirrorEditor || (function(){

    var NO_STEPS_FOUND;
    var DANGLING_SETEVENTNAME_STATEMENT;
    var MISSING_SETEVENTNAME_STATEMENT;
    var STEP_NOT_RECORDED;
    var WRONG_PAGE;
    var WRONG_URL_FORMAT;
    var MEASUREDEVENT_NOT_UNIQUE;
    var VARIABLE_NOT_SUPPORTED;
    var TOO_MANY_SEPARATORS;
    var linkParseScriptAction;
    var linkMergeDefinedAndUsedPlaceholders;
    var linkGetScriptSource;
    var markedLines;
    var measuredEvents;
    var idCodemirrorElement;
    var editorReadonly;
    var parsedScriptUrl;
    var clipboard;
    var editor;
    var errors;


    var init = function(data){
        NO_STEPS_FOUND = data.i18nMessage_NO_STEPS_FOUND;
        DANGLING_SETEVENTNAME_STATEMENT = data.i18nMessage_DANGLING_SETEVENTNAME_STATEMENT;
        MISSING_SETEVENTNAME_STATEMENT = data.i18nMessage_MISSING_SETEVENTNAME_STATEMENT;
        STEP_NOT_RECORDED = data.i18nMessage_STEP_NOT_RECORDED;
        WRONG_PAGE = data.i18nMessage_WRONG_PAGE;
        WRONG_URL_FORMAT = data.i18nMessage_WRONG_URL_FORMAT;
        MEASUREDEVENT_NOT_UNIQUE = data.i18nMessage_MEASUREDEVENT_NOT_UNIQUE;
        VARIABLE_NOT_SUPPORTED = data.i18nMessage_VARIABLE_NOT_SUPPORTED;
        TOO_MANY_SEPARATORS = data.i18nMessage_TOO_MANY_SEPARATORS;
        linkParseScriptAction = data.linkParseScriptAction;
        linkMergeDefinedAndUsedPlaceholders = data.linkMergeDefinedAndUsedPlaceholders;
        linkGetScriptSource = data.linkGetScriptSource;
        measuredEvents = data.measuredEvents;
        idCodemirrorElement = data.idCodemirrorElement;
        editorReadonly = data.readonly;
        parsedScriptUrl = data.parsedScriptUrl;

        editor = CodeMirror.fromTextArea(document.getElementById(idCodemirrorElement), {
            lineNumbers: true,
            extraKeys: { "Ctrl-Space": "autocomplete" },
            readOnly: editorReadonly,
            gutters: ['setEventName-warning-gutter'],
            lineWrapping: true
        });
        markedLines = [];
        //this.editor.setSize(900,300);

        editor.on('change', function(){
            updateWarnings.call()
        });
        $('#lineBreakToggle').click(function() {
            editor.setOption('lineWrapping', $(this).prop('checked'));
            editor.refresh();
        });
        // Create a Javascript array with all MeasuredEvents for auto complete
        CodeMirror.measuredEvents = [];
        for (index = 0; index < measuredEvents.length; ++index) {
            CodeMirror.measuredEvents.push({
                text: measuredEvents[index].testedPage.name + ":::" + measuredEvents[index].name,
                displayText: measuredEvents[index].testedPage.name + ":::" + measuredEvents[index].name
            });
        }
        CodeMirror.measuredEvents.sort(function(a, b) {
            return a.displayText.localeCompare(b.displayText);
        });
        editor.refresh();
        updateWarnings();
        return this;

    };

    var warningMsgs = function (key) {
        switch(key) {
            case "NO_STEPS_FOUND" :                     return NO_STEPS_FOUND;
            case "STEP_NOT_RECORDED" :                  return STEP_NOT_RECORDED;
            case "DANGLING_SETEVENTNAME_STATEMENT" :    return DANGLING_SETEVENTNAME_STATEMENT;
            case "MISSING_SETEVENTNAME_STATEMENT" :     return MISSING_SETEVENTNAME_STATEMENT;
            case "WRONG_PAGE" :                         return WRONG_PAGE;
            case "WRONG_URL_FORMAT" :                   return WRONG_URL_FORMAT;
            case "TOO_MANY_SEPARATORS" :                return TOO_MANY_SEPARATORS;
            case "MEASUREDEVENT_NOT_UNIQUE" :           return MEASUREDEVENT_NOT_UNIQUE;
            case "VARIABLE_NOT_SUPPORTED" :             return VARIABLE_NOT_SUPPORTED;
        }
    };
    var update = function() {
        if (!editor) {
            return;
        }
        $.ajax({
            type : 'POST',
            url : linkMergeDefinedAndUsedPlaceholders,
            data: {
                'jobId': $('input#id').val(),
                'scriptId': $('#script').val()
            },
            success : function(result) {
                $("#placeholderCandidates").html(result);
            },
            error : function(XMLHttpRequest, textStatus, errorThrown) {
                $("#placeholderCandidates").html('');
            }
        });
        if(clipboard != null) clipboard.destroy();
        $.ajax({
            type : 'GET',
            url : parsedScriptUrl,
            data: {
                'jobId': $('input#id').val(),
                'scriptId': $('#script').val()
            },
            success : function(result) {
                clipboard = new Clipboard("#copyToClipboard",{
                    text :function (trigger) {
                        return result
                    }
                });
            },
            error : function(XMLHttpRequest, textStatus, errorThrown) {
                return ""
            }
        });

        $.ajax({
            type : 'POST',
            url : linkGetScriptSource,
            data: {
                'scriptId': $('#script').val()
            },
            success : function(result) {
                setNewContent(result);
            },
            error : function(XMLHttpRequest, textStatus, errorThrown) {
                setNewContent('Error');
            }
        });



    };

    var clearNewPagesOrNewMeasuredEventsInfo= function () {
        $("#newPages").empty();
        $("#newMeasuredEvents").empty();
        $("#newPageOrMeasuredEventInfo").hide();
        $("#newPagesContainer").hide();
        $("#newMeasuredEventsContainer").hide();
        $('#saveButton').prop('disabled', false);
        $('#saveCopyButton').prop('disabled', false);
        $('.tooltip.fade.right.in').remove();
    };

    var appendElementsFromListToDiv =  function (elementList,containerDiv,divToAppendTo) {
        var newElementsString = "";
        var arrayLength = elementList.length;
        for (var i = 0; i < arrayLength; i++) {
            $("#newPageOrMeasuredEventInfo").show();
            $(containerDiv).show();
            if (i >= 1) {
                newElementsString += ", ";
            }
            newElementsString += elementList[i]
        }
        jQuery('<span/>', {
            text: newElementsString
        }).appendTo(divToAppendTo);
    };

    var updateWarnings = function() {
        $.ajax({
            type: 'POST',
            url: linkParseScriptAction,
            data: { navigationScript: editor.getValue() },
            success: function (result) {
                clearGutterAndLines();
                clearNewPagesOrNewMeasuredEventsInfo();
                appendElementsFromListToDiv(result.newPages,"#newPagesContainer","#newPages");
                appendElementsFromListToDiv(result.newMeasuredEvents,"#newMeasuredEventsContainer","#newMeasuredEvents");
                for (var lineNumber in result.warnings) {
                    lineNumber = parseInt(lineNumber);
                    markLine(lineNumber, 'setEventName-warning-line');
                    var warningsForCurrentLine = result.warnings[lineNumber].map(function(warning) { return warningMsgs(warning.type.name); }).join('</li><li>');
                    editor.setGutterMarker(lineNumber, 'setEventName-warning-gutter',
                        $('#setEventName-warning-clone').clone()
                            .attr('id', '')
                            .attr('title', '<ul><li>' + warningsForCurrentLine + '</li></ul>')
                            .css('display', 'block')[0]);
                }
                errors = result.errors;
                for (var lineNumber in result.errors) {
                    $('#saveButton').prop('disabled', true);
                    $('#saveCopyButton').prop('disabled', true);
                    lineNumber = parseInt(lineNumber);
                    markLine(lineNumber, 'setEventName-error-line');
                    var warningsForCurrentLine = result.errors[lineNumber].map(
                        function(error) {
                            var returnValue =  warningMsgs(error.type.name);
                            if (error.type.name == "WRONG_PAGE") {
                                var re = new RegExp("{page}", 'g');
                                returnValue = returnValue.replace(re, result.correctPageName[lineNumber][0].correctPageName);
                            }
                            return returnValue;
                        }).join('</li><li>');
                    editor.setGutterMarker(lineNumber, 'setEventName-warning-gutter',
                        $('#setEventName-warning-clone').clone()
                            .attr('id', '')
                            .attr('title', '<ul><li>' + warningsForCurrentLine + '</li></ul>')
                            .css('display', 'block')[0]);
                }
                $('.setEventName-warning-icon').tooltip({ container: 'body', placement: 'right' });
                if (result.steps) {
                    for (var i = 0; i < result.steps.length; i = i + 2) {
                        var startLineNumber = result.steps[i];
                        var endLineNumber = result.steps[i + 1];
                        if (startLineNumber == endLineNumber) {
                            markLine(startLineNumber, 'eventBlock-oneline');
                        } else {
                            markLine(startLineNumber, 'eventBlock-top');
                            for (var j = startLineNumber + 1; j < endLineNumber; j++) {
                                markLine(j, 'eventBlock-middle');
                            }
                            markLine(endLineNumber, 'eventBlock-bottom');
                        }
                    }
                }
                if ($('#usedVariables').length > 0) {
                    if (result.variables.length == 0) {
                        $('#usedVariables').html($('#usedVariables').attr('data-instructions'));
                    } else {
                        $('#usedVariables').html($('#usedVariables').attr('data-usedvars') + ' ' + result.variables.map(function(variable) {
                                return '<span>$' + '{' + variable + '}</span>';
                            }).join(', '));
                    }
                }
            },
            error: function () {
                clearGutterAndLines();
            }
        });
    };

    var setNewContent = function(content) {
        clearGutterAndLines();
        editor.getDoc().setValue(content);
    };

    var markLine = function(lineNumber, cssClass) {
        editor.removeLineClass(lineNumber,'background','setEventName-warning-line');
        editor.addLineClass(lineNumber, 'background', cssClass);
        markedLines.push(lineNumber);
    };

    var clearGutterAndLines = function() {
        editor.clearGutter('setEventName-warning-gutter');
        var editorInMethodScope = editor;
        markedLines.map(function (lineNumber) {
            editorInMethodScope.removeLineClass(lineNumber, 'background', null);
        });
        markedLines = [];
    };

    var getErrors = function () {
        return errors;
    }

    var getContent = function () {
        return editor.getDoc().getValue();
    }

    return {
        init:init,
        update:update,
        setNewContent: setNewContent,
        getErrors: getErrors,
        getContent: getContent
    };


})();
