<!DOCTYPE html>
<html>
<head>
    <g:set var="entityName" value="${message(code: 'measuredEvent.label', default: 'MeasuredEvent')}" scope="request"/>
    <meta name="layout" content="layoutOsm"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<g:render template="/_menu/submenubarWithoutDelete"/>
<div id="show-measuredEvent" class="content scaffold-show" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <f:display bean="measuredEvent"/>
</div>
</body>
</html>
