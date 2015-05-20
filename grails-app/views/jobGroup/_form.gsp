<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>



			<div class="control-group fieldcontain ${hasErrors(bean: jobGroupInstance, field: 'name', 'error')} ">
				<label for="name" class="control-label"><g:message code="jobGroup.name.label" default="Name" /></label>
				<div class="controls">
					<g:textArea name="name" cols="40" rows="5" maxlength="255" value="${jobGroupInstance?.name}"/>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: jobGroupInstance, field: 'groupType', 'error')} required">
				<label for="groupType" class="control-label"><g:message code="jobGroup.groupType.label" default="Group Type" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:select name="groupType" from="${de.iteratec.osm.measurement.schedule.JobGroupType?.values()}" keys="${de.iteratec.osm.measurement.schedule.JobGroupType.values()*.name()}" required="" value="${jobGroupInstance?.groupType?.name()}"/>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: jobGroupInstance, field: 'graphiteServers', 'error')} ">
				<label for="graphiteServers" class="control-label"><g:message code="jobGroup.graphiteServers.label" default="Graphite Servers" /></label>
				<div class="controls">
					<g:select name="graphiteServers" from="${de.iteratec.osm.report.external.GraphiteServer.list()}" multiple="multiple" optionKey="id" size="5" value="${jobGroupInstance?.graphiteServers*.id}" class="many-to-many"/>
				</div>
			</div>

