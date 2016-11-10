<%@ page import="de.iteratec.osm.measurement.environment.Browser" %>
<table class="table table-bordered">
    <thead>
    <tr>

        <th><g:link action="index" onclick="sortBy('name'); return false;" >${message(code: 'browser.name.label', default: 'name')}</g:link></th>

        <th><g:link action="index" onclick="sortBy('browserAliases'); return false;" >${message(code: 'browser.browserAliases.label', default: 'Browser Aliases')}</g:link></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${browsers}" status="i" var="browserInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

            <td><g:link action="show" id="${browserInstance.id}">${fieldValue(bean: browserInstance, field: "name")}</g:link></td>

            <td>
                <g:each in ="${browserInstance.browserAliases}" var = "browserAlias">
                    <g:link controller="BrowserAlias" action="show" id="${browserAlias.id}">${fieldValue(bean: browserAlias, field: "alias")}</g:link>
                    <br>
                </g:each>
            </td>

        </tr>
    </g:each>
    </tbody>
</table>
