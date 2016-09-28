<%@ page import="de.iteratec.osm.measurement.environment.BrowserAlias" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="sortBy('alias'); return false;" >${message(code: 'browserAlias.alias.label', default: 'Alias')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('browser'); return false;" >${message(code: 'browserAlias.browser.label', default: 'Browser')}</g:link></th>


    </tr>
    </thead>
    <tbody>
    <g:each in="${browserAliases}" status="i" var="browserAliasInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${browserAliasInstance.id}">${fieldValue(bean: browserAliasInstance, field: "alias")}</g:link></td>

            <td><g:link controller="browser" action="show" id="${browserAliasInstance.browser.id}">${fieldValue(bean: browserAliasInstance, field: "browser")}</g:link></td>

        </tr>
    </g:each>
    </tbody>
</table>
