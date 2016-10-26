<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="de.iteratec.osm.batch.BatchActivity" %>
<!doctype html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart_osm"/>
    <g:set var="entityName"
           value="${message(code: 'de.iteratec.osm.batch.batchactivity.list.heading', default: 'Batch Activities')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<h3><g:message code="de.iteratec.osm.batch.batchactivity.list.heading" default="Batch Activities"/></h3>

<g:form>
    <g:if test="${!dbCleanupEnabled}">
        <div class="alert alert-block">
            <h4><g:message code="de.iteratec.osm.batch.gui.warningdisabled.header" default="Warning!"/></h4>
            <g:message
                    code="de.iteratec.osm.batch.gui.warningdisabled.content"
                    default="Nightly Database cleanup is disabled!"/>
            <br>
            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SUPER_ADMIN">
                <g:actionSubmit class="btn btn-small btn-warning" action="activateDatabaseCleanup"
                                value="${message(code: 'de.iteratec.osm.batch.cleanup.activation.label', default: 'Activate nightly cleanup')}"/>
            </sec:ifAnyGranted>
        </div>
    </g:if>
</g:form>

<p>
    <g:message code="de.iteratec.osm.batch.batchactivity.list.description"
               default="A list of all larger activities and their current status."/>
</p>

<div class="container">
    <div class="row">
        <div class="span3">
            <div class="form-group">
                <br>
                <input placeholder="${message(code: 'de.iteratec.osm.batch.filter.label', default: 'Filter by name')}" type="text" class="form-control" id="elementFilter">
            </div>
        </div>
        <div class="span6">
            <div align="center" class="pagination"><ul id="elementPager"></ul></div>
        </div>
        <div class="span3">
            <div align="right" class="form-group">
                <label  for="elementsPerPage">${message(code: 'de.iteratec.osm.batch.batchactivity.batchesPerPage.label', default: 'Batches per page:')}</label>
                <input type="Number" class="form-control" id="elementsPerPage" value=100 min = 1>
            </div>
        </div>
    </div>
</div>
<div class="container">
    <div class="row">
        <div class="span3">
            <label class="checkbox inline">
            <g:checkBox name="filterBatchesByActiveCheckbox" id="filterBatchesByActiveCheckbox"
                        checked="${false}" value="${false}"/>
            <g:message code="de.iteratec.osm.batch.onlyOpenActivities.label"
                       default="Show only open activities"/>
            </label>
        </div>
        <div style="display: none;" id="limitResultsCheckboxContainer">
            <div class="span3">
                <label class="checkbox inline">
                <g:checkBox name="limitResultsCheckbox" id="limitResultsCheckbox"
                            checked="${true}" value="${true}"/>
                <g:message code="de.iteratec.osm.responsiveTable.limitedResults.lable"
                           default="Show only 1000 elements"/>
                </label>
            </div>
        </div>
    </div>
</div>
<br>
<br>
<section id="list-batchActivity" class="first">
    <div id="elementTable">
        <g:render template="batchActivityTable"
                  model="${['batchActivities': batchActivities, 'batchActivityCount': batchActivityCount]}"/>
    </div>
</section>

<g:form>
    <g:if test="${dbCleanupEnabled}">
        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_SUPER_ADMIN">
            <g:actionSubmit class="btn btn-small btn-info" action="deactivateDatabaseCleanup"
                            value="${message(code: 'de.iteratec.osm.batch.cleanup.deactivation.label', default: 'Deactivate nightly cleanup')}"/>
        </sec:ifAnyGranted>
    </g:if>
</g:form>
<content tag="include.bottom">
    <asset:javascript src="batchactivity/batchActivityList.js"/>
    <asset:script type="text/javascript">
        $(document).ready(
            OpenSpeedMonitor.batchActivity.init( '${createLink(action: 'updateTable', absolute: true)}',
                    '${createLink(action: 'checkForUpdate', absolute: true)}',
                    '${createLink(action: 'getUpdate', absolute: true)}',
                    '${batchActivityCount}',
                    {"next":"${message(code: 'de.iteratec.osm.batch.next.label', default: 'Next')}",
                    "previous":"${message(code: 'de.iteratec.osm.batch.previous.label', default: 'Previous')}"},
                    "startDate")
        );

    </asset:script>
</content>
</body>

</html>
