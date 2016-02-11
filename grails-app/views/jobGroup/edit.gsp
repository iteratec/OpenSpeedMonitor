<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<!doctype html>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>

    <asset:stylesheet src="tagit.css"/>
    <g:set var="entityName" value="${message(code: 'jobGroup.label', default: 'JobGroup')}" />
</head>

<body>

<section id="edit-jobGroup" class="first">

	<g:hasErrors bean="${jobGroupInstance}">
	<div class="alert alert-error">
		<g:renderErrors bean="${jobGroupInstance}" as="list" />
	</div>
	</g:hasErrors>

	<g:form method="post" class="form-horizontal" >
		<g:hiddenField name="id" value="${jobGroupInstance?.id}" />
		<g:hiddenField name="version" value="${jobGroupInstance?.version}" />
		<fieldset class="form">
			<g:render template="form"/>
		</fieldset>
		<div class="form-actions">
			<g:actionSubmit class="btn btn-primary" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
      <g:render template="/_common/modals/deleteSymbolLink"/>
      <button class="btn" type="reset"><g:message code="default.button.reset.label" default="Reset" /></button>
		</div>
	</g:form>

</section>

<content tag="include.bottom">
    <asset:javascript src="tagit/tagit.js"/>
    <asset:script type="text/javascript">
        $(document).ready(function() {

            $("ul[name='tags']").tagit({select:true, tagSource: '${g.createLink(action: 'tags', absolute: true)}'});

            changeCsiConfigurationSelectionStatus();

            $("#groupTypeSelection").change(function () {
                changeCsiConfigurationSelectionStatus();
            })
        });

        function changeCsiConfigurationSelectionStatus() {
            if ($("select[name=groupType]").serialize() == 'groupType=CSI_AGGREGATION') {
                $("#csiConfigurationSelection").children().prop('disabled', false);
            } else {
                $("#csiConfigurationSelection").children().prop('disabled', true);
                $("select[name=csiConfiguration]").val('null');
            }
        }
    </asset:script>
</content>
</body>

</html>
