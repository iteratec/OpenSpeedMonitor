<h2>Create your job and start the measurement</h2>

<div class="row form-horizontal">
    <div class="col-sm-6">
        <div class="form-group">
            <label for="inputJobName" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputJobLabel" default="Job"/>
            </label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="inputJobName" required>
            </div>
        </div>
    </div>

    <div class="col-sm-6">
        <p>
            %{--JOHANNES2DO: Write help text for Job tab in wizard--}%
            Lorem ipsum dolor sit amet, consectetur adipisicing elit.
            Est excepturi officiis placeat qui quibusdam?
            Aliquid commodi delectus deleniti dolorem eaque error,
            et id impedit maxime neque qui velit voluptas voluptatem!
            Lorem ipsum dolor sit amet, consectetur adipisicing elit.
            Est excepturi officiis placeat qui quibusdam?
            Aliquid commodi delectus deleniti dolorem eaque error,
            et id impedit maxime neque qui velit voluptas voluptatem!
        </p>
    </div>
</div>

<div class="row">
    <div class="form-group">
        <div class="col-sm-6 text-right">
            <a data-toggle="tab" class="btn btn-default" id="createJobTabPreviousButton">
                <i class="fa fa-caret-left" aria-hidden="true"></i>
                <g:message code="default.paginate.prev" default="Previous"/>
            </a>
            <button type="submit" class="btn btn-primary">
                <i class="fa fa-plus" aria-hidden="true"></i>
                <g:message code="de.iteratec.osm.setupMeasurementWizard.createJobStartMeasurement" default="Create Job and Start Measurement"/>
            </button>
        </div>
    </div>
</div>