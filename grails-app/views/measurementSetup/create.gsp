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
    <g:message code="de.iteratec.osm.setupMeasurementWizard.description" default="A Wizard for creating a new Measurement"
               encodeAs="raw" args="${[
            link(controller: 'job', action: 'list') { message(code:'de.iteratec.isj.jobs', default:'jobs')},
    ]}"/>
</p>

<div class="row">
    <div class="col-md-12">
        <g:if test="${errors || customError}">
            <div class="alert alert-danger">
                <strong><g:message code="de.iteratec.osm.measurement.setup.errorsTitle"/></strong>
                <ul>
                    <g:each var="currentError" in="${errors}">
                        <li><g:message error="${currentError}"/></li>
                    </g:each>
                <g:if test="${customError}">
                    <li><g:message code="${customError}"/></li>
                </g:if>
                </ul>
            </div>
        </g:if>
    </div>
</div>

<div class="card">
    <div class="progress" id="setupWizardProgressBarContainer">
        <div class="progress-bar" id="setupWizardProgressBar" role="progressbar"></div>
    </div>

    %{-- realize breadcrumbs with bootstrap nav-tabs underneath --}%
    <ul id="measurementSetupSteps" class="nav nav-tabs nav-justified">
        <li class="wizardStep active wasActive">
            <a data-toggle="tab" href="#setJobGroup" id="setJobGroupTab">
                <i class="fa fa-folder-open-o fa-2x" aria-hidden="true"></i>
                <g:message code="de.iteratec.osm.setupMeasurementWizard.setJobGroup" default="Set Job Group"/>
            </a>
        </li>
        <li class="wizardStep">
            <a data-toggle="tab" href="#createScript" id="createScriptTab">
                <i class="fa fa-file-text-o fa-2x" aria-hidden="true"></i>
                <g:message code="de.iteratec.osm.setupMeasurementWizard.createScript" default="Create Script"/>
            </a>
        </li>
        <li class="wizardStep">
            <a data-toggle="tab" href="#selectLocationAndConnectivity" id="selectLocationAndConnectivityTab">
                <i class="fa fa-signal fa-2x" aria-hidden="true"></i>
                <g:message code="de.iteratec.osm.setupMeasurementWizard.selectLocationAndConnectivity"
                           default="Select Location and Connectivity"/>
            </a>
        </li>
        <li class="wizardStep">
            <a data-toggle="tab" href="#createJob" id="createJobTab">
                <i class="fa fa-check fa-2x" aria-hidden="true"></i>
                <g:message code="de.iteratec.osm.setupMeasurementWizard.createJob" default="Create Job"/>
            </a>
        </li>
    </ul>

    <form action="save">

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

%{-- initially invisible modal dialog to ask if user wants to cancel the job creation --}%
<g:render template="cancelJobCreationDialog"/>


<content tag="include.bottom">
    <asset:javascript src="codemirror/codemirrorManifest.js"/>
    <asset:javascript src="prettycron/prettycronManifest.js"/>
    <asset:script type="text/javascript">
        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/measurementSetup/measurementSetupWizard.js"/>');

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
        createCodeMirror("navigationScript", false);
        window.onload = function() {
            $(".chosen-select").chosen({ search_contains: true });
        }
    </asset:script>
</content>
</body>
</html>
