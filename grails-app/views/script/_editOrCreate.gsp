<!doctype html>
<%@ defaultCodec="none" %>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="kickstart_osm"/>
        <title><g:message code="default.${mode}.label" args="[entityDisplayName]"/></title>

        <asset:stylesheet src="codemirror/codemirrorManifest.css"/>

        <style type="text/css">
            .control-label {
                width: 250px !important;
                margin-right: 1em !important;
            }
            .form-group {
                margin-bottom: 0.4em !important;
            }
        </style>

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

                <div class="form-actions">
                    <g:if test="${mode == 'edit'}">
                        <g:actionSubmit class="btn btn-primary" action="update"
                                        value="${message(code: 'default.button.save.label', default: 'Speichern')}"/>
                        <g:actionSubmit class="btn btn-primary" action="save"
                                        value="${message(code: 'de.iteratec.actions.duplicate', default: 'Kopie speichern')}"
                                        onclick="return promptForDuplicateName();"/>
                    </g:if>
                    <g:elseif test="${mode == 'create'}">
                        <g:actionSubmit class="btn btn-primary" action="save"
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
                    measuredEvents: ${measuredEvents},
                    linkParseScriptAction: '${createLink(controller: 'script', action: 'parseScript', absolute: true)}',
                    linkMergeDefinedAndUsedPlaceholders: '${createLink(action: 'mergeDefinedAndUsedPlaceholders', absolute: true)}',
                    linkGetScriptSource: '${createLink(action: 'getScriptSource', absolute: true)}',
                    readonly: false
                });
                function promptForDuplicateName() {
                    var newName = prompt(
                            encodeURIComponent(POSTLOADED.i18n_duplicatePrompt),
                            encodeURIComponent($('input#label').val() + POSTLOADED.i18n_duplicateSuffix)
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