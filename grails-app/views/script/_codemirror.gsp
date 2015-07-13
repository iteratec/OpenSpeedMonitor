<%@ page import="org.springframework.web.util.HtmlUtils" %>
<asset:stylesheet src="codemirror/codemirrorManifest.css"/>
<asset:javascript src="codemirror/codemirrorManifest.js"/>
<asset:script type="text/javascript">
	var markedLines = [];
	
	function markLine(editor, lineNumber, cssClass) {
		editor.addLineClass(lineNumber, 'background', cssClass);
		markedLines.push(lineNumber);
	}
	
	function clearGutterAndLines(editor) {
		editor.clearGutter('setEventName-warning-gutter');
		markedLines.map(function (lineNumber) {
			editor.removeLineClass(lineNumber, 'background', null);
		});
		markedLines = [];
	}
	
	var warningMsgs = {
		NO_STEPS_FOUND: '${message(code: 'script.NO_STEPS_FOUND.warning')}',
		STEP_NOT_RECORDED: '${message(code: 'script.STEP_NOT_RECORDED.warning')}',
		DANGLING_SETEVENTNAME_STATEMENT: '${message(code: 'script.DANGLING_SETEVENTNAME_STATEMENT.warning')}',
		MISSING_SETEVENTNAME_STATEMENT: '${message(code: 'script.MISSING_SETEVENTNAME_STATEMENT.warning')}',
	};
	
	function updateWarnings(editor) {
		$.ajax({
			type: 'POST',
			url: '${createLink(controller: 'script', action: 'parseScript', absolute: true)}',
			data: { navigationScript: editor.getValue() },
			success: function (result) {
				clearGutterAndLines(editor);
				
				for (var lineNumber in result.warnings) {
					lineNumber = parseInt(lineNumber);
					markLine(editor, lineNumber, 'setEventName-warning-line');
					var warningsForCurrentLine = result.warnings[lineNumber].map(function(warning) { return warningMsgs[warning.type.name]; }).join('</li><a>');
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
							markLine(editor, startLineNumber, 'eventBlock-oneline');
						} else {
							markLine(editor, startLineNumber, 'eventBlock-top');
							for (var j = startLineNumber + 1; j < endLineNumber; j++) {
								markLine(editor, j, 'eventBlock-middle');
							}
							markLine(editor, endLineNumber, 'eventBlock-bottom');
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
				clearGutterAndLines(editor);
			}
		});
	}
	
	function createCodemirrorEditor() {
		// Initialize CodeMirror editor
		var editor = CodeMirror.fromTextArea(document.getElementById("navigationScript"), {
	        lineNumbers: true,
	        extraKeys: { "Ctrl-Space": "autocomplete" },
	        readOnly: ${readOnly},
	    	gutters: ['setEventName-warning-gutter'],
	    	lineWrapping: true
	    });
	    //editor.setSize(900,300);
		editor.on('change', function() {
			updateWarnings(editor);
		});
		updateWarnings(editor);
		
	    // Create a Javascript array with all MeasuredEvents for auto complete
		CodeMirror.measuredEvents = new Array(  
		<g:each var="measuredEvent" in="${measuredEvents}" status="i">
			{ text: '${measuredEvent.name}',
			  displayText: '${measuredEvent.name} (${measuredEvent.testedPage.name})' }
			<g:if test="${i < measuredEvents.size() - 1}">,</g:if> 
		</g:each>
		);
		
		$('#lineBreakToggle').click(function() {
			editor.setOption('lineWrapping', $(this).prop('checked'));
			editor.refresh(); 
		});
		
		return editor;
	}
	
	function loadNewContent(editor, content) {
		clearGutterAndLines(editor);
		editor.getDoc().setValue(content);
	}
	
	<g:if test="${autoload}">
	$(document).ready(function () {
		createCodemirrorEditor();
	}); 
	</g:if>
</asset:script>

<label for="navigationScript">
    <g:message code="script.navigationScript.label" default="Code" />
</label>
<textarea name="navigationScript" id="navigationScript">${code}</textarea>
<span id="setEventName-warning-clone" class="setEventName-warning-icon" style="display: none;" rel="tooltip" data-html="true"></span>
<p><input type="checkbox" id="lineBreakToggle" checked /> <label for="lineBreakToggle" style="display: inline"><g:message code="script.wrapLines.label" /></label></p>
<g:if test="${autoload}">
	<p id="usedVariables" 
		data-instructions="${HtmlUtils.htmlEscape(message(code: 'script.placeholdersInstructions.label'))}"
		data-usedvars="${HtmlUtils.htmlEscape(message(code: 'codemirror.usedVariables.label'))}"></p>
</g:if>	
