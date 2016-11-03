<g:if test="${ params.action == 'show' || params.action == 'edit' }">
    <li>
        <g:render template="/_common/modals/deleteTextLink"/>
    </li>
</g:if>