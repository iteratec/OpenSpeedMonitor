<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="kickstart_osm"/>
    <title><g:message code="de.iteratec.osm.failedJobResults.title" default="Failed JobResults"/></title>
</head>

<body>
<div class="row">
    <div class="col-md-12">
        <%-- heading --%>
        <h3><g:message code="de.iteratec.osm.failedJobResults.title" default="Failed JobResults"/></h3>

        <p>
            <g:message code="de.iteratec.osm.failedJobResults.description.short"
                       default="The failed tests of the last 150 job runs"/>
        </p>
    </div>
</div>

<div class="card form-horizontal panel-body" id="failedMeasurementsOverview">
    <div class="form-group">
        <div class="col-md-3">
            <g:select id="selectedJob" class="form-control chosen-select" name="selectedJob"
                      from="${allJobs}"
                      optionValue="value"
                      optionKey="key"
                      noSelection="['': '- Choose a Job -']"></g:select>
        </div>
    </div>

    <table class="table table-striped hidden" id="jobResultTable">
        <thead>
        <tr>
            <th class="sortable"><g:message code="de.iteratec.osm.failedJobResults.thead.testId"
                                            default="Test ID"/></th>
            <th><g:message code="de.iteratec.osm.failedJobResults.thead.date" default="Date"/></th>
            <th><g:message code="de.iteratec.osm.failedJobResults.thead.jobResultStatus" default="Status"/></th>
            <th><g:message code="de.iteratec.osm.failedJobResults.thead.wptStatus" default="WPT Status"/></th>
            <th><g:message code="de.iteratec.osm.failedJobResults.thead.description" default="Description"/></th>
        </tr>
        </thead>
        <tbody>
        </tbody>
    </table>

    <div id="no-failed-results-hint" class="hidden">
        <g:message code="de.iteratec.osm.failedJobResults.noData" default="No data"/>
    </div>
</div>

<content tag="include.bottom">
    <asset:javascript src="jobResult/failedJobResultList.js"/>
    <asset:script>
        var url = '${createLink(controller: 'jobResult', action: 'getJobResults')}'
        OpenSpeedMonitor.jobResult(url);

        if ('${selectedJobId}') {
            $("#selectedJob").val(${selectedJobId});
            $('#selectedJob').trigger("chosen:updated");
            $('#selectedJob').trigger('change');
        }
    </asset:script>
</content>
</body>
</html>
