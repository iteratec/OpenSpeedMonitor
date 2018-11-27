<!DOCTYPE html>
<html>
<head>
    <g:set var="entityName" value="${message(code: 'de.iteratec.osmConfiguration.label', default: 'OSM Configuration')}"
           scope="request"/>
    <meta name="layout" content="layoutOsm"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<ul id="Menu" class="nav nav-pills">
    <g:set var="entityName"
           value="${message(code: params.controller + '.label', default: params.controller.substring(0, 1).toUpperCase() + params.controller.substring(1).toLowerCase())}"/>
</ul>

<div id="edit-osmConfiguration" class="content scaffold-edit" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${osmConfiguration}">
        <ul class="errors" role="alert">
            <g:eachError bean="${osmConfiguration}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><div
                        class="alert alert-danger"><g:message error="${error}"/></div></li>
            </g:eachError>
        </ul>
    </g:hasErrors>
    <g:form resource="${osmConfiguration}" method="PUT" class="form-horizontal">
        <g:hiddenField name="version" value="${osmConfiguration?.version}"/>
        <fieldset class="form-horizontal">
            <f:all bean="osmConfiguration" except="globalUserAgentSuffix"/>
            <div class="form-group fieldcontain " data-toggle="popover"
                 data-placement="bottom" data-trigger="hover" data-html="true"
                 data-content="${message(code: 'de.iteratec.osm.configuration.user.agent.hover')}">
                <label for="globalUserAgentSuffix" class="control-label col-md-3">
                    Global User Agent Suffix
                </label>

                <div class="col-md-6">
                    <input type="text" name="globalUserAgentSuffix" value="${osmConfiguration.globalUserAgentSuffix}"
                           class="form-control"
                           id="globalUserAgentSuffix">
                </div>
            </div>
        </fieldset>

        <div>
        <g:actionSubmit class="btn btn-primary" action="update"
                        value="${message(code: 'default.button.update.label', default: 'Update')}"/>
        <button class="btn btn-default" type="reset"><g:message code="default.button.reset.label"
                                                                default="Reset"/></button>
        <g:actionSubmit class="btn btn-warning" action="show"
                        value="${message(code: 'default.button.cancel.label', default: 'Cancel')}"/>
        </div>
    </g:form>
</div>
</body>
</html>


<content tag="include.bottom">
    <asset:script type="text/javascript">
        $(window).on('load', function() {
        $('[data-toggle="popover"]').popover();
    });
    </asset:script>
</content>
