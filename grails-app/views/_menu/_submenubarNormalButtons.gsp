<g:set var="entityName"
       value="${message(code: params.controller + '.label', default: params.controller.substring(0, 1).toUpperCase() + params.controller.substring(1).toLowerCase())}"/>

<li class="${params.action == "list" ? 'active' : ''}">
    <g:link action="list"><i class="fa fa-th-list"></i> <g:message code="default.list.label"
                                                                   args="[entityName]"/></g:link>
</li>
<li class="${params.action == "create" ? 'active' : ''}">
    <g:link action="create"><i class="fa fa-plus"></i> <g:message code="default.new.label"
                                                                  args="[entityName]"/></g:link>
</li>

<g:if test="${params.action == 'show' || params.action == 'edit'}">
    <!-- the item is an object (not a list) -->
    <li class="${params.action == "edit" ? 'active' : ''}">
        <g:link action="edit" id="${params.id}"><i class="fa fa-pencil"></i> <g:message code="default.edit.label"
                                                                                        args="[entityName]"/></g:link>
    </li>

</g:if>
