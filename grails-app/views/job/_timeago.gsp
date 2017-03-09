<%@ page import="org.joda.time.DateTime; org.quartz.CronExpression"%>
<%@ page import="de.iteratec.osm.measurement.schedule.CronExpressionFormatter" %>
<%@ page import="groovy.time.*" %>

<g:if test="${cronstring}">
	<g:if test="${CronExpression.isValidExpression(cronstring)}">
        <g:set var="cronExpression" value="${ new CronExpression(cronstring) }" />
        <g:set var="date" value="${ CronExpressionFormatter.getNextValidTimeAfter(cronExpression, new Date()) }" />
	</g:if>
</g:if>

<g:if test="${date}">
	<g:set var="dateDiffMs" value="${ TimeCategory.minus(date, new Date()).toMilliseconds() }" />
	<g:set var="nextRunISO" value="${ date.format("yyyy-MM-dd'T'HH:mm:ssZ")}" />
	${prepend}
		<g:if test="${url}">
			<a href="${url}">
		</g:if>
		 <abbr class="timeago" title="${nextRunISO}"
            data-date-diff-ms="${dateDiffMs}"
            <g:if test="${cronstring}">
                data-cronstring="${cronstring}"
            </g:if>>
         </abbr>
		<g:if test="${url}">
			</a>
		</g:if>
</g:if>
<g:else>
	${defaultmessage}
</g:else>