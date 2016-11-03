<%@ page import="de.iteratec.osm.report.UserspecificCsiDashboardController" %>
<div class="btn-group">
    <g:link controller="${affectedController}" action="configurations" class="btn btn-default">
        <i class="fa fa-gears"></i>
        <g:message code="de.iteratec.isocsi.csiConfiguration" default="Configuration"/>
    </g:link>
    <a class="dropdown-toggle btn btn-default" data-toggle="dropdown" href="#" role="button"
       aria-haspopup="true" aria-expanded="false" id="customCsiConfigurationDropdownButton">
        <span class="caret"></span>
    </a>
    <ul class="dropdown-menu" id="customCsiConfigurationSelection">
        <li class="dropdown-header">
            <g:message code="de.iteratec.isocsi.csiConfiguration.custom.select.label" default="View another Configuration"/>
        </li>
        <g:if test="${availableCsiConfigurations}">
            <g:each in="${availableCsiConfigurations}" var="availableConfiguration">
                <li><g:link controller="${affectedController}" action="configurations" params="[id: availableConfiguration.id]">
                        ${availableConfiguration.label}
                    </g:link>
                </li>
            </g:each>
        </g:if>
    </ul>
</div>
