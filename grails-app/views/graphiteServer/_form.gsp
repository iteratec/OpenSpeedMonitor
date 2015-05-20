<%@ page import="de.iteratec.osm.report.external.GraphiteServer" %>



			<div class="control-group fieldcontain ${hasErrors(bean: graphiteServerInstance, field: 'serverAdress', 'error')} ">
				<label for="serverAdress" class="control-label"><g:message code="graphiteServer.serverAdress.label" default="Server Adress" /></label>
				<div class="controls">
					<g:textArea name="serverAdress" cols="40" rows="5" maxlength="255" value="${graphiteServerInstance?.serverAdress}"/>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: graphiteServerInstance, field: 'port', 'error')} required">
				<label for="port" class="control-label"><g:message code="graphiteServer.port.label" default="Port" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:field type="number" name="port" min="0" max="65535" required="" value="${graphiteServerInstance.port}"/>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: graphiteServerInstance, field: 'graphitePaths', 'error')} ">
				<label for="graphitePaths" class="control-label"><g:message code="graphiteServer.graphitePaths.label" default="Graphite Paths" /></label>
				<div class="controls">
					<g:select name="graphitePaths" from="${de.iteratec.osm.report.external.GraphitePath.list()}" multiple="multiple" optionKey="id" size="5" value="${graphiteServerInstance?.graphitePaths*.id}" class="many-to-many"/>
				</div>
			</div>

