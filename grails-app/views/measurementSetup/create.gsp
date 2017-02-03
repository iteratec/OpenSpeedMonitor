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

                    <div class="tab-pane active" id="createJobGroup">

                        <h2>Set name for your Job Group</h2>

                        <div class="row form-horizontal">
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="inputJobGroup" class="col-sm-2 control-label">Job Group</label>

                                    <div class="col-sm-10">
                                        <input type="text" class="form-control" id="inputJobGroup" placeholder="Name" required>
                                    </div>
                                </div>
                            </div>

                            <div class="col-sm-6">
                                <p>
                                    Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                                    Est excepturi officiis placeat qui quibusdam?
                                    Aliquid commodi delectus deleniti dolorem eaque error,
                                    et id impedit maxime neque qui velit voluptas voluptatem!
                                    Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                                    Est excepturi officiis placeat qui quibusdam?
                                    Aliquid commodi delectus deleniti dolorem eaque error,
                                    et id impedit maxime neque qui velit voluptas voluptatem!

                                    Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                                    Est excepturi officiis placeat qui quibusdam?
                                    Aliquid commodi delectus deleniti dolorem eaque error,
                                    et id impedit maxime neque qui velit voluptas voluptatem!
                                    Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                                    Est excepturi officiis placeat qui quibusdam?
                                    Aliquid commodi delectus deleniti dolorem eaque error,
                                    et id impedit maxime neque qui velit voluptas voluptatem!
                                </p>
                            </div>
                        </div>

                        <div class="row">
                            <div class="form-group">
                                <div class="col-sm-6 text-right">
                                    <button class="btn btn-default" disabled>
                                        <i class="fa fa-caret-left" aria-hidden="true"></i>
                                        Previous
                                    </button>
                                    <button class="btn btn-primary">
                                        Next
                                        <i class="fa fa-caret-right" aria-hidden="true"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div> %{-- tab-pane --}%

                    <div class="tab-pane" id="createScript">

                        <h2>Write your measurement script</h2>

                        <div class="form-horizontal">

                            <div class="row">

                                <div class="col-sm-6">
                                    <div class="form-group">
                                        <label for="inputScriptName" class="col-sm-2 control-label">Name</label>

                                        <div class="col-sm-10">
                                            <input type="text" class="form-control" id="inputScriptName" placeholder="Name" required>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="inputScriptDescription"
                                               class="col-sm-2 control-label">Description</label>

                                        <div class="col-sm-10">
                                            <textarea class="form-control" id="inputScriptDescription"
                                                      rows="3"></textarea>
                                        </div>
                                    </div>
                                </div>

                                <div class="col-sm-6">
                                    <p>
                                        Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                                        Est excepturi officiis placeat qui quibusdam?
                                        Aliquid commodi delectus deleniti dolorem eaque error,
                                        et id impedit maxime neque qui velit voluptas voluptatem!
                                        Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                                        Est excepturi officiis placeat qui quibusdam?
                                        Aliquid commodi delectus deleniti dolorem eaque error,
                                        et id impedit maxime neque qui velit voluptas voluptatem!

                                        Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                                        Est excepturi officiis placeat qui quibusdam?
                                        Aliquid commodi delectus deleniti dolorem eaque error,
                                        et id impedit maxime neque qui velit voluptas voluptatem!
                                        Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                                        Est excepturi officiis placeat qui quibusdam?
                                        Aliquid commodi delectus deleniti dolorem eaque error,
                                        et id impedit maxime neque qui velit voluptas voluptatem!
                                    </p>
                                </div>

                            </div> %{-- row --}%

                            <div class="row">

                                <div class="col-sm-6">
                                    <p class="col-sm-offset-2"><g:message code="script.autoComplete.label" default="Press Ctrl + Space to get a list of keywords or valid event names, respectively." /></p>
                                    <div class="form-group">
                                        <label for="navigationScript" class="col-sm-2 control-label">Code</label>

                                        <div class="col-sm-10">
                                            <textarea id="navigationScript" class="form-control" style="display: none;">${code}</textarea>
                                            <span id="setEventName-warning-clone" class="setEventName-warning-icon" style="display: none;" rel="tooltip" data-html="true"></span>
                                        </div>
                                    </div>
                                    <div class="col-sm-offset-2 col-sm-10">
                                        <div class="form-group">
                                            <input type="checkbox" id="lineBreakToggle" checked />
                                            <label for="lineBreakToggle" style="display: inline">
                                                <g:message code="script.wrapLines.label" />
                                            </label>
                                        </div>
                                        <p id="usedVariables"
                                           data-instructions="${HtmlUtils.htmlEscape(message(code: 'script.placeholdersInstructions.label'))}"
                                           data-usedvars="${HtmlUtils.htmlEscape(message(code: 'codemirror.usedVariables.label'))}">
                                        </p>
                                    </div>
                                </div>

                            </div> %{-- row --}%

                        </div>

                        <div class="row">
                            <div class="form-group">
                                <div class="col-sm-6 text-right">
                                    <button class="btn btn-default">
                                        <i class="fa fa-caret-left" aria-hidden="true"></i>
                                        Previous
                                    </button>
                                    <button class="btn btn-primary" onclick="promptForDuplicateName()">
                                        Next
                                        <i class="fa fa-caret-right" aria-hidden="true"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div> %{-- tab-pane --}%

                    <div class="tab-pane" id="createJob">

                        <h2>Set name for your job and start the measurement</h2>

                        <div class="row form-horizontal">
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="inputJob" class="col-sm-2 control-label">Job</label>

                                    <div class="col-sm-10">
                                        <input type="text" class="form-control" id="inputJob" placeholder="Name"
                                               required>
                                    </div>
                                </div>
                            </div>

                            <div class="col-sm-6">
                                <p>
                                    Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                                    Est excepturi officiis placeat qui quibusdam?
                                    Aliquid commodi delectus deleniti dolorem eaque error,
                                    et id impedit maxime neque qui velit voluptas voluptatem!
                                    Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                                    Est excepturi officiis placeat qui quibusdam?
                                    Aliquid commodi delectus deleniti dolorem eaque error,
                                    et id impedit maxime neque qui velit voluptas voluptatem!

                                    Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                                    Est excepturi officiis placeat qui quibusdam?
                                    Aliquid commodi delectus deleniti dolorem eaque error,
                                    et id impedit maxime neque qui velit voluptas voluptatem!
                                    Lorem ipsum dolor sit amet, consectetur adipisicing elit.
                                    Est excepturi officiis placeat qui quibusdam?
                                    Aliquid commodi delectus deleniti dolorem eaque error,
                                    et id impedit maxime neque qui velit voluptas voluptatem!
                                </p>
                            </div>
                        </div>

                        <div class="row">
                            <div class="form-group">
                                <div class="col-sm-6 text-right">
                                    <button class="btn btn-default">
                                        <i class="fa fa-caret-left" aria-hidden="true"></i>
                                        Previous
                                    </button>
                                    <button class="btn btn-primary" disabled>
                                        Next
                                        <i class="fa fa-caret-right" aria-hidden="true"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div> %{-- tab-pane --}%

                </div> %{-- tab-content --}%

                <div>
                    <button class="btn btn-default" type="reset">
                        <i class="fa fa-times" aria-hidden="true"></i>
                        Cancel
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