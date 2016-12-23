<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="de.iteratec.osm.measurement.schedule.Job" %>
<%@ page import="de.iteratec.osm.result.JobResult" %>

<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isj.jobs"/></title>

    <g:set var="entityName" value="${message(code: 'de.iteratec.isj.job', default: 'Job')}"/>

    <asset:stylesheet src="job/list"/>

</head>

<body>
<%-- main menu --%>
<g:render template="/layouts/mainMenu" model="${[help: render(template: "updateHints")]}"/>
        <g:form>
            <g:if test="${!measurementsEnabled}">
                <div class="alert alert-warning">
                    <h4><g:message code="de.iteratec.osm.measurement.schedule.gui.warningdisabled.header"
                                   default="Warning!"/></h4>
                    <g:message
                            code="de.iteratec.osm.measurement.schedule.gui.warningdisabled.content"
                            default="Measurements are generally disabled! Even active jobs won't get started until measurements are generally enabled again. Ask your administrator for activation."/>
                    <br>
                    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SUPER_ADMIN">
                        <g:actionSubmit class="btn btn-sm btn-warning" action="activateMeasurementsGenerally"
                                        value="${message(code: 'de.iteratec.osm.measurement.schedule.general.activation.label', default: 'Activate measurements')}"/>
                    </sec:ifAnyGranted>
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
            <g:if test="${saveError}">
                <div class="row">
                    <div class="col-md-12">
                        <div class="alert alert-danger">
                            <g:message error="${saveError}"/>
                        </div>
                    </div>
                </div>
            </g:if>
            <g:if test="${saveSuccess}">
                <div class="row">
                    <div class="col-md-12">
                        <div class="alert alert-success">${saveSuccess}</div>
                    </div>
                </div>
            </g:if>
            <div class="alert alert-warning" id="serverdown">
                <a class="close" data-dismiss="alert">×</a>
                <g:message code="job.getRunningAndRecentlyFinishedJobs.error"/>
            </div>
            <div class="row section">
                <div class="col-md-1">
                    <div class="btn-group" id="show-button-group">
                        <button class="btn btn-default dropdown-toggle" type="button" data-toggle="dropdown">
                            <span class="caret"></span>
                            <g:message code="de.iteratec.isj.job.selected" default="Markierte Jobs"/>
                        </button>
                        <ul class="dropdown-menu">
                            <li>
                                <g:actionSubmit action="activate"
                                                value="${message(code: 'de.iteratec.isj.job.activate', default: 'Aktivieren')}"/>
                            </li>
                            <li>
                                <g:actionSubmit action="deactivate"
                                                value="${message(code: 'de.iteratec.isj.job.deactivate', default: 'Deaktivieren')}"/>
                            </li>
                            <li role="separator" class="divider"></li>
                            <li>
                                <g:actionSubmit action="execute"
                                                value="${message(code: 'de.iteratec.isj.job.runonce', default: 'Run now')}"/>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="col-md-7">
                    <div class="input-group">
                        <input class="form-control" type="text" onkeyup="filterJobList()" oninput="filterJobList()" id="filterByLabel"
                               placeholder="<g:message code="hob.list.filter" default="Jobs filtern"/>"
                               name="filters.filterByLabel" value="${filters?.filterByLabel}"/>
                        <span class="input-group-addon"><g:message code="job.list.filter.by" default="by"/></span>
                        <span class="input-group-btn" data-toggle="buttons">
                            <label class="btn btn-default">
                                <g:checkBox onchange="filterJobList()" id="filterByName"
                                            name="filters.filterCheckedJobs" value="${filters?.filterCheckedJobs}"/>
                                <g:message code="job.list.filter.by.name" default="Name" />
                            </label>
                            <label class="btn btn-default">
                                <g:checkBox onchange="filterJobList()" id="filterByScript"
                                            name="filters.filterCheckedJobs" value="${filters?.filterCheckedJobs}"/>
                                <g:message code="job.list.filter.by.script" default="Script" />
                            </label>
                            <label class="btn btn-default">
                                <g:checkBox onchange="filterJobList()" id="filterByJobGroup"
                                            name="filters.filterCheckedJobs" value="${filters?.filterCheckedJobs}"/>
                                <g:message code="job.list.filter.by.group" default="Job Group" />
                            </label>
                            <label class="btn btn-default">
                                <g:checkBox onchange="filterJobList()" id="filterByJobGroup"
                                            name="filters.filterCheckedJobs" value="${filters?.filterCheckedJobs}"/>
                                <g:message code="job.list.filter.by.location" default="Location" />
                            </label>
                            <label class="btn btn-default">
                                <g:checkBox onchange="filterJobList()" id="filterByJobGroup"
                                            name="filters.filterCheckedJobs" value="${filters?.filterCheckedJobs}"/>
                                <g:message code="job.list.filter.by.browser" default="Browser" />
                            </label>
                            <label class="btn btn-default">
                                <g:checkBox onchange="filterJobList()" id="filterByJobGroup"
                                            name="filters.filterCheckedJobs" value="${filters?.filterCheckedJobs}"/>
                                <g:message code="job.list.filter.by.tags" default="Tags" />
                            </label>
                        </span>
                    </div>
                </div>
                <div class="col-md-3">
                    <label><g:message code="job.list.filter.showOnly" default="Show only" /></label>
                    <div class="btn-group" data-toggle="buttons">
                        <label class="btn btn-default">
                            <g:checkBox onchange="filterJobList()" id="filterCheckedJobs" class="checkbox-inline"
                                name="filters.filterCheckedJobs" value="${filters?.filterCheckedJobs}"/>
                            <g:message code="job.list.filter.checkedJobs"/>
                        </label>
                        <label class="btn btn-default">
                            <g:checkBox onchange="filterJobList()" id="filterHighlightedJobs" class="checkbox-inline"
                                        name="filters.filterHighlightedJobs" value="${filters?.filterHighlightedJobs}"/>
                            <g:message code="job.list.filter.highlightedJobs"/>
                        </label>
                        <label class="btn btn-default">
                            <g:checkBox onchange="filterJobList()" id="filterRunningJobs" class="checkbox-inline"
                                        name="filters.filterRunningJobs" value="${filters?.filterRunningJobs}"/>
                            <g:message code="job.list.filter.runningJobs"/>
                        </label>
                        <label class="btn btn-default active">
                            <g:checkBox onchange="filterJobList()" id="filterInactiveJobs" class="checkbox-inline"
                                        name="filters.filterInactiveJobs" value="${filters?.filterInactiveJobs}"/>
                            <g:message code="job.list.filter.activeJobs"/>
                        </label>
                    </div>
                </div>
                <div class="col-md-1">
                    <a href="<g:createLink action="create"/>" class="btn btn-primary pull-right">
                        <i class="fa fa-plus"></i> <g:message code="default.create.label" args="[entityName]"/>
                    </a>
                </div>
            </div>
            <div id="spinner-joblist" class="spinner-large-content-spinner"></div>
            <table class="table table-striped" id="jobtable">
                <thead class="header">
                <tr>
                    <th><input type="checkbox" id="checkAll"/></th>
                    <g:set var="titleHeader">
                        <g:message code="de.iteratec.isj.job" default="Job"/><br/>
                        <span style="font-weight: normal"><g:message
                                code="de.iteratec.iss.script" default="Skript"/>
                        </span>
                    </g:set>
                    <g:set var="executionScheduleLabel">
                        <g:message code="job.executionSchedule.label" default="Execution Schedule"/>
                    </g:set>
                    <g:sortableColumn property="label" title="${titleHeader}" />
                    <g:sortableColumn property="jobGroup.name" titleKey="job.jobGroup.label"/>
                    <g:sortableColumn property="location.uniqueIdentifierForServer" titleKey="job.location.label"/>
                    <g:sortableColumn property="location.browser.name" titleKey="browser.label"/>
                    <g:sortableColumn property="lastRun" titleKey="job.lastRun.label" title="Zuletzt ausgeführt"/>
                    <g:sortableColumn property="nextExecutionTime" titleKey="job.nextRun.label"/>
                    <g:sortableColumn property="executionSchedule" title="${executionScheduleLabel}"/>
                    <g:sortableColumn property="runs" titleKey="job.runs.label" title="Runs"/>
                    <g:sortableColumn property="firstViewOnly" titleKey="job.2x.label" title="Runs"/>
                </tr>
                </thead>
                <g:render template="jobTable" model="${['jobs': jobs, 'jobsWithTags': jobsWithTags]}"/>
            </table>
            </div>
        </g:form>
<content tag="include.bottom">
    <asset:javascript src="prettycron/prettycronManifest.js"/>
    <asset:javascript src="job/jobList.js"/>
    <asset:javascript src="timeago/futureOnlyTimeago.js"/>
    <g:if test="${org.springframework.web.servlet.support.RequestContextUtils.getLocale(request).language.equals('de')}">
        <asset:javascript src="timeago/timeagoDe.js"/>
    </g:if>
    <asset:script type="text/javascript">
        $(document).ready(
            doOnDomReady(
                '${createLink(action: 'getRunningAndRecentlyFinishedJobs', absolute: true)}',
                '${createLink(action: 'cancelJobRun', absolute: true)}',
                '${createLink(action: 'getLastRun', absolute: true)}',
                "${createLink(action: 'nextExecution', absolute: true)}"
            )
        );
        $(window).load(function() {
            doOnWindowLoad(
                '${createLink(action: 'list', absolute: true)}',
                "${createLink(action: 'nextExecution', absolute: true)}"
            );
        });
    </asset:script>
</content>
</body>
</html>
