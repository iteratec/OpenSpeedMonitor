<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="layoutOsm" />
        <g:set var="entityName" value="${message(code: 'role.label', default: 'Role')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="list-role" class="content scaffold-list" role="main">
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            <f:table collection="${roleList}" />

            <div>
                <bs:paginate total="${roleCount ?: 0}" />
            </div>
        </div>
    </body>
</html>
