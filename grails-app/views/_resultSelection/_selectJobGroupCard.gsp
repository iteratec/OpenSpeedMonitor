<%@ page defaultCodec="none" %></page>
<%--
A card with controls to select a job group
--%>
<div class="card" id="select-jobgroup">
    %{--JobGroups----------------------------------------------------------------------------------------------}%
    <legend>
        <g:message code="de.iteratec.isr.wptrd.labels.filterFolder" default="Folder"/>
    </legend>
    <g:select id="folderSelectHtmlId" class="form-control"
              name="selectedFolder" from="${folders}" optionKey="id"
              optionValue="name" value="${selectedFolder}"
              multiple="true"/>
    <div data-toggle="buttons" class="filter-buttons">
        <div class="btn-group">
            <button type="button" class="btn btn-xs btn-default" onclick="filterJobGroupSelect('')">
                <i class="fa fa-remove"></i>&nbsp;<g:message code="de.iteratec.osm.ui.filter.clear" default="Clear filter"/>
            </button>
        </div>
        <g:each in="${tagToJobGroupNameMap.keySet().collate(5)}" var="tagSubset">
            <div class="btn-group">
                <g:each in="${tagSubset}" var="tag">
                    <button type="button" class="btn btn-xs btn-default" onclick="filterJobGroupSelect('${tag}')">
                        <i class="fa fa-filter"></i>&nbsp;${tag}
                    </button>
                </g:each>
            </div>
        </g:each>
    </div>
</div>
<asset:script type="text/javascript">

    var tagToJobGroupNameMap = ${tagToJobGroupNameMap as grails.converters.JSON};
    var jobGroupOptions = $('#folderSelectHtmlId option').clone();

    var filterJobGroupSelect = function(filterText){

        $('#folderSelectHtmlId').empty();

        var jobGroupNamesToShow = tagToJobGroupNameMap[filterText];
        jobGroupOptions.filter(function (idx, jobGroupOption) {
            return (filterText === '' || $.inArray($(jobGroupOption).text(), jobGroupNamesToShow) > -1);
        }).appendTo('#folderSelectHtmlId');

    }
</asset:script>