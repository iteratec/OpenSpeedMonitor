<div id="jobGroupWeights_${i}" class="jobGroupWeight-div" <g:if test="${hidden}">style="display:none;"</g:if>>
    <g:hiddenField name='jobGroupWeights_${i}.id' value='${jobGroupWeight?.id}'/>

    <g:select name="jobGroupWeights_${i}.jobGroup"
              from="${de.iteratec.osm.measurement.schedule.JobGroup.findAllByCsiConfigurationIsNotNull()}"
              optionKey="id"
              optionValue="name"
              value="${jobGroupWeight?.jobGroup?.id}"/>

    <g:textField placeholder="weight" name='jobGroupWeights_${i}.weight' value='${jobGroupWeight?.weight}'/>
    <input id="jobGroupWeights_${i}.removeButton" type="button" class="close" value="&times;" />
    <p></p> //needed to create a bit space between the elements
</div>