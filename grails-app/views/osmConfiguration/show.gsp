<!DOCTYPE html>
<html>
<head>
    <g:set var="entityName" value="${message(code: 'de.iteratec.osmConfiguration.label', default: 'OSM Configuration')}"
           scope="request"/>
    <meta name="layout" content="kickstart"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<ul id="Menu" class="nav nav-pills">
    <g:set var="entityName"
           value="${message(code: params.controller + '.label', default: params.controller.substring(0, 1).toUpperCase() + params.controller.substring(1).toLowerCase())}"/>
    <li class="${params.action == "edit" ? 'active' : ''}">
        <g:link action="edit" id="${params.id}"><i class="fa fa-pencil"></i> <g:message code="default.edit.label"
                                                                                        args="[entityName]"/></g:link>
    </li>
</ul>

<div id="show-osmConfiguration" class="content scaffold-show" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <f:display bean="osmConfiguration"/>
</div>
</body>
</html>
