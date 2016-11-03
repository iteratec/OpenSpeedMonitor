<%@ page import="de.iteratec.osm.csi.CsiSystem" %>


<div class="form-group fieldcontain ${hasErrors(bean: csiSystem, field: 'label', 'error')} ">
    <label for="label" class="control-label"><g:message code="csiSystem.label.label" default="Label"/></label>

    <div>
        <g:textField name="label" value="${csiSystem?.label}"/>
    </div>
</div>


<div class="form-group fieldcontain ${hasErrors(bean: csiSystem, field: 'jobGroupWeights', 'error')} ">
    <label for="jobGroupWeights" class="control-label"><g:message code="csiSystem.jobGroupWeights.label"
                                                                  default="Job Group Weights"/></label>

    <div>

        <g:render template="jobGroupWeights" model="['csiSystem': csiSystem]"/>

    </div>
</div>

<asset:script type="text/javascript">
    function validateInput() {
        var errorDiv = document.getElementById('errorDiv');
        errorDiv.style.display = 'none';
        var identifiers = ${'jobGroupWeightIdentifiers'}.value;

        var emptyList = false;
        if (identifiers == "[]") {
            emptyList = true;
        } else {
            identifiers = identifiers.split(",");
        }

        if (emptyList || identifiers.length <= 1) {
            errorDiv.style.display = 'block';
            var errorMessage = "${message(code: 'de.iteratec.osm.csi.CsiSystem.notEnoughJobGroups', default: 'at least two jobGroups')}";
            errorDiv.innerHTML = errorMessage;
            return false;
        }

        var jobGroups = [];
        for (var x = 0; x < identifiers.length; x++) {
            var selector = identifiers[x] + '.jobGroup';
            var jobGroup = document.getElementById(selector).value;
            jobGroups.push(jobGroup)
        }

        var unique = jobGroups.filter(function (itm, i, jobGroups) {
            return i == jobGroups.indexOf(itm);
        });

        var allDifferent = unique.length == jobGroups.length;


        if (!allDifferent) {
            errorDiv.style.display = 'block';
            var errorMessage = "${message(code: 'de.iteratec.osm.csi.CsiSystem.jobGroupsDifferent', default: 'unterschiedliche jobGruppen')}";
            errorDiv.innerHTML = errorMessage;
        }

        return allDifferent;
    }
</asset:script>


