<div id="jobGroupWeights_${i}" class="jobGroupWeight-div" <g:if test="${hidden}">style="display:none;"</g:if>>
    <g:hiddenField name='jobGroupWeights_${i}.id' value='${jobGroupWeight?.id}'/>
    <g:hiddenField name='jobGroupWeights_${i}.deleted' value='false'/>
    <g:hiddenField name='jobGroupWeights_${i}.new' value="${jobGroupWeight?.id == null ? 'true' : 'false'}"/>

    <g:select name="jobGroupWeights_${i}.jobGroup"
              from="${de.iteratec.osm.measurement.schedule.JobGroup.findAllByCsiConfigurationIsNotNull()}"
              optionKey="id"
              optionValue="name"
              value="${jobGroupWeight?.jobGroup?.id}"/>

    <g:textField placeholder="weight" name='jobGroupWeights_${i}.weight' value='${jobGroupWeight?.weight}'/>
    <span class="del-jobGroupWeight">
        <input id="jobGroupWeights_${i}.removeButton" type="button" class="close" value="&times;" />
    </span>
</div>