<!DOCTYPE html>
<html>
    <head>
        <g:set var="entityName" value="${message(code: 'threshold.label', default: 'Threshold')}" scope="request"/>
        <meta name="layout" content="kickstart" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="list-threshold" class="content scaffold-list" role="main">
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            <f:table collection="${thresholdList}" />

            <div>
                <bs:paginate total="${thresholdCount ?: 0}" />
            </div>
        </div>
    </body>
</html>