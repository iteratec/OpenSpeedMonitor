<%@ page import="de.iteratec.osm.measurement.schedule.JobGroup" %>
<g:select name="${property}" multiple="true" from="${persistentProperty.getAssociatedEntity().getJavaClass().list()}"
          optionKey="id" class="form-control" />
