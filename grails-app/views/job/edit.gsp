<%@ page import="de.iteratec.osm.measurement.schedule.Job; de.iteratec.osm.measurement.schedule.JobController" %>
<%=packageName%>
<g:render
        template="editOrCreate"
        model="['mode': 'edit', 'entityDisplayName': message(code: 'de.iteratec.isj.job', default: 'Job'), 'entity': job, 'globalUserAgentSuffix': globalUserAgentSuffix, controllerLink:g.createLink(action: 'createDeleteConfirmationText')]"
/>