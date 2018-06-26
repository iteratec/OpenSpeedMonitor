<div class="form-group fieldcontain  ">
    <g:render template="/_fields/labelTemplate"/>
    <g:set var="lowerCaseBean"  value="${bean.class.getSimpleName()[0].toLowerCase()+bean.class.getSimpleName().substring(1)}"/>  %{--Well that's one way, but their should be a "nice" way :/--}%
    <g:set var="propertyClass" value="${persistentProperty.getAssociatedEntity().getDecapitalizedName()}"/>
    <div class="col-md-6">
    <g:if test="${persistentProperty.isBidirectional()}">
        <g:render template="/_fields/oneToMany/bidirectional"/>
    </g:if>
    <g:else>
        <g:render template="/_fields/oneToMany/unidirectional"/>
    </g:else>
    </div>
</div>
