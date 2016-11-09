<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<g:select name="${property}" multiple="true" from="${persistentProperty.getReferencedDomainClass().getClazz().list()}"
          optionKey="id" class="form-control" />