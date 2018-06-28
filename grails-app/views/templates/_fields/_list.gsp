<table class="table">
    <tbody>
    <g:each in="${domainClass.persistentProperties}" var="p">
        <td class="prop">
            <td valign="top" class="name"><g:message code="${domainClass.decapitalizedName}.${p.name}.label" default="${grails.util.GrailsNameUtils.getNaturalName(p.name)}" /></td>
            <td><div class="property-value" aria-labelledby="${p.name}-label">${body(p)}</div></td>
        </tr>
    </g:each>
    </tbody>
</table>
