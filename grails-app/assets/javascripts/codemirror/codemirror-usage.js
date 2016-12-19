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
function CodemirrorEditor(data) {

    this.editor;
    this.NO_STEPS_FOUND = data.i18nMessage_NO_STEPS_FOUND;
    this.DANGLING_SETEVENTNAME_STATEMENT = data.i18nMessage_DANGLING_SETEVENTNAME_STATEMENT;
    this.MISSING_SETEVENTNAME_STATEMENT = data.i18nMessage_MISSING_SETEVENTNAME_STATEMENT;
    this.STEP_NOT_RECORDED = data.i18nMessage_STEP_NOT_RECORDED;
    this.linkParseScriptAction = data.linkParseScriptAction;
    this.linkMergeDefinedAndUsedPlaceholders = data.linkMergeDefinedAndUsedPlaceholders;
    this.linkGetScriptSource = data.linkGetScriptSource;
    this.linkCheckForNewPageOrMeasuredEventNames = data.linkCheckForNewPageOrMeasuredEventNames;
    this.markedLines;
    this.measuredEvents = data.measuredEvents;
    this.idCodemirrorElement = data.idCodemirrorElement;
    this.editorReadonly = data.readonly;
    this.parsedScriptUrl = data.parsedScriptUrl;
    this.clipboard;

    this.init = function(){

        this.editor = CodeMirror.fromTextArea(document.getElementById(this.idCodemirrorElement), {
            lineNumbers: true,
            extraKeys: { "Ctrl-Space": "autocomplete" },
            readOnly: this.editorReadonly,
            gutters: ['setEventName-warning-gutter'],
            lineWrapping: true
        });
        this.markedLines = [];
        //this.editor.setSize(900,300);

        var codemirrorEditor = this;
        this.editor.on('change', function(){
            codemirrorEditor.updateWarnings.call(codemirrorEditor)
        });
        $('#lineBreakToggle').click(function() {
            codemirrorEditor.editor.setOption('lineWrapping', $(this).prop('checked'));
            codemirrorEditor.editor.refresh();
        });
        // Create a Javascript array with all MeasuredEvents for auto complete
        CodeMirror.measuredEvents = []
        for (index = 0; index < this.measuredEvents.length; ++index) {
            CodeMirror.measuredEvents.push({
                text: this.measuredEvents[index].testedPage.name + ":::" + this.measuredEvents[index].name,
                displayText: this.measuredEvents[index].testedPage.name + ":::" + this.measuredEvents[index].name
            });
        }
        CodeMirror.measuredEvents.sort(function(a, b) {
            return a.displayText.localeCompare(b.displayText);
        });
        codemirrorEditor.editor.refresh();
    };

    this.warningMsgs = {
        NO_STEPS_FOUND: this.NO_STEPS_FOUND,
        STEP_NOT_RECORDED: this.STEP_NOT_RECORDED,
        DANGLING_SETEVENTNAME_STATEMENT: this.DANGLING_SETEVENTNAME_STATEMENT,
        MISSING_SETEVENTNAME_STATEMENT: this.MISSING_SETEVENTNAME_STATEMENT
    };

    this.update = function() {
        if (!this.editor) {
            return;
        }
        var codeMirrorEditor = this;
        $.ajax({
            type : 'POST',
            url : codeMirrorEditor.linkMergeDefinedAndUsedPlaceholders,
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
        if(this.clipboard != null) this.clipboard.destroy();
        $.ajax({
            type : 'GET',
            url : codeMirrorEditor.parsedScriptUrl,
            data: {
                'jobId': $('input#id').val(),
                'scriptId': $('#script').val()
            },
            success : function(result) {
                codeMirrorEditor.clipboard = new Clipboard("#copyToClipboard",{
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
            url : codeMirrorEditor.linkGetScriptSource,
            data: {
                'scriptId': $('#script').val()
            },
            success : function(result) {
                //var scriptToLoad = result;
                //if(typeof result != 'undefined') {scriptToLoad = result.innerHtml();}
                codeMirrorEditor.loadNewContent(result);
            },
            error : function(XMLHttpRequest, textStatus, errorThrown) {
                codeMirrorEditor.loadNewContent('Error');
            }
        });



    }

    this.updateWarnings = function() {
        var codeMirrorEditor = this;
        $.ajax({
            type: 'POST',
            url: codeMirrorEditor.linkParseScriptAction,
            data: { navigationScript: codeMirrorEditor.editor.getValue() },
            success: function (result) {
                codeMirrorEditor.clearGutterAndLines();

                for (var lineNumber in result.warnings) {
                    lineNumber = parseInt(lineNumber);
                    codeMirrorEditor.markLine(lineNumber, 'setEventName-warning-line');
                    var warningsForCurrentLine = result.warnings[lineNumber].map(function(warning) { return codeMirrorEditor.warningMsgs[warning.type.name]; }).join('</li><a>');
                    codeMirrorEditor.editor.setGutterMarker(lineNumber, 'setEventName-warning-gutter',
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
                            codeMirrorEditor.markLine(startLineNumber, 'eventBlock-oneline');
                        } else {
                            codeMirrorEditor.markLine(startLineNumber, 'eventBlock-top');
                            for (var j = startLineNumber + 1; j < endLineNumber; j++) {
                                codeMirrorEditor.markLine(j, 'eventBlock-middle');
                            }
                            codeMirrorEditor.markLine(endLineNumber, 'eventBlock-bottom');
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
                codeMirrorEditor.clearGutterAndLines();
            }
        });
    }

    this.loadNewContent = function(content) {
        this.clearGutterAndLines();
        this.editor.getDoc().setValue(content);
    }

    this.markLine = function(lineNumber, cssClass) {
        this.editor.addLineClass(lineNumber, 'background', cssClass);
        this.markedLines.push(lineNumber);
    }

    this.clearGutterAndLines = function() {
        this.editor.clearGutter('setEventName-warning-gutter');
        var editorInMethodScope = this.editor
        this.markedLines.map(function (lineNumber) {
            editorInMethodScope.removeLineClass(lineNumber, 'background', null);
        });
        this.markedLines = [];
    }
    this.checkForNewPageOrMeasuredEventNames = function(displayPrompt) {
        var codeMirrorEditor = this;

        $.ajax({
            type : 'GET',
            url : codeMirrorEditor.linkCheckForNewPageOrMeasuredEventNames,
            data: { navigationScript: codeMirrorEditor.editor.getValue() },
            success : function(result) {
                console.log(result)
                displayPrompt (JSON.parse(result));
            },
            error : function(XMLHttpRequest, textStatus, errorThrown) {
                return ""
            }
        });
    }

    this.init();

    this.updateWarnings();

}