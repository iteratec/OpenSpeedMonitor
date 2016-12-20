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
            <!-- Modal -->
            <div id="showCreateModal" class="modal fade" role="dialog">
                <div class="modal-dialog">

                    <!-- Modal content-->
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal">&times;</button>
                            <h4 class="modal-title">${message(code: 'script.validation.create.confirmation.title')}</h4>
                        </div>
                        <div class="modal-body" id="createModalBody"></div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-primary" onclick="saveScript()">${message(code: 'default.button.create.label', default: 'Create')}</button>
                            <button type="button" class="btn btn-error" data-dismiss="modal"><g:message code="default.button.cancel.label" default="Cancel"/></button>
                        </div>
                    </div>

                </div>
            </div>

            <h1><g:message code="default.${mode}.label" args="[entityDisplayName]"/></h1>
            <g:render template="messages"/>

            <p><g:message code="default.form.asterisk"/></p>

            <g:form method="post" role="form" class="form-horizontal">
                <g:hiddenField name="id" value="${entity?.id}"/>
                <g:hiddenField name="version" value="${entity?.version}"/>
                <fieldset class="form">
                    <g:render template="form"/>
                </fieldset>
                <div>
                    <g:if test="${mode == 'edit'}">
                        <input type="button" class="btn btn-primary"
                                        onclick="checkForNewPageOrMeasuredEventNames();"
                                        value="${message(code: 'default.button.save.label', default: 'Speichern')}"/>
                        <g:actionSubmit value="!" style="display: none" action="update" id="updateScriptActionSubmit"/>
                        <g:actionSubmit class="btn btn-primary" action="save"
                                        value="${message(code: 'de.iteratec.actions.duplicate', default: 'Kopie speichern')}"
                                        onclick="return promptForDuplicateName();"/>
                    </g:if>
                    <g:elseif test="${mode == 'create'}">
                        <g:actionSubmit class="btn btn-primary" action="save"
                                        value="${message(code: 'default.button.create.label', default: 'Create')}"/>
                        <g:actionSubmit value="!" style="display: none" action="update" id="saveScriptActionSubmit"/>
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
            <asset:script type="text/javascript">
                var editor = new CodemirrorEditor({
                    idCodemirrorElement: "navigationScript",
                    i18nMessage_NO_STEPS_FOUND: '${message(code: 'script.NO_STEPS_FOUND.warning')}',
                    i18nMessage_STEP_NOT_RECORDED: '${message(code: 'script.STEP_NOT_RECORDED.warning')}',
                    i18nMessage_DANGLING_SETEVENTNAME_STATEMENT: '${message(code: 'script.DANGLING_SETEVENTNAME_STATEMENT.warning')}',
                    i18nMessage_MISSING_SETEVENTNAME_STATEMENT: '${message(code: 'script.MISSING_SETEVENTNAME_STATEMENT.warning')}',
                    measuredEvents: ${measuredEvents},
                    linkParseScriptAction: '${createLink(controller: 'script', action: 'parseScript', absolute: true)}',
                    linkMergeDefinedAndUsedPlaceholders: '${createLink(action: 'mergeDefinedAndUsedPlaceholders', absolute: true)}',
                    linkGetScriptSource: '${createLink(action: 'getScriptSource', absolute: true)}',
                    linkCheckForNewPageOrMeasuredEventNames: '${createLink(action: 'getNewPagesAndMeasuredEvents', absolute: true)}',
                    readonly: false
                });
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
                function saveScript(){
                    if ("${mode}" == "edit") $("#updateScriptActionSubmit").click();
                    else if ("${mode}" == "create") $("#saveScriptActionSubmit").click();
                }
                function  displayPrompt(newPageAndMeasuredEventMap) {
                    $("#createModalBody").empty();
                    if (newPageAndMeasuredEventMap.newPageNames!="") $("#createModalBody").append($("<p>",{html:"${message(code: 'script.validation.create.confirmation.page')}"+ newPageAndMeasuredEventMap.newPageNames}));
                    if (newPageAndMeasuredEventMap.newMeasuredEventNames) $("#createModalBody").append($("<p>",{html:"${message(code: 'script.validation.create.confirmation.measuredEvent')}"+newPageAndMeasuredEventMap.newMeasuredEventNames}));
                    $('#showCreateModal').modal('show');
                }
                function checkForNewPageOrMeasuredEventNames() {
                    editor.checkForNewPageOrMeasuredEventNames(displayPrompt, saveScript);
                }
            </asset:script>
        </content>
    </body>
</html>