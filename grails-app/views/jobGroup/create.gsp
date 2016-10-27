<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<!doctype html>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="${message(code: 'jobGroup.label', default: 'JobGroup')}" />
	<title><g:message code="default.create.label" args="[entityName]" /></title>
    <asset:stylesheet src="tagit.css"/>
</head>

<body>

<section id="create-jobGroup" class="first">

	<g:hasErrors bean="${jobGroup}">
	<div class="alert alert-error">
		<g:renderErrors bean="${jobGroup}" as="list" />
	</div>
	</g:hasErrors>

	
	<g:form action="save" class="form-horizontal" >
		<fieldset class="form">
			<g:render template="form"/>
		</fieldset>
		<div class="form-actions">
			<g:submitButton name="create" class="btn btn-primary" value="${message(code: 'default.button.create.label', default: 'Create')}" />
            <button class="btn" type="reset"><g:message code="default.button.reset.label" default="Reset" /></button>
		</div>
	</g:form>
	
</section>

<content tag="include.bottom">
    <asset:javascript src="bower_components/tagit/js/tag-it.min.js"/>
	<asset:script type="text/javascript">
		$(document).ready(function() {

            $("ul[name='tags']").tagit({select:true, tagSource: '${g.createLink(action: 'tags', absolute: true)}'});
        });

		function selectAllGraphiteServer(select) {
            var obj = $("#graphiteServers")[0];
            for (var i=0; i<obj.options.length; i++) {
                obj.options[i].selected = select;
            }
        }
	</asset:script>
</content>
		
</body>

</html>
