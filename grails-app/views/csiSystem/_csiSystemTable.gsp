<%@ page import="de.iteratec.osm.csi.CsiSystem" %>
<table class="table table-bordered">
    <thead>
    <tr>
        <th><g:link action="index" onclick="sortBy('label'); return false;" >${message(code: 'csiSystem.label.label', default: 'Label')}</g:link></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${csiSystems}" status="i" var="csiSystemInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${csiSystemInstance.id}">${fieldValue(bean: csiSystemInstance, field: "label")}</g:link></td>

        </tr>
    </g:each>
    </tbody>
</table>
