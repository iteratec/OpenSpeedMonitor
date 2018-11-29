<!DOCTYPE html>
<html>
    <head>
        <g:set var="entityName" value="${message(code: 'graphiteEventSourcePath.label', default: 'GraphiteEventSourcePath')}" scope="request"/>
        <meta name="layout" content="layoutOsm" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="show-graphiteEventSourcePath" class="content scaffold-show" role="main">
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <f:display bean="graphiteEventSourcePath" />
        </div>
    </body>
</html>
