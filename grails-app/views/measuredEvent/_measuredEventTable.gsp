<%@ page import="de.iteratec.osm.result.MeasuredEvent" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="sortBy('name'); return false;" >${message(code: 'measuredEvent.name.label', default: 'Name')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('testedPage'); return false;" >${message(code: 'measuredEvent.testedPage.label', default: 'Tested Page')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${measuredEvents}" status="i" var="measuredEventInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${measuredEventInstance.id}">${fieldValue(bean: measuredEventInstance, field: "name")}</g:link></td>

            <td><g:link controller="page" action="show" id="${measuredEventInstance.testedPage.id}">${fieldValue(bean: measuredEventInstance, field: "testedPage")}</g:link></td>

        </tr>
    </g:each>
    </tbody>
</table>
