<div id="jobGroupWeights_${i}" class="form-group section" <g:if test="${hidden}">style="display:none;"</g:if>>
    <g:hiddenField name='jobGroupWeights_${i}.id' value='${jobGroupWeight?.id}'/>
    <div class="col-md-3">
        <g:select name="jobGroupWeights_${i}.jobGroup"
                  from="${de.iteratec.osm.measurement.schedule.JobGroup.findAllByCsiConfigurationIsNotNull()}"
                  optionKey="id"
                  optionValue="name"
                  class="form-control"
                  value="${jobGroupWeight?.jobGroup?.id}"/>
    </div>
    <div class="col-md-2">
        <g:textField placeholder="weight" name='jobGroupWeights_${i}.weight' value='${jobGroupWeight?.weight}' class="form-control" />
    </div>
    <div class="col-md-1">
        <input id="jobGroupWeights_${i}.removeButton" type="button" class="close btn btn-default" value="&times;" />
    </div>
</div>