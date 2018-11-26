<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="layoutOsm" />
        <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="list-user" class="content scaffold-list" role="main">
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            <f:table collection="${userList}" properties='["username","enabled","accountExpired","accountLocked","passwordExpired"]'/>

            <div>
                <bs:paginate total="${userCount ?: 0}" />
            </div>
        </div>
    </body>
</html>
