<%@ page import="de.iteratec.osm.csi.Page" %>
<table class="table table-bordered">
    <thead>
    <tr>
        <th><g:link action="index" onclick="sortBy('name'); return false;" >${message(code: 'page.name.label', default: 'Name')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('weight'); return false;" >${message(code: 'page.weight.label', default: 'Weight')}</g:link></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${pages}" status="i" var="pageInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${pageInstance.id}">${fieldValue(bean: pageInstance, field: "name")}</g:link></td>

            <td>${fieldValue(bean: pageInstance, field: "weight")}</td>

        </tr>
    </g:each>
    </tbody>
</table>
