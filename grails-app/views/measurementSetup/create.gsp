<%@ page defaultCodec="none" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.springframework.web.util.HtmlUtils" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.osm.setupMeasurement" default="Setup Measurement"/></title>

    <asset:stylesheet src="script/scriptManifest.css"/>
</head>

<body>
<h1><g:message code="de.iteratec.osm.setupMeasurement" default="Setup Measurement"/></h1>

<p>
    Steht hier ein sinnvoller Text?
</p>

<div class="row">

    <div class="col-md-12">

        <div class="card">

            %{-- realize breadcrumbs with nav-tabs --}%
            <ul id="measurementSetupSteps" class="nav nav-tabs nav-justified">
                <li class="active">
                    <a data-toggle="tab" href="#createJobGroup">
                        Step 1
                    </a>
                </li>
                <li>
                    <a data-toggle="tab" href="#createScript">
                        Step 2
                    </a>
                </li>
                <li>
                    <a data-toggle="tab" href="#createJob">
                        Step 3
                    </a>
                </li>
            </ul>

            <form>

                <div class="tab-content">
                    <div class="tab-pane active" id="setJobGroup">
                        <g:render template="/measurementSetup/setJobGroup" />
                    </div>
                    <div class="tab-pane" id="createScript">
                        <g:render template="/measurementSetup/createScript" />
                    </div>
                    <div class="tab-pane" id="selectLocationAndConnectivity">
                        <g:render template="/measurementSetup/selectLocationAndConnectivity" />
                    </div>
                    <div class="tab-pane" id="createJob">
                        <g:render template="/measurementSetup/createJob" />
                    </div>

                </div> %{-- tab-content --}%

                <div>
                    <button class="btn btn-default" type="reset">
                        <i class="fa fa-times" aria-hidden="true"></i>
                        <g:message code="script.versionControl.cancel.button" default="Cancel"/>
                    </button>
                </div>
            </form>

        </div> %{-- card --}%

    </div> %{-- col-md-12 --}%

</div> %{-- row --}%

<content tag="include.bottom">
    <asset:javascript src="codemirror/codemirrorManifest.js"/>
    <asset:javascript src="prettycron/prettycronManifest.js"/>
    <asset:javascript src="script/versionControl.js"/>
    <asset:script type="text/javascript">
        function createCodeMirror(idCodemirrorElement, readonly){
            OpenSpeedMonitor.script.codemirrorEditor.init({
                idCodemirrorElement: idCodemirrorElement,
                i18nMessage_NO_STEPS_FOUND: '${message(code: 'script.NO_STEPS_FOUND.warning')}',
                        i18nMessage_STEP_NOT_RECORDED: '${message(code: 'script.STEP_NOT_RECORDED.warning')}',
                        i18nMessage_DANGLING_SETEVENTNAME_STATEMENT: '${message(code: 'script.DANGLING_SETEVENTNAME_STATEMENT.warning')}',
                        i18nMessage_MISSING_SETEVENTNAME_STATEMENT: '${message(code: 'script.MISSING_SETEVENTNAME_STATEMENT.warning')}',
                        i18nMessage_WRONG_PAGE: '${message(code: 'script.WRONG_PAGE.error')}',
                        i18nMessage_TOO_MANY_SEPARATORS: '${message(code: 'script.TOO_MANY_SEPARATORS.error')}',
                        i18nMessage_MEASUREDEVENT_NOT_UNIQUE: '${message(code: 'script.MEASUREDEVENT_NOT_UNIQUE.error')}',
                        i18nMessage_VARIABLE_NOT_SUPPORTED: '${message(code: 'script.VARIABLE_NOT_SUPPORTED.error')}',
                        i18nMessage_WRONG_URL_FORMAT: '${message(code: 'script.WRONG_URL_FORMAT.error')}',
                        measuredEvents: ${measuredEvents},
                        linkParseScriptAction: '${createLink(controller: 'script', action: 'parseScript', absolute: true)}',
                        linkMergeDefinedAndUsedPlaceholders: '${createLink(action: 'mergeDefinedAndUsedPlaceholders', absolute: true)}',
                        linkGetScriptSource: '${createLink(action: 'getScriptSource', absolute: true)}',
                        readonly: readonly
                    });
                }
                function promptForDuplicateName() {

                    var newName = prompt(
                            OpenSpeedMonitor.i18n.duplicatePrompt,
                            $('input#label').val() + OpenSpeedMonitor.i18n.duplicateSuffix
                    );
                    if (newName != null && newName != '') {
                        $('input#label').val(newName);
                        return true;
                    } else {
                        return false;
                    }
                }
                createCodeMirror("navigationScript", false);
                window.onload = function() {
                    OpenSpeedMonitor.script.versionControl.initVersionControl(${archivedScripts*.id},'${createLink(controller: 'script', action: 'getArchivedNavigationScript', absolute: true)}','${createLink(controller: 'script', action: 'updateVersionDescriptionUrl', absolute: true)}');
                }
    </asset:script>
</content>
</body>
</html>