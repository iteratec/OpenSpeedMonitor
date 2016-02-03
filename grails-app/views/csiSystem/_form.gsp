<%@ page import="de.iteratec.osm.csi.CsiSystem" %>


<div class="control-group fieldcontain ${hasErrors(bean: csiSystemInstance, field: 'label', 'error')} ">
    <label for="label" class="control-label"><g:message code="csiSystem.label.label" default="Label"/></label>

    <div class="controls">
        <g:textField name="label" value="${csiSystemInstance?.label}"/>
    </div>
</div>


<div class="control-group fieldcontain ${hasErrors(bean: csiSystemInstance, field: 'jobGroupWeights', 'error')} ">
    <label for="jobGroupWeights" class="control-label"><g:message code="csiSystem.jobGroupWeights.label"
                                                                  default="Job Group Weights"/></label>

    <div class="controls">

        <g:render template="jobGroupWeights" model="['csiSystemInstance':csiSystemInstance]" />

    </div>
</div>

<asset:script type="text/javascript">
    function checkAllJobGroupsAreDifferent() {
        var errorDiv = document.getElementById('errorDiv');
        errorDiv.style.display = 'none';
        var identifiers = ${'jobGroupWeightIdentifiers'}.value;
        if(identifiers == "[]") {
            return true
        } else {
            identifiers=identifiers.split(",");
        }

        var jobGroups = [];
        for(var x = 0; x < identifiers.length; x++) {
            var selector = identifiers[x] + '.jobGroup';
            var jobGroup = document.getElementById(selector).value;
            jobGroups.push(jobGroup)
        }

        var unique=jobGroups.filter(function(itm,i,jobGroups){
            return i==jobGroups.indexOf(itm);
        });

        var allDifferent = unique.length == jobGroups.length;


        if(!allDifferent) {
            errorDiv.style.display = 'block';
            errorDiv.innerText = "JobGroups muessen unterschiedlich sein";
        }

        return allDifferent;
    }
</asset:script>


