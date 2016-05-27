<div class="control-group fieldcontain  <g:if test="${required}">required</g:if>">
    <g:render template="/_fields/labelTemplate"/>
    <div class="controls">
        <g:select name="${property}.id" from="${type.list()}" class="input-medium" optionKey="id"/>
    </div>
</div>