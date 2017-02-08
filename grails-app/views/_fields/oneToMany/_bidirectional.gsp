<ul class="one-to-many">
    <g:if test="${bean."$property"}">
        <g:each var="alias" in="${bean."$property"}" >
            <li>
                <a href="<g:createLink controller="${propertyClass}"  action="show" id="${alias.id}" />">${alias}</a>
            </li>
        </g:each>
    </g:if>
    <li class="add">
        <a href="<g:createLink controller="${propertyClass}"  action="create" />?${lowerCaseBean}.id=${bean.id}"><g:message code="default.add.label" args="${[label]}" default="Add ${label}" /></a>
    </li>
</ul>