<!DOCTYPE html>
<html>
    <head>
        <g:set var="entityName" value="${message(code: 'threshold.label', default: 'Threshold')}" scope="request"/>
        <meta name="layout" content="kickstart" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="show-threshold" class="content scaffold-show" role="main">
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <f:display bean="threshold" />
        </div>
    </body>
</html>
