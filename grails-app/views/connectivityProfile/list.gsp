<%@ page import="de.iteratec.osm.measurement.schedule.ConnectivityProfile" %>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
    <head>
        <meta name="layout" content="kickstart_osm" />
        <g:set var="entityName" value="${message(code: 'connectivityProfile.label', default: 'Connection')}" />
        <title><g:message code="connectivityProfile.label.plural" /></title>
    </head>

    <body>

        <g:render template="/layouts/mainMenu" />

        <a href="<g:createLink action="create" />" class="btn btn-primary" style="margin-bottom: 1em;">
                <i class="fa fa-plus"></i> <g:message code="default.create.label" args="[entityName]" />
            </a>
        <section id="list-connectivityProfile" class="first">

            <table class="table table-bordered">
                <thead>
                    <tr>

                        <g:sortableColumn property="name" title="${message(code: 'connectivityProfile.name.label', default: 'Name')}" />

                        <g:sortableColumn property="bandwidthDown" title="${message(code: 'connectivityProfile.bandwidthDown.label', default: 'Bandwidth Down')}" />

                        <g:sortableColumn property="bandwidthUp" title="${message(code: 'connectivityProfile.bandwidthUp.label', default: 'Bandwidth Up')}" />

                        <g:sortableColumn property="latency" title="${message(code: 'connectivityProfile.latency.label', default: 'Latency')}" />

                        <g:sortableColumn property="packetLoss" title="${message(code: 'connectivityProfile.packetLoss.label', default: 'Packet Loss')}" />

                    </tr>
                </thead>
                <tbody>
                <g:each in="${connectivityProfileInstanceList}" status="i" var="connectivityProfileInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                        <td><g:link action="edit" id="${connectivityProfileInstance.id}">${connectivityProfileInstance.name}</g:link></td>

                        <td>${connectivityProfileInstance.bandwidthDown}</td>

                        <td>${connectivityProfileInstance.bandwidthUp}</td>

                        <td>${connectivityProfileInstance.latency}</td>

                        <td>${connectivityProfileInstance.packetLoss}</td>

                    </tr>
                </g:each>
                </tbody>
            </table>
            <div class="pagination">
                <bs:paginate total="${connectivityProfileInstanceTotal}" />
            </div>
        </section>

    </body>

</html>
