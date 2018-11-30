<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<%@ defaultCodec="none" %>


<div class="form-group fieldcontain ${hasErrors(bean: jobGroup, field: 'name', 'error')} required">
    <label for="name" class="control-label col-md-3"><g:message code="jobGroup.name.label" default="Name"/><span
            class="required-indicator">*</span></label>

    <div class="col-md-6">
        <g:textField class="form-control" name="name" cols="40" rows="5" maxlength="255" value="${jobGroup?.name}"/>
    </div>
</div>

<div class="form-group fieldcontain ${hasErrors(bean: jobGroup, field: 'resultGraphiteServers', 'error')} ">
    <label for="resultGraphiteServers" class="control-label col-md-3">
        <g:message code="jobGroup.graphiteServers.label" default="Result Graphite Servers"/>
    </label>

    <div class="col-md-6">
        <g:select id="resultGraphiteServers" name="resultGraphiteServers"
                  from="${de.iteratec.osm.report.external.GraphiteServer.list()*.serverAdress}"
                  keys="${de.iteratec.osm.csi.CsiConfiguration?.list()*.id}"
                  multiple="true" optionKey="id" size="5" value="${jobGroup?.resultGraphiteServers*.id}"
                  class="many-to-many form-control"/>
    </div>

    <div class="col-md-3">
        <a href="#" id="deselectAllResultGraphiteServer" onclick="selectAllResultGraphiteServer(false)">
            <i class="fas fa-undo" aria-hidden="true"></i>
            <g:message message="de.iteratec.isocsi.jobGroup.deselect.all.graphiteServer" default="Deselect all"/>
        </a>
    </div>
</div>


<div class="form-group fieldcontain ${hasErrors(bean: jobGroup, field: 'jobHealthGraphiteServers', 'error')} ">
    <label for="jobHealthGraphiteServers" class="control-label col-md-3">
        <g:message code="jobGroup.graphiteServers.label" default="Job Health Graphite Servers"/>
    </label>

    <div class="col-md-6">
        <g:select id="jobHealthGraphiteServers" name="jobHealthGraphiteServers"
                  from="${de.iteratec.osm.report.external.GraphiteServer.list()*.serverAdress}"
                  keys="${de.iteratec.osm.csi.CsiConfiguration?.list()*.id}"
                  multiple="true" optionKey="id" size="5" value="${jobGroup?.jobHealthGraphiteServers*.id}"
                  class="many-to-many form-control"/>
    </div>

    <div class="col-md-3">
        <a href="#" id="deselectAllJobHealthGraphiteServer" onclick="selectAllJobHealthGraphiteServer(false)">
            <i class="fas fa-undo" aria-hidden="true"></i>
            <g:message message="de.iteratec.isocsi.jobGroup.deselect.all.graphiteServer" default="Deselect all"/>
        </a>
    </div>
</div>


<div class="form-group fieldcontain">
    <label for="jobGroupTags" class="control-label col-md-3">
        <g:message code="job.tags.label" default="tags"/>
    </label>

    <div class="col-md-6">
        <ul name="jobGroupTags" id="jobGroupTags">
            <g:each in="${jobGroup?.tags}">
                <li>${it}</li>
            </g:each>
        </ul>
    </div>
</div>

<div class="form-group fieldcontain">
    <label for="csiConfiguration" class="control-label col-md-3">
        <g:message code="jobGroup.csi_configuration.label" default="CSI Configuration"/>
    </label>

    <div id="csiConfigurationSelection" class="col-md-6">
        <g:select class="form-control"
                  name="csiConfiguration" from="${de.iteratec.osm.csi.CsiConfiguration?.list()*.label}"
                  keys="${de.iteratec.osm.csi.CsiConfiguration?.list()*.label}"
                  value="${jobGroup?.csiConfiguration?.label}"
                  noSelection="${[null: g.message(code: 'jobGroup.csi_configuration.emptyLabel', default: 'Select')]}"/>
    </div>
</div>

<g:if test="${grailsApplication.config.getProperty('grails.de.iteratec.osm.detailAnalysis.enablePersistenceOfDetailAnalysisData')?.toLowerCase() == "true"}">
    <div class="form-group fieldcontain">
        <label for="persistDetailData" class="control-label col-md-3"><g:message code="job.jobGroup.persistHar.label"
                                                                        default="Persist Detaildata"/></label>

        <div>
            <bs:checkBox name="persistDetailData" value="${jobGroup?.persistDetailData}" class="col-md-6"/>
        </div>
    </div>
</g:if>
