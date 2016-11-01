<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="kickstart" />
        <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="show-user" class="content scaffold-show" role="main">
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <table class="table">
                <tbody>
                <td class="prop">
                <td valign="top" class="name">Username</td>
                <td><div class="property-value" >${user.username}</div></td>
                </tr>
                <td class="prop">
                <td valign="top" class="name">Password</td>
                <td><div class="property-value" >*****</div></td>
                </tr>
                <td class="prop">
                <td valign="top" class="name">Email</td>
                <td><div class="property-value" >${user.email}</div></td>
                </tr>
                <td class="prop">
                <td valign="top" class="name">Enabled</td>
                <td><div class="property-value" >${user.enabled}</div></td>
                </tr>
                <td class="prop">
                <td valign="top" class="name">AccountExpired</td>
                <td><div class="property-value" >${user.accountExpired}</div></td>
                </tr>
                <td class="prop">
                <td valign="top" class="name">AccountLocked</td>
                <td><div class="property-value" >${user.accountLocked}</div></td>
                </tr>
                <td class="prop">
                <td valign="top" class="name">PasswordExpired</td>
                <td><div class="property-value" >${user.passwordExpired}</div></td>
                </tr>

                </tbody>
            </table>
        </div>
    </body>
</html>
