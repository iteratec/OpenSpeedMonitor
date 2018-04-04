<!DOCTYPE html>
<html>
    <head>
        <g:set var="entityName" value="${message(code: 'osmConfiguration.label', default: 'OsmConfiguration')}" scope="request"/>
        <meta name="layout" content="kickstart" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="show-osmConfiguration" class="content scaffold-show" role="main">
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <f:display bean="osmConfiguration" />
        </div>
    </body>
</html>
