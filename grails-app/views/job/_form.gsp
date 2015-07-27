<%@ page import="de.iteratec.osm.measurement.environment.Location"%>
<%@ page import="de.iteratec.osm.measurement.environment.WebPageTestServer" %>

<asset:javascript src="tagit/tagit.js"/>
<asset:stylesheet src="tagit.css"/>
<asset:javascript src="chosen/chosen.jquery.min.js"/>
<asset:stylesheet src="chosen/chosen.css"/>
<asset:javascript src="job/edit.js"/>
<asset:stylesheet src="job/edit.css"/>
<asset:javascript src="timeago/futureOnlyTimeago.js"/>
<g:if test="${org.springframework.web.servlet.support.RequestContextUtils.getLocale(request).language.equals('de')}">
    <asset:javascript src="timeago/timeagoDe.js"/>
</g:if>

<div class="row form-group ${hasErrors(bean: job, field: 'label', 'error')} required">
	<label for="label" class="span3 text-right" style="width: 70px !important;">
		<g:message code="job.label.label" default="label" /><span class="required-indicator">*</span>
	</label>
	<div class="span9">
		<g:textField class="form-control width_31em" name="label" value="${job?.label}" />
	</div>
</div>

<div class="row form-group ${hasErrors(bean: job, field: 'location', 'error')} required">
	<label class="span3 text-right" for="location" style="width: 70px !important;"> 
		<g:message code="job.location.label" default="location" /><span class="required-indicator">*</span>
	</label> 
	<div class="span9">
		<select id="location" class="form-control chosen" name="location.id">
			<g:each in="${WebPageTestServer.findAllByActive(true)}" var="server">
				<optgroup label="${server.label}">
					<g:each in="${Location.findAllByWptServerAndActive(server, true)}" var="loc">
						<option value="${loc.id}" <g:if test="${job?.location?.id==loc.id}">selected</g:if>>${loc.uniqueIdentifierForServer ?: loc.location}</option>
					</g:each>
				</optgroup>
			</g:each>
		</select>
	</div>
</div>

<div class="tabbable" style="margin-top: 1em;">
  <ul class="nav nav-tabs">
    <li class="active">
      <a href="#1" data-toggle="tab"><g:message code="job.form.testSettings.label" default="Test Settings" /></a>
    </li>
    <li>
      <a id="scriptTabLink" href="#2" data-toggle="tab"><g:message code="job.form.script.label" default="Script" /></a>
    </li>            
    <li>
      <a href="#3" data-toggle="tab"><g:message code="job.form.advanced.label" default="Advanced" /></a>
    </li>            
  </ul>
  <div class="tab-content">
    <div class="tab-pane active" id="1">
	  <g:render template="testSettingsTab" model="${['job': job, 'connectivites': connectivites]}" />
    </div>
    <div class="tab-pane" id="2">
      <g:render template="scriptTab" model="${['job': job]}" />
    </div>            
    <div class="tab-pane" id="3">
      <g:render template="advancedTab" model="${['job': job]}" />
    </div>
  </div>
</div>

<!-- included because there is no way for the user to supply a value for validationRequest -->
<g:hiddenField name="validationRequest" value="" />

<asset:script type="text/javascript">
    $(document).ready(
        doOnDomReady(
            ${job.label == null},
            "${g.createLink(action: 'nextExecution', absolute: true)}",
            '${customConnNameForNative}',
            ${job.connectivityProfile?job.connectivityProfile.id:'null'},
            ${job.noTrafficShapingAtAll},
            "${g.createLink(action: 'tags', absolute: true)}"
        )
    );
</asset:script>
<asset:deferredScripts/>