<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <g:set var="entityName" value="${message(code: 'location.label', default: 'Location')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<g:render template="/_menu/submenubarWithoutDelete"/>
<div id="show-location" class="content scaffold-show" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <f:display bean="location"/>
</div>
</body>
</html>
