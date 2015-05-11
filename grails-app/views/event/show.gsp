<%@ page import="de.iteratec.osm.report.chart.Event" %>
<!doctype html>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart"/>
    <g:set var="entityName" value="${message(code: 'event.label', default: 'Event')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>

<section id="show-event" class="first">

    <table class="table">
        <tbody>

        <tr class="prop">
            <td valign="top" class="name"><g:message code="event.date.label" default="Date"/></td>

            <td valign="top" class="value"><g:formatDate date="${eventInstance?.eventDate}"
                                                         formatName="default.date.format.short"/></td>

        </tr>

        <tr class="prop">
            <td valign="top" class="name"><g:message code="event.time.label" default="Time"/></td>

            <td valign="top"
                class="value">${(eventInstance?.eventDate?.getHours() as String).padLeft(2, "0") + ":" + (eventInstance?.eventDate?.minutes as String).padLeft(2, "0")}</td>

        </tr>

        <tr class="prop">
            <td valign="top" class="name"><g:message code="event.shortName.label" default="Short Name"/></td>

            <td valign="top" class="value">${fieldValue(bean: eventInstance, field: "shortName")}</td>

        </tr>

        <tr class="prop">
            <td valign="top" class="name"><g:message code="event.htmlDescription.label"
                                                     default="Html Description"/></td>

            <td valign="top" class="value"><markdown:renderHtml
                    text="${fieldValue(bean: eventInstance, field: "htmlDescription")}"/></td>

        </tr>

        <tr class="prop">
            <td valign="top" class="name"><g:message code="event.globallyVisible.label"
                                                     default="Globally Visible"/></td>

            <td valign="top" class="value"><g:formatBoolean boolean="${eventInstance?.globallyVisible}"/></td>

        </tr>

        <tr class="prop">
            <td valign="top" class="name"><g:message code="event.jobGroup.label" default="Job Group"/></td>

            <td valign="top" style="text-align: left;" class="value">
                <ul>
                    <g:each in="${eventInstance.jobGroup}" var="j">
                        <li><g:link controller="jobGroup" action="show" id="${j.id}">${j?.encodeAsHTML()}</g:link></li>
                    </g:each>
                </ul>
            </td>

        </tr>

        </tbody>
    </table>
</section>

</body>

</html>
