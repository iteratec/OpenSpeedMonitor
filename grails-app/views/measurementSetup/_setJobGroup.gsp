<h2>Set name for your Job Group</h2>

<div class="row form-horizontal">
    <div class="col-sm-6">
        <div class="form-group">
            <label for="inputJobGroup" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputJobGroupLabel" default="Job Group"/>
            </label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="inputJobGroup" name="inputJobGroup" required>
            </div>
        </div>
    </div>

    <div class="col-sm-6">
        <p>
            %{-- TODO JOHANNES: Define help text Job Group Creation --}%
            You will find the results of your measurement under the here created Job Group.
        </p>
    </div>
</div>

<div class="row">
    <div class="form-group">
        <div class="col-sm-6 text-right">
            <a data-toggle="tab" href="#createScript" class="btn btn-primary" id="setJobGroubTabNextButton">
                <g:message code="default.paginate.next" default="Next"/>
                <i class="fa fa-caret-right" aria-hidden="true"></i>
            </a>
        </div>
    </div>
</div>