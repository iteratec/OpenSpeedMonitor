<%@ page import="de.iteratec.osm.csi.CsiSystem" %>
<!doctype html>
<html>

<head>
    <g:set var="entityName" value="${message(code: 'csiSystem.label', default: 'CsiSystem')}" scope="request"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="layoutOsm"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>

<section id="show-csiSystem" class="first">

    <table class="table">
        <tbody>

        <tr class="prop">
            <td valign="top" class="name"><g:message code="csiSystem.label.label" default="Label"/></td>

            <td valign="top" class="value">${fieldValue(bean: csiSystem, field: "label")}</td>

        </tr>

        <tr class="prop">
            <td valign="top" class="name"><g:message code="csiSystem.jobGroupWeights.label"
                                                     default="Job Group Weights"/></td>

            <td valign="top" style="text-align: left;" class="value">
                <table class="table">
                    <g:each in="${csiSystem.jobGroupWeights}" var="j">
                        <tr>
                            <td style="border: none">${j.jobGroup.name}</td>
                            <td style="border: none">${j.weight}</td>
                        </tr>
                    </g:each>
                </table>
            </td>

        </tr>

        </tbody>
    </table>
</section>

</body>

</html>
