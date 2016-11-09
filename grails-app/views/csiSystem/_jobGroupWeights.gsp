<asset:script type="text/javascript">
    var childCount = ${csiSystem?.jobGroupWeights.size()} +0;

    function addJobGroupWeight() {
        var clone = $("#jobGroupWeights__clone").clone();
        var htmlId = 'jobGroupWeights_' + childCount;
        var jobGroupWeightInput = clone.find("input[id$=number]");

        clone.find("input[id$=id]")
                .attr('id', htmlId + '.id')
                .attr('name', htmlId + '.id');
        clone.find("select[id$=jobGroup]")
                .attr('id', htmlId + '.jobGroup')
                .attr('name', htmlId + '.jobGroup');
        clone.find("input[id$=weight]")
                .attr('id', htmlId + '.weight')
                .attr('name', htmlId + '.weight');
        jobGroupWeightInput.attr('id', htmlId + '.number')
                .attr('name', htmlId + '.number');
        clone.find("select[id$=type]")
                .attr('id', htmlId + '.type')
                .attr('name', htmlId + '.type');

        clone.find("input[id$=removeButton]")
                .on('click', function () {
                    deleteJobGroupWeight(htmlId);
                });

        clone.attr('id', 'jobGroupWeights_' + childCount);
        $("#childList").append(clone);
        clone.show();
        jobGroupWeightInput.focus();
        childCount++;
        ${'jobGroupWeightCount'}.
        value = childCount;
        var identifiers =
        ${'jobGroupWeightIdentifiers'}.
        value;
        if (identifiers == "[]") {
            identifiers = JSON.parse(identifiers);
        } else {
            identifiers = identifiers.split(",");
        }
        identifiers.push(htmlId);
        ${'jobGroupWeightIdentifiers'}.
        value = identifiers;
    }

    function deleteJobGroupWeight(identifier) {
        $('#' + identifier).remove();
        var identifiers =
        ${'jobGroupWeightIdentifiers'}.
        value;
        if (identifiers == "[]") {
            identifiers = JSON.parse(identifiers);
        } else {
            identifiers = identifiers.split(",");
        }
        var index = identifiers.indexOf(identifier);
        identifiers.splice(index, 1);
        ${'jobGroupWeightIdentifiers'}.
        value = identifiers;
    }

</asset:script>

<div id="childList">
    <g:hiddenField name="jobGroupWeightCount" value="${csiSystem.jobGroupWeights.size()}"/>
    <g:hiddenField name="jobGroupWeightIdentifiers" value="${[]}"/>
    <g:each var="jobGroupWeight" in="${csiSystem.jobGroupWeights}" status="i">

        <g:render template='jobGroupWeight' model="['jobGroupWeight': jobGroupWeight, 'i': i, 'hidden': false]"/>
        <asset:script type="text/javascript">
            var identifiers = ${'jobGroupWeightIdentifiers'}.value;
        if(identifiers == "[]") {
            identifiers = JSON.parse(identifiers);
        } else {
            identifiers=identifiers.split(",");
        }
        identifiers.push('jobGroupWeights_${i}');
            ${'jobGroupWeightIdentifiers'}.value = identifiers;
            var selector = "jobGroupWeights_${i}.removeButton";
            document.getElementById(selector).onclick = function() {
                    deleteJobGroupWeight('jobGroupWeights_${i}');
            }
        </asset:script>
    </g:each>
</div>
<input type="button" value="Add jobGroupWeight" onclick="addJobGroupWeight();" class="btn btn-default" />