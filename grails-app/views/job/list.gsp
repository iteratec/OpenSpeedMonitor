<%@ page contentType="text/html;charset=UTF-8"%>
<%@ page import="de.iteratec.osm.measurement.schedule.Job"%>
<%@ page import="de.iteratec.osm.result.JobResult"%>

<html>
<head>
	<meta name="layout" content="kickstart_osm" />
	<title><g:message code="de.iteratec.isj.jobs" /></title>
	
	<g:set var="entityName"	value="${message(code: 'de.iteratec.isj.job', default: 'Job')}" />
	
	<r:require modules="prettycron, chosen, joblist, future-only-timeago, spin"/>
	
	<g:if test="${org.springframework.web.servlet.support.RequestContextUtils.getLocale(request).language.equals('de')}">
		<r:use modules="timeago-de" />
	</g:if>
	
	<style>
	.running {
		background: center left no-repeat url('<g:resource dir="images" file="loading_indicator.gif" absolute="true" />');
		padding-left: 20px;
	}
	</style>
	
</head>
<body>
	<%-- main menu --%>
	<g:render template="/layouts/mainMenu" />
	<div>
	<g:form>
		<div class="controlribbon">
			<g:if test="${!measurementsEnabled}">
				<div class="alert alert-block">
					<h4><g:message code="de.iteratec.osm.measurement.schedule.gui.warningdisabled.header" default="Warning!"/></h4>
					<g:message 
						code="de.iteratec.osm.measurement.schedule.gui.warningdisabled.content" 
						default="Measurements are generally disabled! Even active jobs won't get started until measurements are generally enabled again. Ask your administrator for activation."/>
				</div>
				<%-- 
				here should be a link for enabling measurements generally  
				<g:createLink absolute="true" ... />
				--%>
			</g:if>
			<g:else>
			<%-- 
			here should be a link for disabling measurements generally  
			<g:createLink absolute="true" ... />
			--%>
			</g:else>
			<p>
				<g:message code="de.iteratec.osm.gui.job.list.description" default="" />
				<a href="#" id="updateHints" class="icon-question-sign icon-large" rel="popover"
				data-placement="bottom" data-content="${render(template: "updateHints")}" data-html="true"></a>
			</p>
			<div class="row noIdent">
				<h5><g:message code="de.iteratec.sri.wptrd.jobs.filter.heading" default="Jobs filtern"/></h5>
				<input class="span2" type="text" onkeyup="filterJobList()" oninput="filterJobList()" id="filterByLabel"
					placeholder="<g:message code="Job.list.filter" default="Jobs filtern" />"
					name="filters.filterByLabel" value="${filters?.filterByLabel}" />
				<input class="span2" type="text" onkeyup="filterJobList()" oninput="filterJobList()" id="filterByJobGroup"
					placeholder="<g:message code="Job.list.filterByGroup" default="Job-Gruppen filtern" />" 
					name="filters.filterByJobGroup" value="${filters?.filterByJobGroup}" />
				<input class="span2" type="text" onkeyup="filterJobList()" oninput="filterJobList()" id="filterByLocation"
					placeholder="<g:message code="Job.list.filterByLocation" default="Locations filtern" />" 
					name="filters.filterByLocation" value="${filters?.filterByLocation}" />
				<select id="filterTags" multiple data-placeholder="${message(code: 'job.filter.filterTags')}" class="chosen-select span2"
					name="filters.filterTags">
				<g:each in="${Job.allTags}">
					<option ${ filters && it in filters.filterTags ? 'selected' : '' }>${it}</option>
				</g:each>
				</select>
			</div>
      <div class="row noIdent">
        <input class="span2" type="text" onkeyup="filterJobList()" oninput="filterJobList()" id="filterBySkript"
        placeholder="<g:message code="Job.list.filterBySkript" default="Nach Skriptname filtern" />"
        name="filters.filterBySkript" value="${filters?.filterBySkript}" />
        <input class="span2" type="text" onkeyup="filterJobList()" oninput="filterJobList()" id="filterByJobGroup"
          placeholder="<g:message code="de.iteratec.isj.Job.list.filterByGroup" default="Job-Gruppen filtern" />" 
          name="filters.filterByJobGroup" value="${filters?.filterByJobGroup}" style="visibility: hidden; "/>
        <input class="span2" type="text" onkeyup="filterJobList()" oninput="filterJobList()" id="filterByBrowser"
        placeholder="<g:message code="Job.list.filterByBrowser" default="Browser filtern" />" 
        name="filters.filterByBrowser" value="${filters?.filterByBrowser}" />
      </div>
			<div>
			<g:checkBox onchange="filterJobList()"
				id="filterCheckedJobs" class="checkbox inline"
				name="filters.filterCheckedJobs" value="${filters?.filterCheckedJobs}" /><label for="filterCheckedJobs"><g:message
					code="job.filter.filterCheckedJobs" /></label> 
			<g:checkBox
				onchange="filterJobList()" id="filterHighlightedJobs" class="checkbox inline"
				name="filters.filterHighlightedJobs" value="${filters?.filterHighlightedJobs}" /><label
				for="filterHighlightedJobs"><g:message
					code="job.filter.filterHighlightedJobs" /></label> 
			<g:checkBox
				onchange="filterJobList()" id="filterRunningJobs" class="checkbox inline"
				name="filters.filterRunningJobs" value="${filters?.filterRunningJobs}" /><label for="filterRunningJobs"><g:message
					code="job.filter.filterRunningJobs" /></label>
			<g:checkBox
				onchange="filterJobList()" id="filterInactiveJobs" class="checkbox inline"
				name="filters.filterInactiveJobs" value="${filters?.filterInactiveJobs}" /><label
				for="filterInactiveJobs"><g:message
					code="job.filter.filterInactiveJobs" /></label> 
			</div>
		</div>
		<div class="controlribbon">
			<g:message code="de.iteratec.isj.job.selected"
				default="Markierte Jobs" />

			<g:actionSubmit action="activate" class="btn btn-default"
				value="${ message(code: 'de.iteratec.isj.job.activate', default: 'Aktivieren') }" />
			<g:actionSubmit action="deactivate" class="btn btn-default"
				value="${ message(code: 'de.iteratec.isj.job.deactivate', default: 'Deaktivieren') }" />
			<g:actionSubmit class="btn btn-info" action="execute"
				value="${message(code: 'de.iteratec.isj.job.runonce', default: 'Run now')}" />
		</div>
		<div class="controlribbon">
			<a
				href="<g:createLink action="create" />" class="btn btn-primary">
				<i class="icon-plus"></i> <g:message code="default.create.label" args="[entityName]" />
			</a>
		</div>
		
		
		<div class="alert" id="serverdown">  
		  <a class="close" data-dismiss="alert">×</a>  
		  <g:message code="job.getRunningAndRecentlyFinishedJobs.error"/>  
		</div> 
			<table class="table-striped table-fixed-header" id="jobtable">
				<thead class="header">
					<tr>
						<th><input type="checkbox" id="checkAll" /></th>
						<g:set var="titleHeader"><g:message code="de.iteratec.isj.job" default="Job" /><br />
							<span style="font-weight: normal"><g:message
									code="de.iteratec.iss.script" default="Skript" /></span></g:set>
						<g:sortableColumn property="label" title="${titleHeader}" />
						<g:sortableColumn property="jobGroup.name" titleKey="job.jobGroup.label" />
						<g:sortableColumn property="location.uniqueIdentifierForServer" titleKey="job.location.label" />
						<g:sortableColumn property="location.browser.name" titleKey="browser.label" />
						<g:sortableColumn property="lastRun" titleKey="job.lastRun.label" title="Zuletzt ausgeführt" />
						<g:sortableColumn property="nextExecutionTime" titleKey="job.nextRun.label" />
						<g:sortableColumn property="executionSchedule" titleKey="job.executionSchedule.label" />
						<g:sortableColumn property="runs" titleKey="job.runs.label" title="Runs" />
						<g:sortableColumn property="firstViewOnly" titleKey="job.2x.label" title="Runs" />
					</tr>
				</thead>
				<g:render template="jobTable" model="${['jobs': jobs, 'jobsWithTags': jobsWithTags]}" />
			</table>
	</g:form>
	</div>
	<r:script>
		$(document).ready(
			doOnDomReady(
				'${createLink(action: 'getRunningAndRecentlyFinishedJobs', absolute: true)}',
				'${createLink(action: 'cancelJobRun', absolute: true)}',
				'${createLink(action: 'getLastRun', absolute: true)}',
				${onlyActiveJobs},
				"${createLink(action: 'nextExecution', absolute: true)}",
				'${createLink(action: 'list', absolute: true)}'
			)
		);
	</r:script>
</body>
</html>