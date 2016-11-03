<div class="form-group fieldcontain  <g:if test="${required}">required</g:if>">
    <g:render template="/_fields/labelTemplate"/>
    <div>
        <g:select name="${property}.id" from="${type.list()}" class="form-control" optionKey="id"/>
    </div>
</div>