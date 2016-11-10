<%@ page import="de.iteratec.osm.csi.Page" %>
<table class="table table-bordered">
    <thead>
    <tr>
        <th><g:link action="index" onclick="OpenSpeedMonitor.responsiveTable.sortBy('name'); return false;" >${message(code: 'page.name.label', default: 'Name')}</g:link></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${pages}" status="i" var="pageInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${pageInstance.id}">${fieldValue(bean: pageInstance, field: "name")}</g:link></td>

        </tr>
    </g:each>
    </tbody>
</table>
