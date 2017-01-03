<!doctype html>
<%@ defaultCodec="none" %>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="kickstart_osm"/>
        <title><g:message code="default.${mode}.label" args="[entityDisplayName]"/></title>

        <asset:stylesheet src="codemirror/codemirrorManifest.css"/>

    </head>

    <body>

        <%-- main menu --%>
        <g:render template="/layouts/mainMenu"/>

        <section id="${mode}-script" class="first">
            <h1><g:message code="default.${mode}.label" args="[entityDisplayName]"/></h1>
            <g:render template="messages"/>

            <p><g:message code="default.form.asterisk"/></p>
            <g:form method="post" role="form" class="form-horizontal">
                <!-- Modal -->
                <div class="modal fade" id="versionControlModal" tabindex="-1" role="dialog" aria-labelledby="versionControlModalLabel">
                    <div class="modal-dialog modal-lg" role="document">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                                <h4 class="modal-title" id="versionControlModalLabel">Modal title</h4>
                            </div>
                            <div class="row">
                                <div class="col-md-4">
                                    <div class="modal-body">
                                        <div id="archivedNavigationScriptTableWrapper">
                                            <div id="archivedNavigationScriptTableScroll">
                                                <table class="table table-hover">
                                                    <thead>
                                                    <tr>
                                                        <th>Date</th>
                                                        <th>Tag</th>
                                                    </tr>
                                                    </thead>
                                                    <tbody id="archivedNavigationScriptTableBody">
                                                    <g:each in="${archivedScripts}" status="i" var="archivedScript" >
                                                        <tr id="archivedNavigationScriptTableRow-${archivedScript.id}" onclick="showArchivedScriptById(${archivedScript.id},'${createLink(controller: 'script', action: 'getArchivedNavigationScript', absolute: true)}');">
                                                        <td class="versionControlMinCellWidth"><g:formatDate date="${archivedScript.dateCreated}" /></td>
                                                        <td>${archivedScript.archiveTag}</td>
                                                    %{--<g:if test="${i==0}">--}%
                                                    %{--<asset:script type="text/javascript">--}%
                                                    %{--showArchivedScriptById(${archivedScript.id},'${createLink(controller: 'script', action: 'getArchivedNavigationScript', absolute: true)}');--}%
                                                    %{--</asset:script>--}%
                                                    %{--</g:if>--}%
                                                    </g:each>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-8" id="archivedNavigationScriptColumn">
                                    <label for="archivedNavigationScript">
                                        <g:message code="script.navigationScript.label" default="Code" />
                                    </label>
                                    <textarea name="archivedNavigationScript" id="archivedNavigationScript"></textarea>
                                    <span id="archived-setEventName-warning-clone" class="setEventName-warning-icon" style="display: none;" rel="tooltip" data-html="true"></span>
                                    <p>
                                        <input type="checkbox" id="archivedLineBreakToggle" checked />
                                        <label for="archivedLineBreakToggle" style="display: inline">
                                            <g:message code="script.wrapLines.label" />
                                        </label>
                                    </p>

                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                                <g:actionSubmit class="btn btn-primary" action="loadArchivedScript"
                                                value="${message(code: 'de.iteratec.actions.duplicate', default: 'Kopie speichern')}"/>
                            </div>
                        </div>
                    </div>
                </div>
                <g:hiddenField name="archivedScriptId"/>
                <g:hiddenField name="id" value="${entity?.id}"/>
                <g:hiddenField name="version" value="${entity?.version}"/>
                <fieldset class="form">
                    <g:render template="form"/>
                </fieldset>
                <div id="newPageOrMeasuredEventInfo" class="card" style="display:none;">
                    <div id="newPagesContainer" style="display:none;">
                        ${message(code: 'script.newPage.info')}
                        <div id="newPages"></div>
                        <hr class="style-one">
                    </div>
                    <div id="newMeasuredEventsContainer" style="display:none;">
                        ${message(code: 'script.newMeasuredEvent.info')}
                        <div id="newMeasuredEvents"></div>
                    </div>
                </div>

                <div>
                    <g:if test="${mode == 'edit'}">
                        <g:actionSubmit type="button" class="btn btn-primary" id="saveButton" action="update"
                                        value="${message(code: 'default.button.save.label', default: 'Speichern')}"/>
                        <g:actionSubmit class="btn btn-primary" action="save" id="saveCopyButton"
                                        value="${message(code: 'de.iteratec.actions.duplicate', default: 'Kopie speichern')}"
                                        onclick="return promptForDuplicateName();"/>
                        <button type="button" class="btn btn-primary" data-toggle="modal" data-target="#versionControlModal"
                                onclick="$('#archivedNavigationScriptTableBody').children('tr:first').click();">
                            version control
                        </button>
                    </g:if>
                    <g:elseif test="${mode == 'create'}">
                        <g:actionSubmit class="btn btn-primary" action="save" id="saveButton"
                                        value="${message(code: 'default.button.create.label', default: 'Create')}"/>
                    </g:elseif>

                    <a href="<g:createLink action="list"/>" class="btn btn-warning"
                       onclick="return confirm('${message(code: 'default.button.unsavedChanges.confirm.message', default: 'Sind Sie sicher?')}');">
                        <g:message code="default.button.cancel.label" default="Abbrechen"/>
                    </a>

                    <g:if test="${mode == 'edit'}">
                        <g:render template="/_common/modals/deleteSymbolLink" model="[controllerLink: controllerLink]"/>
                    </g:if>
                </div>
            </g:form>
        </section>
        <content tag="include.bottom">
            <asset:javascript src="codemirror/codemirrorManifest.js"/>
            <asset:javascript src="prettycron/prettycronManifest.js"/>
            <asset:javascript src="script/versionControl.js"/>
            <asset:script type="text/javascript">
                function createCodeMirror(idCodemirrorElement, readonly){
                    new CodemirrorEditor({
                        idCodemirrorElement: idCodemirrorElement,
                        i18nMessage_NO_STEPS_FOUND: '${message(code: 'script.NO_STEPS_FOUND.warning')}',
                        i18nMessage_STEP_NOT_RECORDED: '${message(code: 'script.STEP_NOT_RECORDED.warning')}',
                        i18nMessage_DANGLING_SETEVENTNAME_STATEMENT: '${message(code: 'script.DANGLING_SETEVENTNAME_STATEMENT.warning')}',
                        i18nMessage_MISSING_SETEVENTNAME_STATEMENT: '${message(code: 'script.MISSING_SETEVENTNAME_STATEMENT.warning')}',
                        i18nMessage_WRONG_PAGE: '${message(code: 'script.WRONG_PAGE.error')}',
                        i18nMessage_TOO_MANY_SEPARATORS: '${message(code: 'script.TOO_MANY_SEPARATORS.error')}',
                        i18nMessage_MEASUREDEVENT_NOT_UNIQUE: '${message(code: 'script.MEASUREDEVENT_NOT_UNIQUE.error')}',
                        i18nMessage_WRONG_URL_FORMAT: '${message(code: 'script.WRONG_URL_FORMAT.error')}',
                        measuredEvents: '',
                        linkParseScriptAction: '${createLink(controller: 'script', action: 'parseScript', absolute: true)}',
                        linkMergeDefinedAndUsedPlaceholders: '${createLink(action: 'mergeDefinedAndUsedPlaceholders', absolute: true)}',
                        linkGetScriptSource: '${createLink(action: 'getScriptSource', absolute: true)}',
                        readonly: readonly
                    });
                }
                createCodeMirror("navigationScript",false);
                function promptForDuplicateName() {

                    var newName = prompt(
                            encodeURIComponent(OpenSpeedMonitor.i18n.duplicatePrompt),
                            encodeURIComponent($('input#label').val() + OpenSpeedMonitor.i18n.duplicateSuffix)
                    );
                    if (newName != null && newName != '') {
                        $('input#label').val(newName);
                        return true;
                    } else {
                        return false;
                    }
                }
            </asset:script>
        </content>
    </body>
</html>