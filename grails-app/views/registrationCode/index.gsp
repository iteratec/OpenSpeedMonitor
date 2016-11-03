<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="kickstart_osm" />
        <g:set var="entityName" value="${message(code: 'role.label', default: 'Registration Code')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="list-registrationCode" class="content scaffold-list" role="main">
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            <table  class="table table-bordered">
                <thead>
                    <tr>
                        <g:sortableColumn property="dateCreated" title="dateCreated"/>
                        <g:sortableColumn property="username" title="username"/>
                        <g:sortableColumn property="token" title="token"/>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${registrationCodeList}" var="registrationCode">
                        <tr>
                            <td>${registrationCode.dateCreated}</td>
                            <td>${registrationCode.username}</td>
                            <td>${registrationCode.token}</td>
                        </tr>

                    </g:each>
                </tbody>
            </table>
            <div>
                <bs:paginate total="${registrationCodeCount ?: 0}" />
            </div>
        </div>
    </body>
</html>
