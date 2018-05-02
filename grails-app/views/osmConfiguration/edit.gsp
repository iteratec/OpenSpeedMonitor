<!DOCTYPE html>
<html>
<head>
    <g:set var="entityName" value="${message(code: 'osmConfiguration.label', default: 'OsmConfiguration')}"
           scope="request"/>
    <meta name="layout" content="kickstart"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
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
                 data-content="<g:message code="de.iteratec.osm.configuration.userAgent.hover"/>">
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
            <button class="btn btn-primary">
                ${message(code: 'default.button.update.label', default: 'Update')}
            </button>
            <g:render template="/_common/modals/deleteSymbolLink"/>
            <button class="btn btn-default" type="reset">
                <g:message code="default.button.reset.label" default="Reset"/>
            </button>
        </div>
    </g:form>
</div>
</body>
</html>


<content tag="include.bottom">
    <asset:script type="text/javascript">
        $(window).load(function() {
    %{--OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="osmConfiguration/update.js"/>', "update");--}%
        $('[data-toggle="popover"]').popover();
    });
    </asset:script>
</content>
