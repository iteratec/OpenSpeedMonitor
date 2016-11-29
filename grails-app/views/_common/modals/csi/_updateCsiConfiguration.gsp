%{--
    Modal dialog to update CsiConfiguration in view via ajax.
--}%
<div id="updateCsiConfModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="updateCsiConfModalLabel" >
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" ria-hidden="true">×</button>
                <h4 class="modal-title" id="updateCsiConfModalLabel">
                    <g:message code="de.iteratec.osm.csi.configuration.update.heading" default="Update CSI configuration"/>
                </h4>
            </div>
            <div class="modal-body">
                %{--container for errors --}%
                <div class="alert alert-danger" id="errorUpdatingCsiConfiguration" style="display: none">
                    <strong>
                        <g:message code="de.iteratec.osm.csiConfiguration.updateErrorTitle"/>
                    </strong>
                    <p id="updatingCsiConfigurationErrors"></p>
                </div>
                %{--controls for attributes to update--}%
                <form class="form-horizontal">
                    <div class="form-group">
                        <label class="control-label col-md-3" for="confLabelFromModal">
                            ${message(code: 'de.iteratec.osm.gui.label.notation', default: 'Label')}:
                        </label>
                        <div class="col-md-9">
                            <g:textField name="confLabelFromModal" id="confLabelFromModal" class="form-control" />
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-3" for="confDescriptionFromModal">
                            ${message(code: 'de.iteratec.osm.gui.description.notation', default: 'Description')}:
                        </label>
                        <div class="col-md-9">
                            <g:textArea class="form-control" rows="3" name="confDescriptionFromModal" id="confDescriptionFromModal"/>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                %{--buttons--}%
                <button class="btn btn-default" data-dismiss="modal" aria-hidden="true"><g:message code="default.button.cancel.label" default="Cancel"/></button>
                <a href="#" class="btn btn-primary" onclick="updateCsiConfiguration()">
                    <g:message code="de.iteratec.ism.ui.labels.save" default="Save"/>
                </a>
            </div>
        </div>
    </div>
</div>

<asset:script type="text/javascript">

    function updateCsiConfiguration(){

        var csiConfLabel = document.getElementById("confLabelFromModal").value;
        var csiConfDescription = document.getElementById("confDescriptionFromModal").value;

        $.ajax({
            type: 'POST',
            url: '${createLink(controller: 'csiConfiguration', action: 'updateConfiguration', absolute: true)}',
            data: {
                csiConfId: actualCsiConfigurationId,
                csiConfNewLabel: csiConfLabel,
                csiConfNewDescription: csiConfDescription
            },
            success: function(data) {
                $('#updateCsiConfModal').modal('hide');
                window.scrollTo(0, 0);
                document.getElementById('headerCsiConfLabel').innerHTML = csiConfLabel;
                document.getElementById('headerCsiConfDescription').innerHTML = csiConfDescription;
                return false;
            },
            error: function(result, textStatus, errorThrown) {
                $('#updatingCsiConfigurationErrors').text('');
                $('#updatingCsiConfigurationErrors').append(result.responseText);
                $('#errorUpdatingCsiConfiguration').show();
                window.scrollTo(0, 0);
                return false;
            }
        });
    }

</asset:script>