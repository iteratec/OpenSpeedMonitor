<%@ page defaultCodec="none" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.springframework.web.util.HtmlUtils" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm" />
    <title><g:message code="de.iteratec.osm.ui.setupwizards.infra.title" default="Setup infrastructure"></g:message></title>

    <asset:stylesheet src="script/scriptManifest.css"/>
    <asset:stylesheet src="setupWizard/setupWizard.css"/>
</head>
<body>
<h1><g:message code="de.iteratec.osm.ui.setupwizards.infra.title" default="Setup infrastructure"></g:message></h1>

<div class="card">
    <div class="progress" id="setupWizardProgressBarContainer">
        <div class="progress-bar" id="setupWizardProgressBar" role="progressbar"></div>
    </div>

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
                <h1>Tab1</h1>

                <button data-toggle="tab" href="#createScript" class="btn btn-primary" id="setJobGroubTabNextButton" enabled>
                    <g:message code="default.paginate.next" default="Next"/>
                    <i class="fa fa-caret-right" aria-hidden="true"></i>
                </button>
            </div>

            <div class="tab-pane" id="createScript">
                <h1>Tab2</h1>

                <button data-toggle="tab" href="#selectLocationAndConnectivity" class="btn btn-primary" id="createScriptTabNextButton" enabled>
                    <g:message code="default.paginate.next" default="Next"/>
                    <i class="fa fa-caret-right" aria-hidden="true"></i>
                </button>
            </div>

            <div class="tab-pane" id="selectLocationAndConnectivity">
                <h1>Tab3</h1>

                <button data-toggle="tab" href="#createJob" class="btn btn-primary" id="selectLocationAndConnectivityTabNextButton" enabled>
                    <g:message code="default.paginate.next" default="Next"/>
                    <i class="fa fa-caret-right" aria-hidden="true"></i>
                </button>
            </div>

            <div class="tab-pane" id="createJob">
                <h1>Tab4</h1>

                <button data-toggle="tab" href="#setJobGroup" class="btn btn-primary" id="createJobTabNextButton" enabled>
                    <g:message code="default.paginate.next" default="Next"/>
                    <i class="fa fa-caret-right" aria-hidden="true"></i>
                </button>
            </div>

        </div> %{-- tab-content --}%
    </form>


    <%--<div class="tabbable">
        <ul class="nav nav-tabs">
            <li class="active">
                <a id="change-user-passwords" href="#1" data-toggle="tab">change user passwords</a>
            </li>
            <li>
                <a id="add-wpt-api-key" href="#2" data-toggle="tab">add wpt apikey</a>
            </li>
            <li>
                <a id="show-infos" href="#3" data-toggle="tab">show infos</a>
            </li>
        </ul>
        <div class="iteratec-tab-content">
            <div class="tab-pane active" id="1">

            </div>
            <div class="tab-pane" id="2">

            </div>
            <div class="tab-pane" id="3">

            </div>
        </div>
    </div>--%>
</div>

<content tag="include.bottom">
    <asset:javascript src="/infrastructureSetup/infrastructureSetupWizard.js"/>
    %{--<asset:script type="text/javascript">--}%
        %{--OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/infrastructureSetup/infrastructureSetupWizard.js"/>');--}%
    %{--</asset:script>--}%
</content>
</body>
</html>