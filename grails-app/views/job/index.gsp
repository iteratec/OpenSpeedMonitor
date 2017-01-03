<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="de.iteratec.osm.measurement.schedule.Job" %>
<%@ page import="de.iteratec.osm.result.JobResult" %>

<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.isj.jobs"/></title>

    <g:set var="entityName" value="${message(code: 'de.iteratec.isj.job', default: 'Job')}"/>

    <asset:stylesheet src="job/list"/>

    <style>
    .dropdown-submenu {
        position: relative;
    }

    .dropdown-submenu .dropdown-menu {
        top: 0;
        left: 100%;
        margin-top: -1px;
    }
    </style>

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

    <div id="filterRow">
        <div class="btn-group" id="actionForSelectedContainer">
            <button class="btn btn-default dropdown-toggle" type="button" data-toggle="dropdown"
                    id="actionForSelected">
                <span class="caret"></span>
                <g:message code="job.list.actionForSelected" default="Action"/>
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
                <li class="dropdown-submenu">
                    <a class="test" tabindex="-1"
                       href="#">${message(code: "de.iteratec.isj.job.addTag", default: "add Tag")}</a>
                    <ul class="dropdown-menu">
                        <li>
                            <input id="add-tag-input" class="form-control" placeholder="tag">
                            <a href="#" id="add-tag-confirm-button" class="btn btn-primary"><i class="fa fa-check"></i>
                            </a>
                        </li>
                    </ul>
                </li>
                <li class="dropdown-submenu">
                    <a class="test" tabindex="-1"
                       href="#">${message(code: "de.iteratec.isj.job.removeTag", default: "remove Tag")}</a>
                    <ul class="dropdown-menu" id="remove-tag-dropdown">
                    </ul>
                </li>

                <li role="separator" class="divider"></li>
                <li>
                    <g:actionSubmit action="execute"
                                    value="${message(code: 'de.iteratec.isj.job.runonce', default: 'Run now')}"/>
                </li>
            </ul>
        </div>

        <div id="filterInputContainer">
            <input class="form-control" type="text" id="filterInput"
                   placeholder="<g:message code="job.list.filter" default="Jobs filtern"/>"
                   name="filter" value="${filters?.filter}"/>
            <span class="fa fa-times-circle" id="clearFilter"></span>
        </div>

        <div class="filterButtonsContainer">
            <span class="description">
                <g:message code="job.list.filter.by" default="by"/>
            </span>
            <span class="btn-group" data-toggle="buttons">
                <label class="btn btn-default">
                    <input type="checkbox" id="filterByName" name="filters.filterByName"/>
                    <g:message code="job.list.filter.by.name" default="Name"/>
                </label>
                <label class="btn btn-default">
                    <input type="checkbox" id="filterByScript" name="filters.filterByScript"/>
                    <g:message code="job.list.filter.by.script" default="Script"/>
                </label>
                <label class="btn btn-default">
                    <input type="checkbox" id="filterByJobGroup" name="filters.filterByJobGroup"/>
                    <g:message code="job.list.filter.by.group" default="Job Group"/>
                </label>
                <label class="btn btn-default">
                    <input type="checkbox" id="filterByLocation" name="filters.filterByLocation"/>
                    <g:message code="job.list.filter.by.location" default="Location"/>
                </label>
                <label class="btn btn-default">
                    <input type="checkbox" id="filterByBrowser" name="filters.filterByBrowser"/>
                    <g:message code="job.list.filter.by.browser" default="Browser"/>
                </label>
                <label class="btn btn-default">
                    <input type="checkbox" id="filterByTags" name="filters.filterByTags"/>
                    <g:message code="job.list.filter.by.tags" default="Tags"/>
                </label>
            </span>
        </div>

        <div class="filterButtonsContainer" id="filterShowOnly">
            <span class="description"><g:message code="job.list.filter.showOnly" default="Show only"/></span>

            <div class="btn-group" data-toggle="buttons">
                <label class="btn btn-default active">
                    <input type="checkbox" id="filterActiveJobs" name="filters.filterActiveJobs" checked/>
                    <g:message code="job.list.filter.activeJobs"/>
                </label>
                <label class="btn btn-default">
                    <input type="checkbox" id="filterCheckedJobs" name="filters.filterCheckedJobs"/>
                    <g:message code="job.list.filter.checkedJobs"/>
                </label>
                <label class="btn btn-default">
                    <input type="checkbox" id="filterHighlightedJobs" name="filters.filterHighlightedJobs"/>
                    <g:message code="job.list.filter.highlightedJobs"/>
                </label>
                <label class="btn btn-default">
                    <input type="checkbox" id="filterRunningJobs" name="filters.filterRunningJobs"/>
                    <g:message code="job.list.filter.runningJobs"/>
                </label>
            </div>
        </div>

        <div id="createJobContainer">
            <a href="<g:createLink action="create"/>" class="btn btn-primary pull-right">
                <i class="fa fa-plus"></i> <g:message code="default.create.label" args="[entityName]"/>
            </a>
        </div>
    </div>

    <div id="spinner-joblist" class="spinner-large-content-spinner"></div>
    <table class="table" id="jobtable">
        <thead class="header">
        <tr>
            <th><input type="checkbox" id="checkAll"/></th>
            <g:set var="titleHeader">
                <g:message code="de.iteratec.isj.job" default="Job"/><br/>
                <span class="script"><g:message
                        code="de.iteratec.iss.script" default="Skript"/>
                </span>
            </g:set>
            <g:set var="executionScheduleLabel">
                <abbr title="<g:message code="job.executionSchedule.hint" default="Cron String"/>">
                    <g:message code="job.executionSchedule.label" default="Execution Schedule"/>
                </abbr>
            </g:set>
            <g:sortableColumn property="label" title="${titleHeader}"/>
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
