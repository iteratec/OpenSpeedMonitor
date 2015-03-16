<%@ page import="de.iteratec.osm.batch.BatchActivity" %>



			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'domain', 'error')} ">
				<label for="domain" class="control-label"><g:message code="batchActivity.domain.label" default="Domain" /></label>
				<div class="controls">
					<g:textField name="domain" value="${batchActivityInstance?.domain}"/>
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'domain', 'error')}</span>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'idWithinDomain', 'error')} required">
				<label for="idWithinDomain" class="control-label"><g:message code="batchActivity.idWithinDomain.label" default="Id within Domain" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:field type="number" name="idWithinDomain" required="" value="${batchActivityInstance.idWithinDomain}"/>
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'idWithinDomain', 'error')}</span>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'name', 'error')} ">
				<label for="name" class="control-label"><g:message code="batchActivity.name.label" default="Name" /></label>
				<div class="controls">
					<g:textField name="name" value="${batchActivityInstance?.name}"/>
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'name', 'error')}</span>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'activity', 'error')} required">
				<label for="activity" class="control-label"><g:message code="batchActivity.activity.label" default="Activity" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:select name="activity" from="${de.iteratec.osm.batch.Activity?.values()}" keys="${de.iteratec.osm.batch.Activity.values()*.name()}" required="" value="${batchActivityInstance?.activity?.name()}"/>
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'activity', 'error')}</span>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'startDate', 'error')} required">
				<label for="startDate" class="control-label"><g:message code="batchActivity.startDate.label" default="Start Date" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<bs:datePicker name="startDate" precision="day"  value="${batchActivityInstance?.startDate}"  />
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'startDate', 'error')}</span>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'status', 'error')} required">
				<label for="status" class="control-label"><g:message code="batchActivity.status.label" default="Status" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:select name="status" from="${de.iteratec.osm.batch.Status?.values()}" keys="${de.iteratec.osm.batch.Status.values()*.name()}" required="" value="${batchActivityInstance?.status?.name()}"/>
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'status', 'error')}</span>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'progress', 'error')} ">
				<label for="progress" class="control-label"><g:message code="batchActivity.progress.label" default="Progress" /></label>
				<div class="controls">
					<g:textField name="progress" value="${batchActivityInstance?.progress}"/>
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'progress', 'error')}</span>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'successfulActions', 'error')} required">
				<label for="successfulActions" class="control-label"><g:message code="batchActivity.successfulActions.label" default="Successful Actions" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:field type="number" name="successfulActions" required="" value="${batchActivityInstance.successfulActions}"/>
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'successfulActions', 'error')}</span>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'stage', 'error')} ">
				<label for="stage" class="control-label"><g:message code="batchActivity.stage.label" default="Stage" /></label>
				<div class="controls">
					<g:textField name="stage" value="${batchActivityInstance?.stage}"/>
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'stage', 'error')}</span>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'progressWithinStage', 'error')} ">
				<label for="progressWithinStage" class="control-label"><g:message code="batchActivity.progressWithinStage.label" default="Progress Within Stage" /></label>
				<div class="controls">
					<g:textField name="progressWithinStage" value="${batchActivityInstance?.progressWithinStage}"/>
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'progressWithinStage', 'error')}</span>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'failures', 'error')} required">
				<label for="failures" class="control-label"><g:message code="batchActivity.failures.label" default="Failures" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:field type="number" name="failures" required="" value="${batchActivityInstance.failures}"/>
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'failures', 'error')}</span>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'lastFailureMessage', 'error')} ">
				<label for="lastFailureMessage" class="control-label"><g:message code="batchActivity.lastFailureMessage.label" default="Last Failure Message" /></label>
				<div class="controls">
					<g:textField name="lastFailureMessage" value="${batchActivityInstance?.lastFailureMessage}"/>
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'lastFailureMessage', 'error')}</span>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: batchActivityInstance, field: 'endDate', 'error')} ">
				<label for="endDate" class="control-label"><g:message code="batchActivity.endDate.label" default="End Date" /></label>
				<div class="controls">
					<bs:datePicker name="endDate" precision="day"  value="${batchActivityInstance?.endDate}" default="none" noSelection="['': '']" />
					<span class="help-inline">${hasErrors(bean: batchActivityInstance, field: 'endDate', 'error')}</span>
				</div>
			</div>

