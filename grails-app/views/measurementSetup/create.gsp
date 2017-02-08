<%@ page defaultCodec="none" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.springframework.web.util.HtmlUtils" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.osm.setupMeasurementWizard.title" default="Setup Measurement"/></title>

    <asset:stylesheet src="script/scriptManifest.css"/>
    <asset:stylesheet src="setupWizard/setupWizard.css"/>
</head>

<body>
<h1><g:message code="de.iteratec.osm.setupMeasurementWizard.title" default="Setup Measurement"/></h1>

<p>
    %{--JOHANNES2DO: Write description for the wizard--}%
    Steht hier ein sinnvoller Text?
</p>

<div class="card" style="min-height: 700px;">

    %{-- realize breadcrumbs with bootstrap nav-tabs underneath --}%
    <ul id="measurementSetupSteps" class="nav nav-tabs nav-justified">
        <li class="wizardStep active">
            <a data-toggle="tab" href="#setJobGroup" id="setJobGroupTab">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.setJobGroup" default="Set Job Group"/>
            </a>
        </li>
        <li class="wizardStep">
            <a data-toggle="tab" href="#createScript" id="createScriptTab">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.createScript" default="Create Script"/>
            </a>
        </li>
        <li class="wizardStep">
            <a data-toggle="tab" href="#selectLocationAndConnectivity" id="selectLocationAndConnectivityTab">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.selectLocationAndConnectivity"
                           default="Select Location and Connectivity"/>
            </a>
        </li>
        <li class="wizardStep">
            <a data-toggle="tab" href="#createJob" id="createJobTab">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.createJob" default="Create Job"/>
            </a>
        </li>
    </ul>

    <form>

        <div class="tab-content">
            <div class="tab-pane active" id="setJobGroup">
                <g:render template="/measurementSetup/setJobGroup"/>
            </div>

            <div class="tab-pane" id="createScript">
                <g:render template="/measurementSetup/createScript"/>
            </div>

            <div class="tab-pane" id="selectLocationAndConnectivity">
                <g:render template="/measurementSetup/selectLocationAndConnectivity"/>
            </div>

            <div class="tab-pane" id="createJob">
                <g:render template="/measurementSetup/createJob"/>
            </div>

        </div> %{-- tab-content --}%

    </form>

</div> %{-- card --}%

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
                function presetScriptName() {
                    var jobGroubTabNextButton = document.querySelector("#setJobGroubTabNextButton");
                    var inputScriptName = document.querySelector("#inputScriptName");

                    jobGroubTabNextButton.addEventListener("click", function () {
                        // if the user changed the script name, don't set the value
                        if (inputScriptName.value == "")
                            inputScriptName.value = document.querySelector("#inputJobGroup").value;
                    });
                }

                function presetJobName() {
                    var jobGroubTabNextButton = document.querySelector("#selectLocationAndConnectivityTabNextButton");
                    var inputJobName = document.querySelector("#inputJobName");

                    jobGroubTabNextButton.addEventListener("click", function () {
                        // if the user changed the script name, don't set the value
                        if (inputJobName.value == "") {
                            var jobGroup = document.querySelector("#inputJobGroup").value;

                            var locationOptions = document.querySelector("#inputLocation").options;
                            var selectedLocationIndex = locationOptions.selectedIndex;
                            var location = locationOptions[selectedLocationIndex].text;

                            var connectivity = document.querySelector("#inputConnectivity").value;

                            inputJobName.value = jobGroup + "_" + location + "_" + connectivity;
                        }
                    });
                }

                function moveThroughWizard() {
                    document.querySelector("#setJobGroubTabNextButton").addEventListener("click", function () {
                        document.querySelector("#setJobGroupTab").parentElement.classList.toggle("active");
                        document.querySelector("#createScriptTab").parentElement.classList.toggle("active");
                    });
                    document.querySelector("#createScriptTabNextButton").addEventListener("click", function () {
                        document.querySelector("#createScriptTab").parentElement.classList.toggle("active");
                        document.querySelector("#selectLocationAndConnectivityTab").parentElement.classList.toggle("active");
                    });
                    document.querySelector("#selectLocationAndConnectivityTabNextButton").addEventListener("click", function () {
                        document.querySelector("#selectLocationAndConnectivityTab").parentElement.classList.toggle("active");
                        document.querySelector("#createJobTab").parentElement.classList.toggle("active");
                    });

                    document.querySelector("#createScriptTabPreviousButton").addEventListener("click", function () {
                        document.querySelector("#createScriptTab").parentElement.classList.toggle("active");
                        document.querySelector("#setJobGroupTab").parentElement.classList.toggle("active");
                    });
                    document.querySelector("#selectLocationAndConnectivityTabPreviousButton").addEventListener("click", function () {
                        document.querySelector("#selectLocationAndConnectivityTab").parentElement.classList.toggle("active");
                        document.querySelector("#createScriptTab").parentElement.classList.toggle("active");
                    });
                    document.querySelector("#createJobTabPreviousButton").addEventListener("click", function () {
                        document.querySelector("#createJobTab").parentElement.classList.toggle("active");
                        document.querySelector("#selectLocationAndConnectivityTab").parentElement.classList.toggle("active");
                    });
                }

                createCodeMirror("navigationScript", false);
                window.onload = function() {
                    OpenSpeedMonitor.script.versionControl.initVersionControl(${archivedScripts*.id},'${createLink(controller: 'script', action: 'getArchivedNavigationScript', absolute: true)}','${createLink(controller: 'script', action: 'updateVersionDescriptionUrl', absolute: true)}');
                    presetScriptName();
                    presetJobName();
                    moveThroughWizard();
                }
    </asset:script>
</content>
</body>
</html>