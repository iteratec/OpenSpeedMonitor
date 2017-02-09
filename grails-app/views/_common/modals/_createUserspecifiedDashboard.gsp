<div id="CreateUserspecifiedDashboardModal" class="modal fade" tabindex="-1" role="dialog"
     aria-labelledby="CreateUserspecifiedDashboardModalLabel">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="CreateUserspecifiedDashboardModalLabel">
                    <g:message code="de.iteratec.isocsi.dashBoardControllers.custom.title"
                               default="To save as custom dashboard, please enter additional information!"/>
                </h4>
            </div>

            <div class="modal-body form-horizontal">
                <div class="alert alert-danger renderInvisible" id="validateDashboardNameErrorDiv"></div>
                <div class="form-group">
                    <label class="control-label col-md-3" for="dashboardNameFromModal">
                        ${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.label', default: 'Dashboard name')}:
                    </label>

                    <div class="col-md-7">
                        <input type="text" class="form-control" name="dashboardNameFromModal" id="dashboardNameFromModal"
                               placeholder="${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.label', default: 'Dashboard name')}">
                    </div>
                </div>
                <div class="form-group">
                    <div class="checkbox col-md-10 col-md-offset-3">
                        <label for="publiclyVisibleFromModal">
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
                    <button class="btn btn-default" data-dismiss="modal"><g:message code="default.button.cancel.label"
                                                                        default="Cancel"/></button>
                    <a id="saveDashboardButton" href="#" class="btn btn-primary"
                       onclick="checkDashboardAlreadyExists()"><g:message
                            code="de.iteratec.ism.ui.labels.save" default="Save"/></a>
                    <a id="overwriteDashboardButton" href="#" class="btn btn-primary" style="display: none"
                       onclick="saveCustomDashboard()"><g:message
                            code="de.iteratec.ism.ui.labels.overwrite" default="Overwrite"/></a>
                </g:form>

            </div>
        </div>
    </div>
</div>
<asset:script type="text/javascript">

    function startSpinner(spinnerElement) {
        var opts = {
            lines: 15, // The number of lines to draw
            length: 20, // The length of each line
            width: 10, // The line thickness
            radius: 10, // The radius of the inner circle
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
            top: '0', // Top position relative to parent in px
            left: '0' // Left position relative to parent in px
        };
        return new Spinner(opts).spin(spinnerElement);
    }
    function hideMessages() {
        $("#saveDashboardSuccessDiv").hide();
        $("#saveDashboardErrorDiv").hide();
        $("#validateDashboardNameErrorDiv").hide();
    }

    function checkDashboardAlreadyExists() {
        var objectData = {};
        objectData['dashboardName'] = document.getElementById("dashboardNameFromModal").value;
        var json_data = JSON.stringify(objectData);

        $.ajax({
            type: 'POST',
            url: '${createLink(action: 'checkDashboardNameUnique')}',
            data: {values: json_data},
            success: function (data) {
                if (data['result'] == 'true') {
                    saveCustomDashboard();
                } else {
                    $("#validateDashboardNameErrorDiv").show();
                    document.all.validateDashboardNameErrorDiv.innerHTML = "${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.error.overwriting', default: 'Already Exists.')}";
                    window.scrollTo(0, 0);

                    var saveButton = $('#saveDashboardButton');
                    saveButton.hide();
                    var overwriteButton = $('#overwriteDashboardButton');
                    overwriteButton.show();

                    $('#dashboardNameFromModal').on('input', function () {
                        var saveButton = $('#saveDashboardButton');
                        saveButton.show();
                        var overwriteButton = $('#overwriteDashboardButton');
                        overwriteButton.hide();
                    });
                    return false;
                }
            }
        });

    }

    function updateCustomDashboard(name, publiclyVisible) {
        var message = "${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.warn.overwriting', default: 'Das aktuelle Dashboard wird überschrieben.')}" + ": \n" + name;
        var r = confirm(message);
        if (r == true) {
            saveCustomDashboard(name, publiclyVisible);
        } else {
            return false;
        }
    }

    function saveCustomDashboard(name, publiclyVisible) {

        hideMessages();

        var dashboardName = document.getElementById("dashboardNameFromModal").value;
        dashboardName = dashboardName ? dashboardName : name;
        if (dashboardName.trim() !== "") {

            var spinner = new OpenSpeedMonitor.Spinner();
            spinner.start();

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
            var visible = publiclyVisible != undefined ? publiclyVisible : document.getElementById("publiclyVisibleFromModal").checked;
            objectData["publiclyVisible"] = visible;
            objectData["wideScreenDiagramMontage"] = $("#wide-screen-diagram-montage").is(':checked');
            objectData["chartTitle"] = $("#dia-title").val();
            objectData["chartWidth"] = $("#dia-width").val();
            objectData["chartHeight"] = $("#dia-height").val();
            objectData["loadTimeMaximum"] = $(".dia-y-axis-max").val();
            objectData["loadTimeMinimum"] = $(".dia-y-axis-min").val();
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
                url: '${createLink(action: 'validateAndSaveDashboardValues')}',
                data: {values: json_data},
                statusCode: {
                    200: function (response) {
                        $("#saveDashboardSuccessDiv").show();
                        window.scrollTo(0, 0);
                        var jsonResponse = JSON.parse(response);
                        var linkText = document.createTextNode(dashboardName);
                        var li= document.createElement("li");
                        var a = document.createElement("a");
                        a.appendChild(linkText);
                        li.appendChild(a);
                        a.setAttribute("href", jsonResponse.path+"showAll?dashboardID="+jsonResponse.dashboardId);
                        var customDashBoardSelection = document.getElementById("show-button-dropdown");
                        customDashBoardSelection.appendChild(li);

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