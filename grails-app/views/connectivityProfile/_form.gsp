<%@ page import="de.iteratec.osm.measurement.schedule.ConnectivityProfile" %>



			<div class="form-group fieldcontain ${hasErrors(bean: connectivityProfileInstance, field: 'name', 'error')} required">
				<label for="name" class="control-label"><g:message code="connectivityProfile.name.label" default="Name" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:textField name="name" required="" value="${connectivityProfileInstance?.name}"/>
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: connectivityProfileInstance, field: 'bandwidthDown', 'error')} required">
				<label for="bandwidthDown" class="control-label"><g:message code="connectivityProfile.bandwidthDown.label" default="Bandwidth Down" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:field type="number" name="bandwidthDown" required="" value="${connectivityProfileInstance.bandwidthDown}"/>
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: connectivityProfileInstance, field: 'bandwidthUp', 'error')} required">
				<label for="bandwidthUp" class="control-label"><g:message code="connectivityProfile.bandwidthUp.label" default="Bandwidth Up" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:field type="number" name="bandwidthUp" required="" value="${connectivityProfileInstance.bandwidthUp}"/>
				</div>
			</div>


			<div class="form-group fieldcontain ${hasErrors(bean: connectivityProfileInstance, field: 'latency', 'error')} required">
				<label for="latency" class="control-label"><g:message code="connectivityProfile.latency.label" default="Latency" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:field type="number" name="latency" required="" value="${connectivityProfileInstance.latency}"/>
				</div>
			</div>

			<div class="form-group fieldcontain ${hasErrors(bean: connectivityProfileInstance, field: 'packetLoss', 'error')} required">
				<label for="packetLoss" class="control-label"><g:message code="connectivityProfile.packetLoss.label" default="Packet Loss" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:field type="number" name="packetLoss" required="" value="${connectivityProfileInstance.packetLoss}"/>
				</div>
			</div>