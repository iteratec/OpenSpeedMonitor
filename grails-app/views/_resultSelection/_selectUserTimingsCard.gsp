
<%--
A card with controls to select a user timing
--%>
<h4><g:message
        code="de.iteratec.isr.wptrd.labels.filterUserTimings"
        default="User Timings"/></h4>
<div id="select-usertimings-card${name}" data-no-auto-update="${(boolean) noAutoUpdate}" class="select-usertimings-card-class">
    <g:select id="userTimingsSelectHtmlId${name}"
              class="form-control select-usertimings-element-class"
              name="selectedUserTimings${name}" from="${userTimings}" optionKey="${it}"
              optionValue="${it}" multiple="true"/>
</div>
