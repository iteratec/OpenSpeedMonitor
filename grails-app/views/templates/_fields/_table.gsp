<table class="table table-bordered">
    <thead>
    <tr>
        <g:each in="${domainProperties}" var="p" status="i">
            <g:set var="propTitle">${domainClass.propertyName}.${p.name}.label</g:set>
            <g:sortableColumn property="${p.name}" title="${message(code: propTitle, default: p.naturalName)}" />
        </g:each>
    </tr>
    </thead>
    <tbody>
    <g:each in="${collection}" var="bean" status="i">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
            <g:each in="${domainProperties}" var="p" status="j">
                <g:if test="${j==0}">
                    <td><g:link method="GET" resource="${bean}"><f:display bean="${bean}" property="${p.name}" displayStyle="${displayStyle?:'table'}" /></g:link></td>
                </g:if>
                <g:else>
                    <td><f:display bean="${bean}" property="${p.name}"  displayStyle="${displayStyle?:'table'}" /></td>
                </g:else>
            </g:each>
        </tr>
    </g:each>
    </tbody>
</table>