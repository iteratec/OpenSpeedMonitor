<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>



<div class="control-group fieldcontain ${hasErrors(bean: jobGroupInstance, field: 'name', 'error')} ">
    <label for="name" class="control-label"><g:message code="jobGroup.name.label" default="Name"/></label>

    <div class="controls">
        <g:textArea name="name" cols="40" rows="5" maxlength="255" value="${jobGroupInstance?.name}"/>
    </div>
</div>

<div class="control-group fieldcontain ${hasErrors(bean: jobGroupInstance, field: 'graphiteServers', 'error')} ">
    <label for="graphiteServers" class="control-label"><g:message code="jobGroup.graphiteServers.label"
                                                                  default="Graphite Servers"/></label>

    <div class="controls">
        <g:select name="graphiteServers" from="${de.iteratec.osm.report.external.GraphiteServer.list()}"
                  multiple="multiple" optionKey="id" size="5" value="${jobGroupInstance?.graphiteServers*.id}"
                  class="many-to-many"/>
    </div>
</div>

<div class="control-group fieldcontain">
    <label for="tags" class="control-label">
        <g:message code="job.tags.label" default="tags" />
    </label>
    <div class="controls">
        <ul name="tags" id="tags" style="margin-left:0px;" class="width_31em">
            <g:each in="${jobGroupInstance?.tags}">
                <li>${it}</li>
            </g:each>
        </ul>
    </div>
</div>

<div class="control-group fieldcontain">
    <label for="csiConfiguration" class="control-label"><g:message code="jobGroup.csi_configuration.label"
                                                                   default="CSI Configuration"/></label>

    <div class="controls" id="csiConfigurationSelection">
        <g:select name="csiConfiguration" from="${de.iteratec.osm.csi.CsiConfiguration?.list()*.label}"
                  keys="${de.iteratec.osm.csi.CsiConfiguration?.list()*.label}"
                  value="${jobGroupInstance?.csiConfiguration?.label}"
                  noSelection="${[null:g.message(code: 'jobGroup.csi_configuration.emptyLabel', default: 'Select one...')]}"/>
    </div>
</div>

<div>
    <label for="csiConfiguration" class="control-label"><g:message code="job.jobGroup.persistHar.label"
                                                                   default="Persist Detaildata"/></label>

    <div class="controls">
        <g:checkBox name="persistHar" />
    </div>

</div>

