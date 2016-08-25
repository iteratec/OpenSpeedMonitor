<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="kickstart" />
        <g:set var="entityName" value="${message(code: 'jobSet.label', default: 'JobSet')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="list-jobSet" class="content scaffold-list" role="main">
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            <f:table collection="${jobSetList}" />

            <div class="pagination">
                <bs:paginate total="${jobSetCount ?: 0}" />
            </div>
        </div>
    </body>
</html>