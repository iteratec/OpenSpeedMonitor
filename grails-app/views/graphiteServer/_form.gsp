<%@ page import="de.iteratec.osm.report.external.GraphiteServer" %>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'serverAdress', 'error')} required">
    <label for="serverAdress" class="control-label col-md-3"><g:message code="graphiteServer.serverAdress.label" default="Server Adress" /><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:textField name="serverAdress" value="${graphiteServer?.serverAdress}" class="form-control"></g:textField>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'port', 'error')} required">
    <label for="port" class="control-label col-md-3"><g:message code="graphiteServer.port.label" default="Port" /><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:field name="port" type="number" min="0" value="${graphiteServer?.port}" class="form-control"></g:field>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'graphitePaths', 'error')} ">
    <label for="graphitePaths" class="control-label col-md-3"><g:message code="graphiteServer.graphitePaths.label" default="Graphite Paths"/></label>

    <div class="col-md-6">
        <g:select name="graphitePaths" from="${de.iteratec.osm.report.external.GraphitePath.list()}" multiple="multiple"
                  optionKey="id" size="5" value="${graphiteServer?.graphitePaths*.id}" class="many-to-many form-control"/>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'webappUrl', 'error')} required">
    <label for="webappUrl" class="control-label col-md-3"><g:message code="graphiteServer.webappUrl.label" default="Webapp Url" /><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:textField name="webappUrl" type="url" value="${graphiteServer?.webappUrl}" class="form-control"></g:textField>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'webappProtocol', 'error')} required">
    <label for="webappProtocol" class="control-label col-md-3"><g:message code="graphiteServer.webappProtocol.label" default="Webapp Protocol" /><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:select name="webappProtocol" from="${de.iteratec.osm.measurement.environment.wptserverproxy.Protocol.values()}"
                   value="${graphiteServer?.webappProtocol}" class="form-control"/>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'webappPathToRenderingEngine', 'error')} required">
    <label for="webappPathToRenderingEngine" class="control-label col-md-3"><g:message code="graphiteServer.webappPathToRenderingEngine.label" default="Webapp Path To Rendering Engine" /><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:textField name="webappPathToRenderingEngine" value="${graphiteServer?.webappPathToRenderingEngine}" class="form-control"></g:textField>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'reportHealthMetrics', 'error')}">
    <label for="reportHealthMetrics" class="control-label col-md-3"><g:message code="graphiteServer.reportHealthMetrics.label" default="Report Health Metrics" /></label>

    <div class="col-md-6">
        <bs:checkBox name="reportHealthMetrics" value="${graphiteServer?.reportHealthMetrics}" class="form-control"/>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'timeBetweenReportsInSeconds', 'error')} required">
    <label for="timeBetweenReportsInSeconds" class="control-label col-md-3"><g:message code="graphiteServer.timeBetweenReportsInSeconds.label" default="Time Between Reports In Seconds" /><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:field name="timeBetweenReportsInSeconds" type="number" min="0" value="${graphiteServer?.timeBetweenReportsInSeconds}" class="form-control"></g:field>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'healthMetricsReportPrefix', 'error')} required">
    <label for="healthMetricsReportPrefix" class="control-label col-md-3"><g:message code="graphiteServer.healthMetricsReportPrefix.label" default="Health Metrics Report Prefix" /><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:textField name="healthMetricsReportPrefix" value="${graphiteServer?.healthMetricsReportPrefix}" class="form-control"></g:textField>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'garbageCollectorPrefix', 'error')} required">
    <label for="garbageCollectorPrefix" class="control-label col-md-3"><g:message code="graphiteServer.garbageCollectorPrefix.label" default="Garbage Collector Prefix" /><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:textField name="garbageCollectorPrefix" value="${graphiteServer?.garbageCollectorPrefix}" class="form-control"></g:textField>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'memoryReportPrefix', 'error')} required">
    <label for="memoryReportPrefix" class="control-label col-md-3"><g:message code="graphiteServer.memoryReportPrefix.label" default="Memory Report Prefix" /><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:textField name="memoryReportPrefix" value="${graphiteServer?.memoryReportPrefix}" class="form-control"></g:textField>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'threadStatesReportPrefix', 'error')} required">
    <label for="threadStatesReportPrefix" class="control-label col-md-3"><g:message code="graphiteServer.threadStatesReportPrefix.label" default="Thread States Report Prefix" /><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:textField name="threadStatesReportPrefix" value="${graphiteServer?.threadStatesReportPrefix}" class="form-control"></g:textField>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'reportEventResultsToGraphiteServer', 'error')}">
    <label for="reportEventResultsToGraphiteServer" class="control-label col-md-3"><g:message code="graphiteServer.reportEventResultsToGraphiteServer.label" default="Report EventResults To GraphiteServer" /></label>

    <div class="col-md-6">
        <bs:checkBox name="reportEventResultsToGraphiteServer" value="${graphiteServer?.reportEventResultsToGraphiteServer}" class="form-control"/>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'processCpuLoadPrefix', 'error')} required">
    <label for="processCpuLoadPrefix" class="control-label col-md-3"><g:message code="graphiteServer.processCpuLoadPrefix.label" default="Process Cpu Load Prefix" /><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:textField name="processCpuLoadPrefix" value="${graphiteServer?.processCpuLoadPrefix}" class="form-control"></g:textField>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'reportCsiAggregationsToGraphiteServer', 'error')}">
    <label for="reportCsiAggregationsToGraphiteServer" class="control-label col-md-3"><g:message code="graphiteServer.reportCsiAggregationsToGraphiteServer.label" default="Rreport CsiAggregations To GraphiteServer" /></label>

    <div class="col-md-6">
        <bs:checkBox name="reportCsiAggregationsToGraphiteServer" value="${graphiteServer?.reportCsiAggregationsToGraphiteServer}" class="form-control"/>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: graphiteServer, field: 'graphiteEventSourcePaths', 'error')} ">
    <label for="graphiteEventSourcePaths" class="control-label col-md-3"><g:message code="graphiteServer.graphiteEventSourcePaths.label" default="Graphite Event Source Paths"/></label>

    <div class="col-md-6">
        <g:select name="graphiteEventSourcePaths" from="${de.iteratec.osm.report.external.GraphiteEventSourcePath.list()}" multiple="multiple"
                  optionKey="id" size="5" value="${graphiteServer?.graphiteEventSourcePaths*.id}" class="many-to-many form-control"/>
    </div>
</div>