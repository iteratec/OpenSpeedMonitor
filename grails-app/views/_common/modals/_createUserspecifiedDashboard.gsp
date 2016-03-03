<div id="CreateUserspecifiedDashboardModal" class="modal hide fade" tabindex="-1" role="dialog"
     aria-labelledby="CreateUserspecifiedDashboardModalLabel">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" ria-hidden="true">×</button>
        <h4 id="CreateUserspecifiedDashboardModalLabel">
            <g:message code="de.iteratec.isocsi.dashBoardControllers.custom.title"
                       default="To save as custom dashboard, please enter additional information!"/>
        </h4>
    </div>

    <div class="modal-body">
        <div class="alert alert-error renderInvisible" id="validateDashboardNameErrorDiv"></div>

        <div class="control-group">
            <label class="control-label"
                   for="dashboardNameFromModal">${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.label', default: 'Dashboard name')}</label>

            <div class="controls">
                <div id="spinner-position"></div>
                <input type="text" class="span3" name="dashboardNameFromModal" id="dashboardNameFromModal"
                       placeholder="${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.label', default: 'Dashboard name')}">
            </div>
        </div>

        <div class="control-group">
            <div class="controls">
                <label class="checkbox" for="publiclyVisibleFromModal">
                    <input type="checkbox" name="publiclyVisibleFromModal" id="publiclyVisibleFromModal">
                    ${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.publiclyVisible.label', default: 'Everybody is entitled to view this custom dashboard.')}
                </label>
            </div>
        </div>
    </div>

    <div class="modal-footer">
        <g:form>
            <g:hiddenField name="id" value="${item ? item.id : params.id}"/>
            <g:hiddenField name="_method" value="POST"/>
            <button class="btn" data-dismiss="modal"><g:message code="default.button.cancel.label"
                                                                default="Cancel"/></button>
            <a href="#" class="btn btn-primary" onclick="saveCustomDashboard()"><g:message
                    code="de.iteratec.ism.ui.labels.save" default="Save"/></a>
        </g:form>

    </div>
</div>
<asset:script type="text/javascript">

    function startSpinner(spinnerElement) {
        var opts = {
            lines: 15, // The number of lines to draw
            length: 20, // The length of each line
            width: 10, // The line thickness
            radius: 30, // The radius of the inner circle
            corners: 1, // Corner roundness (0..1)
            rotate: 0, // The rotation offset
            direction: 1, // 1: clockwise, -1: counterclockwise
            color: '#000', // #rgb or #rrggbb or array of colors
            speed: 1, // Rounds per second
            trail: 60, // Afterglow percentage
            shadow: true, // Whether to render a shadow
            hwaccel: false, // Whether to use hardware acceleration
            className: 'spinner', // The CSS class to assign to the spinner
            zIndex: 2e9, // The z-index (defaults to 2000000000)
            top: '50%', // Top position relative to parent in px
            left: '50%' // Left position relative to parent in px
        };
        return new Spinner(opts).spin(spinnerElement);
    }
    function hideMessages() {
        $("#saveDashboardSuccessDiv").hide();
        $("#saveDashboardErrorDiv").hide();
        $("#validateDashboardNameErrorDiv").hide();
    }

    function saveCustomDashboard() {

        hideMessages()

        var dashboardName = document.getElementById("dashboardNameFromModal").value;
        if (dashboardName.trim() !== "") {

            var spinner = startSpinner(document.getElementById('spinner-position'));

            dashBoardParamsFormValues = {};

            var arrayData, objectData;
            arrayData = $('#dashBoardParamsForm').serializeArray();
            objectData = {};

            $.each(arrayData, function () {
                var value;

                if (this.value != null) {
                    value = this.value;
                } else {
                    value = '';
                }

                if (objectData[this.name] != null) {
                    if (!objectData[this.name].push) {
                        objectData[this.name] = [objectData[this.name]];
                    }

                    objectData[this.name].push(value);
                } else {
                    objectData[this.name] = value;
                }
            });

            objectData["dashboardName"] = dashboardName;
            objectData["publiclyVisible"] = document.getElementById("publiclyVisibleFromModal").checked;
            objectData["wideScreenDiagramMontage"] = $("#wide-screen-diagram-montage").is(':checked');
            objectData["chartTitle"] = $("#dia-title").val();
            objectData["chartWidth"] = $("#dia-width").val();
            objectData["chartHeight"] = $("#dia-height").val();
            objectData["loadTimeMaximum"] = $("#dia-y-axis-max").val();
            objectData["loadTimeMinimum"] = $("#dia-y-axis-min").val();
            objectData["showDataMarkers"] = $("#to-enable-marker").is(':checked');
            objectData["showDataLabels"] = $("#to-enable-label").is(':checked');
            objectData["csiTypeDocComplete"] = $("#csiTypeDocComplete").is(':checked');
            objectData["csiTypeVisuallyComplete"] = $("#csiTypeVisuallyComplete").is(':checked');

            var aliases = {};
            var colors = {};
            $(".graphAlias-div").each(function () {
                var id = $(this).attr("id");
                if (id != "graphAlias_clone") {
                    var name = $(this).find("#graphName").val();
                    var alias = $(this).find("#alias").val();
                    var color = $(this).find("#color").val();
                    aliases[name] = alias;
                    // ignore white color
                    if (color != "#FFFFFF" && color != "#ffffff") {
                        colors[name] = color;
                    }
                }
            });

            objectData["graphAliases"] = aliases;
            objectData["graphColors"] = colors;

            json_data = JSON.stringify(objectData);

            $.ajax({
                type: 'POST',
                url: '${createLink(action: 'validateAndSaveDashboardValues', absolute: true)}',
                data: {values: json_data},
                statusCode: {
                    200: function (response) {
                        $("#saveDashboardSuccessDiv").show();
                        window.scrollTo(0, 0);
                        return false;
                    },
                    400: function (response) {
                        $("#saveDashboardErrorDiv").show();
                        var re = /(.*beginErrorMessage\[)(.*)(\]endErrorMessage.*)/;
                        var newtext = response.responseText.replace(re, "$2");
                        newtext = newtext.replace(/, /g, '<br/>');
                        newtext = newtext.replace(/&amp;/g, '&');
                        document.all.saveDashboardErrorDiv.innerHTML = newtext;
                        $('#CreateUserspecifiedDashboardModal').modal('hide');
                        window.scrollTo(0, 0);
                        return false;
                    },
                    500: function (response) {
                        $("#saveDashboardErrorDiv").show();
                        document.all.saveDashboardErrorDiv.innerHTML = "${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.error.save', default: 'An error occured while saving - please try again!')}";
                        $('#CreateUserspecifiedDashboardModal').modal('hide');
                        window.scrollTo(0, 0);
                        return false;
                    },
                    302: function (response) {
                        $("#validateDashboardNameErrorDiv").show();
                        document.all.validateDashboardNameErrorDiv.innerHTML = "${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.error.uniqueness', default: 'Please enter a unique name.')}";
                        window.scrollTo(0, 0);
                        return false;
                    }
                },
                success: function (data) {
                    $('#CreateUserspecifiedDashboardModal').modal('hide');
                    spinner.stop();
                    window.scrollTo(0, 0);
                    return false;
                },
                error: function (result) {
                    spinner.stop();
                    window.scrollTo(0, 0);
                    return false;
                }
            });
        } else {
            $("#validateDashboardNameErrorDiv").show();
            document.all.validateDashboardNameErrorDiv.innerHTML = "${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.error.missing', default: 'Please enter a non-empty name.')}";
            return false;
        }
    }
</asset:script>