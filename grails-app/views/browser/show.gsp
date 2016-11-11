<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <g:set var="entityName" value="${message(code: 'browser.label', default: 'Browser')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<g:render template="/_menu/submenubarWithoutDelete"/>
<div id="show-browser" class="content scaffold-show" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <f:display bean="browser"/>
</div>
</body>
</html>
