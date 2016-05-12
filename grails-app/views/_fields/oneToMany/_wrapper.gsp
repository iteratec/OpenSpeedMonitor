<div class="control-group fieldcontain  ">
    <label for="${property}" class="control-label">${label}</label>
    <g:set var="lowerCaseBean"  value="${bean.class.getSimpleName()[0].toLowerCase()+bean.class.getSimpleName().substring(1)}"/>  %{--Well that's one way, but their should be a "nice" way :/--}%
    <g:set var="propertyClass" value="${persistentProperty.getOtherSide().getDomainClass().getName()}"/>
    <div class="controls">
        <ul class="one-to-many">
            <g:if test="${bean."$property"}">
                <g:each var="alias" in="${bean."$property"}" >
                    <li>
                        <a href="<g:createLink controller="${propertyClass}" absolute="true" action="show" id="${alias.id}" />">${alias}</a>
                    </li>
                </g:each>
            </g:if>
            <li class="add">
                <a href="<g:createLink controller="${propertyClass}" absolute="true" action="create" />?${lowerCaseBean}.id=${bean.id}"><g:message code="default.add.label" args="${[label]}" default="Add ${label}" /></a>
            </li>
        </ul>
    </div>
</div>