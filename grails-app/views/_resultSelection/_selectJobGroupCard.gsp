<%@ page defaultCodec="none" %></page>
<%--
A card with controls to select a job group
--%>
<div class="card" id="select-jobgroup-card" data-no-auto-update="${(boolean) noAutoUpdate}"
     data-tag-to-job-group-name-map='${tagToJobGroupNameMap as grails.converters.JSON}'>
    %{--JobGroups----------------------------------------------------------------------------------------------}%
    <h2>
        <g:message code="de.iteratec.isr.wptrd.labels.filterFolder" default="Folder"/>
    </h2>
    <g:select id="folderSelectHtmlId" class="form-control"
              name="selectedFolder" from="${folders}" optionKey="id"
              optionValue="name" value="${selectedFolder}"
              multiple="true"/>
    <div data-toggle="buttons" class="filter-buttons">
        <g:each in="${tagToJobGroupNameMap.keySet().collate(5)}" var="tagSubset">
            <div class="btn-group">
                <g:each in="${tagSubset}" var="tag">
                    <button type="button" class="btn btn-xs btn-default filter-button" data-tag="${tag}">
                        <i class="fa fa-filter"></i>&nbsp;${tag}
                    </button>
                </g:each>
            </div>
        </g:each>
    </div>
</div>
<asset:script type="text/javascript">
    $(window).load(function() {
        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="_resultSelection/selectJobGroupCard.js" />', 'selectJobGroupCard');
    });
</asset:script>
