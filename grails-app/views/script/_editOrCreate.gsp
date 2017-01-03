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
            <asset:script type="text/javascript">
                var editor = new CodemirrorEditor({
                    idCodemirrorElement: "navigationScript",
                    i18nMessage_NO_STEPS_FOUND: '${message(code: 'script.NO_STEPS_FOUND.warning')}',
                    i18nMessage_STEP_NOT_RECORDED: '${message(code: 'script.STEP_NOT_RECORDED.warning')}',
                    i18nMessage_DANGLING_SETEVENTNAME_STATEMENT: '${message(code: 'script.DANGLING_SETEVENTNAME_STATEMENT.warning')}',
                    i18nMessage_MISSING_SETEVENTNAME_STATEMENT: '${message(code: 'script.MISSING_SETEVENTNAME_STATEMENT.warning')}',
                    i18nMessage_WRONG_PAGE: '${message(code: 'script.WRONG_PAGE.error')}',
                    i18nMessage_TOO_MANY_SEPARATORS: '${message(code: 'script.TOO_MANY_SEPARATORS.error')}',
                    i18nMessage_MEASUREDEVENT_NOT_UNIQUE: '${message(code: 'script.MEASUREDEVENT_NOT_UNIQUE.error')}',
                    i18nMessage_WRONG_URL_FORMAT: '${message(code: 'script.WRONG_URL_FORMAT.error')}',
                    measuredEvents: ${measuredEvents},
                    linkParseScriptAction: '${createLink(controller: 'script', action: 'parseScript', absolute: true)}',
                    linkMergeDefinedAndUsedPlaceholders: '${createLink(action: 'mergeDefinedAndUsedPlaceholders', absolute: true)}',
                    linkGetScriptSource: '${createLink(action: 'getScriptSource', absolute: true)}',
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
            </asset:script>
        </content>
    </body>
</html>