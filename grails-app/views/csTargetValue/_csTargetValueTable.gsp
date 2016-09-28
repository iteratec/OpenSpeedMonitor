<%@ page import="de.iteratec.osm.csi.CsTargetValue" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="sortBy('date'); return false;" >${message(code: 'csTargetValue.date.label', default: 'Date')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('csInPercent'); return false;" >${message(code: 'csTargetValue.csInPercent.label', default: 'Cs In Percent')}</g:link></th>



    </tr>
    </thead>
    <tbody>
    <g:each in="${csTargetValues}" status="i" var="csTargetValueInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${csTargetValueInstance.id}">${fieldValue(bean: csTargetValueInstance, field: "date")}</g:link></td>

            <td>${fieldValue(bean: csTargetValueInstance, field: "csInPercent")}</td>

        </tr>
    </g:each>
    </tbody>
</table>
