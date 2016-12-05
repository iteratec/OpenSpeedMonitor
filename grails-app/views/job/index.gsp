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
<g:render template="/layouts/mainMenu"/>
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
            <legend><g:message code="de.iteratec.sri.wptrd.jobs.filter.heading" default="Jobs filtern" class="control-label"/></legend>
            <div class="row">
                <div class="col-md-3">
                    <input class="form-control" type="text" onkeyup="filterJobList()" oninput="filterJobList()" id="filterByLabel"
                           placeholder="<g:message code="Job.list.filter" default="Jobs filtern"/>"
                           name="filters.filterByLabel" value="${filters?.filterByLabel}"/>
                </div>
                <div class="col-md-3">
                    <input class="form-control" type="text" onkeyup="filterJobList()" oninput="filterJobList()"
                           id="filterByJobGroup"
                           placeholder="<g:message code="Job.list.filterByGroup" default="Job-Gruppen filtern"/>"
                           name="filters.filterByJobGroup" value="${filters?.filterByJobGroup}"/>
                </div>
                <div class="col-md-3">
                    <input class="form-control" type="text" onkeyup="filterJobList()" oninput="filterJobList()"
                           id="filterByLocation"
                           placeholder="<g:message code="Job.list.filterByLocation" default="Locations filtern"/>"
                           name="filters.filterByLocation" value="${filters?.filterByLocation}"/>
                </div>
                <div class="col-md-3">
                    <select id="filterTags" multiple data-placeholder="${message(code: 'job.filter.filterTags')}"
                            class="chosen-select form-control"
                            name="filters.filterTags">
                        <g:each in="${Job.allTags}">
                            <option ${filters && it in filters.filterTags ? 'selected' : ''}>${it}</option>
                        </g:each>
                    </select>
                </div>
            </div>

            <div class="row">
                <div class="col-md-3">
                    <input class="form-control" type="text" onkeyup="filterJobList()" oninput="filterJobList()" id="filterBySkript"
                           placeholder="<g:message code="Job.list.filterBySkript" default="Nach Skriptname filtern"/>"
                           name="filters.filterBySkript" value="${filters?.filterBySkript}"/>
                </div>
                <div class="col-md-3 col-md-offset-3">
                    <input class="form-control" type="text" onkeyup="filterJobList()" oninput="filterJobList()"
                           id="filterByBrowser"
                           placeholder="<g:message code="Job.list.filterByBrowser" default="Browser filtern"/>"
                           name="filters.filterByBrowser" value="${filters?.filterByBrowser}"/>
                </div>
                %{--Filter by JobSet--}%
                <div class="col-md-3">
                    <div class="btn-group">
                        <div class="btn-group">
                            <button id="jobSetButton" class="btn-default btn btn-sm dropdown-toggle"
                                    type="button" data-toggle="dropdown">
                                <g:message code="de.iteratec.osm.job.filterHeadline" default="Filter by JobSet"/>
                                <span class="caret"></span>
                            </button>
                            <ul class="dropdown-menu">
                                <g:if test="${jobSets.size > 0}">
                                    <g:each in="${jobSets}" var="jobSet">
                                        <li><a id="${jobSet.name}" href="#"
                                               onclick="filterJobSet('${jobSet.name}', '${jobSet.jobs*.toString()}')">${jobSet.name}</a>
                                        </li>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <li><g:message code="de.iteratec.osm.job.filterNoJobSet" default="Kein JobSet"/> </li>
                                </g:else>
                            </ul>
                        </div>
                        <button type="button" class="btn-default btn btn-sm"
                                onclick="clearFilterJobSet('<g:message code="de.iteratec.osm.job.filterHeadline"
                                default="Filter by JobSet" />')">&#160;&times;</button>
                    </div>
                </div>
            </div>
            <div class="section-xl">
                <div class="checkbox-inline">
                    <g:checkBox onchange="filterJobList()" id="filterCheckedJobs" class="checkbox-inline"
                                name="filters.filterCheckedJobs" value="${filters?.filterCheckedJobs}"/>
                    <label for="filterCheckedJobs"><g:message code="job.filter.filterCheckedJobs"/></label>
                </div>
                <div class="checkbox-inline">
                    <g:checkBox onchange="filterJobList()" id="filterHighlightedJobs" class="checkbox-inline"
                                name="filters.filterHighlightedJobs" value="${filters?.filterHighlightedJobs}"/>
                    <label for="filterHighlightedJobs"><g:message code="job.filter.filterHighlightedJobs"/></label>
                </div>
                <div class="checkbox-inline">
                    <g:checkBox onchange="filterJobList()" id="filterRunningJobs" class="checkbox-inline"
                                name="filters.filterRunningJobs" value="${filters?.filterRunningJobs}"/>
                    <label for="filterRunningJobs"><g:message code="job.filter.filterRunningJobs"/></label>
                </div>
                <div class="checkbox-inline">
                    <g:checkBox onchange="filterJobList()" id="filterInactiveJobs" class="checkbox-inline"
                                name="filters.filterInactiveJobs" value="${filters?.filterInactiveJobs}"/>
                    <label for="filterInactiveJobs"><g:message code="job.filter.filterInactiveJobs"/></label>
                </div>
            </div>
            <div class="row table-filter">
                <div class="col-md-6">
                    <i class="fa fa-arrow-down"></i>
                    <g:message code="de.iteratec.isj.job.selected"
                               default="Markierte Jobs"/>:&nbsp;
                    <div class="btn-group">
                        <g:actionSubmit action="activate" class="btn btn-default"
                                        value="${message(code: 'de.iteratec.isj.job.activate', default: 'Aktivieren')}"/>


                        <g:actionSubmit action="deactivate" class="btn btn-default"
                                        value="${message(code: 'de.iteratec.isj.job.deactivate', default: 'Deaktivieren')}"/>
                        <g:actionSubmit class="btn btn-info" action="execute"
                                        value="${message(code: 'de.iteratec.isj.job.runonce', default: 'Run now')}"/>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="input-group">
                        <g:field type="text" name="jobSetName" class="form-control"
                                 placeholder="${message(code: 'de.iteratec.osm.job.savePlaceholder', default: 'placeholder')}"/>
                        <span class="input-group-btn">
                            <g:actionSubmit action="saveJobSet" class="btn btn-default"
                                            value="${message(code: 'de.iteratec.osm.job.jobSetSaveButton', default: 'save jobSet')}"/>
                        </span>
                    </div>
                </div>

                <div class="col-md-3">
                    <a href="<g:createLink action="create"/>" class="btn btn-primary">
                        <i class="fa fa-plus"></i> <g:message code="default.create.label" args="[entityName]"/>
                    </a>
                    <a href="#" id="updateHints" class="fa fa-question-circle fa-lg pull-right" data-toggle="popover"
                        title="${g.message(code: 'de.iteratec.osm.joblist.activeruns.title', default: 'Active job runs')}"
                       data-placement="bottom" data-content="${render(template: "updateHints")}" data-trigger="hover" data-html="true"></a>
                </div>
            </div>


            <div class="alert alert-warning" id="serverdown">
                <a class="close" data-dismiss="alert">×</a>
                <g:message code="job.getRunningAndRecentlyFinishedJobs.error"/>
            </div>
            <div id="spinner-joblist" class="spinner-large-content-spinner"></div>
            <table class="table table-striped" id="jobtable">
                <thead class="header">
                <tr>
                    <th><input type="checkbox" id="checkAll"/></th>
                    <g:set var="titleHeader"><g:message code="de.iteratec.isj.job" default="Job"/><br/>
                        <span style="font-weight: normal"><g:message
                                code="de.iteratec.iss.script" default="Skript"/>
                        </span>
                    </g:set>
                    <g:set var="executionScheduleLabel">
                        <g:message code="job.executionSchedule.label" default="Execution Schedule"/>
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