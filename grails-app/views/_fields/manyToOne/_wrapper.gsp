<div class="form-group fieldcontain  <g:if test="${required}">required</g:if>">
    <g:render template="/_fields/labelTemplate"/>
    <div class="col-md-6">
        <g:select name="${property}.id" from="${type.list()}" optionKey="id" class="form-control" />
    </div>
</div>