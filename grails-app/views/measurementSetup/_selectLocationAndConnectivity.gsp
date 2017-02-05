<h2>Choose Location and Connectivity</h2>

<div class="row form-horizontal">
    <div class="col-sm-6">
        <div class="form-group">
            <label for="inputLocation" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputLocationLabel" default="Location"/>
            </label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="inputLocation" required>
            </div>
        </div>

        <div class="form-group">
            <label for="inputConnectivity" class="col-sm-2 control-label">
                <g:message code="de.iteratec.osm.setupMeasurementWizard.inputConnectivityLabel" default="Connectivity"/>
            </label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="inputConnectivity" required>
            </div>
        </div>
    </div>

    <div class="col-sm-6">
        <p>
            %{--JOHANNES2DO: Write help text for location and connectivity tab in wizard--}%
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
            <a data-toggle="tab" href="#createScript"
               class="btn btn-default" id="selectLocationAndConnectivityTabPreviousButton">
                <i class="fa fa-caret-left" aria-hidden="true"></i>
                <g:message code="default.paginate.prev" default="Previous"/>
            </a>
            <a data-toggle="tab" href="#createJob"
               class="btn btn-primary" id="selectLocationAndConnectivityTabNextButton">
                <g:message code="default.paginate.next" default="Next"/>
                <i class="fa fa-caret-right" aria-hidden="true"></i>
            </a>
        </div>
    </div>
</div>