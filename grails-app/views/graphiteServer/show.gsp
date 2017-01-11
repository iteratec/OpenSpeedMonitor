<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="kickstart" />
        <g:set var="entityName" value="${message(code: 'graphiteServer.label', default: 'GraphiteServer')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="show-graphiteServer" class="content scaffold-show" role="main">
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <f:display bean="graphiteServer" />
        </div>
    </body>
</html>
