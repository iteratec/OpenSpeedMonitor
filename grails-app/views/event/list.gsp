<%@ page import="de.iteratec.osm.report.chart.Event" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart"/>
    <g:set var="entityName" value="${message(code: 'event.label', default: 'Event')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>

<section id="list-event" class="first">
    <div class="row">
        <div class="span12">
            <g:if test="${errorList && !errorList.empty}">
                <div class="alert alert-error">
                    <strong><g:message code="de.iteratec.isocsi.CsiDashboardController.selectionErrors.title"/></strong>
                    <ul>
                        <g:each in="${errorList}">
                            <li><g:message error = "${it}"/></li>
                        </g:each>
                    </ul>
                </div>
            </g:if>
        </div>
    </div>
    <form class="form-inline">
        <div class="form-group">
            <input id="filterListEventsByDate" name="filterDate" value="${filterDate}" type="text" placeholder="${message(code: 'event.tableFilter.date.placeholder', default: 'Filter by date')}">
            <input id="filterListEventsByTime" name="filterTime" value="${filterTime}" type="text" placeholder="${message(code: 'event.tableFilter.time.placeholder', default: 'Filter by time')}">
            <input id="filterListEventsByName" name="filterName" value="${filterName}" type="text" placeholder="${message(code: 'event.tableFilter.name.placeholder', default: 'Filter by short name')}">
            <input id="filterListEventsByDescription" name="filterDescription" value="${filterDescription}" type="text" placeholder="${message(code: 'event.tableFilter.description.placeholder', default: 'Filter by description')}">
            <g:actionSubmit class="btn btn-primary" action="list" value="${message(code: 'default.button.filter.label', default: 'Filter')}" />
        </div>
    </form>

    <table class="table table-bordered">
        <thead>
        <tr>

            <g:sortableColumn property="date" title="${message(code: 'event.date.label', default: 'Date')}"/>

            <g:sortableColumn property="date" title="${message(code: 'event.time.label', default: 'Time')}"/>

            <g:sortableColumn property="shortName"
                              title="${message(code: 'event.shortName.label', default: 'Short Name')}"/>

            <g:sortableColumn property="description"
                              title="${message(code: 'event.description.label', default: 'Description')}"/>

            <g:sortableColumn property="globallyVisible"
                              title="${message(code: 'event.globallyVisible.label', default: 'Globally Visible')}"/>

        </tr>
        </thead>
        <tbody>
        <g:each in="${eventInstanceList}" status="i" var="eventInstance">
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                <td><g:link action="show" id="${eventInstance.id}"><g:formatDate date="${eventInstance?.eventDate}"
                                                                                 formatName="default.date.format.short"/></g:link></td>

                <td>${(eventInstance?.eventDate?.getHours() as String).padLeft(2, "0") + ":" + (eventInstance?.eventDate?.minutes as String).padLeft(2, "0")}</td>

                <td>${fieldValue(bean: eventInstance, field: "shortName")}</td>

                <td>${fieldValue(bean: eventInstance, field: "description")}</td>

                <td><g:formatBoolean boolean="${eventInstance.globallyVisible}"/></td>

            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <bs:paginate total="${eventInstanceTotal}"/>
    </div>
</section>

</body>

</html>