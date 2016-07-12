<table class="table">
    <tbody>
    <g:each in="${domainClass.persistentProperties}" var="p">
        <td class="prop">
            <td valign="top" class="name"><g:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></td>
            <td><div class="property-value" aria-labelledby="${p.name}-label">${body(p)}</div></td>
        </tr>
    </g:each>
    </tbody>
</table>
