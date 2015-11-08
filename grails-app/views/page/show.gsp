
<%@ page import="grails.converters.JSON; de.iteratec.osm.csi.Page" %>
<!doctype html>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="${message(code: 'page.label', default: 'Page')}" />
	<title><g:message code="default.show.label" args="[entityName]" /></title>
	%{--Styles for multi line chart--}%
	<style>
	.axis path,
	.axis line {
		fill: none;
		stroke: black;
		shape-rendering: crisp-edges ;
	}
	.line {
		fill: none;
		stroke-width: 2px;
	}
	.verticalLine,
	.horizontalLine {
		opacity: 0.3;
		stroke-dasharray: 3,3;
		stroke: blue;
	}
	.xTextContainer,
	.tooltipTextContainer{
		opacity: 0.5;
	}
	</style>
</head>

<body>

<section id="show-page" class="first">

	<table class="table">
		<tbody>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="page.name.label" default="Name" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: pageInstance, field: "name")}</td>
				
			</tr>
		
			<tr class="prop">
				<td valign="top" class="name"><g:message code="page.weight.label" default="Weight" /></td>
				
				<td valign="top" class="value">${fieldValue(bean: pageInstance, field: "weight")}</td>
				
			</tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="page.csimapping.existence.label" default="CSI-Mapping" /></td>

                <td valign="top" class="value">
                    <g:if test="${mappingsOfPage.size()>0}">
                        <g:render template="/chart/csi-mappings"
                                  model="${['chartData':multiLineChart,'transformableMappings': mappingsOfPage, 'chartIdentifier': 'show_page',
                                            'bottomOffsetXAxis': 216, 'yAxisRightOffset': 950, 'chartBottomOffset': 170,
                                            'yAxisTopOffset': 5, 'bottomOffsetLegend': 130]}"/>
                    </g:if>
                    <g:else>
                        <p><g:message code="page.csimapping.nonexistence" default="None associated"/></p>
                    </g:else>
                </td>


            </tr>

		</tbody>
	</table>
</section>

</body>

</html>
