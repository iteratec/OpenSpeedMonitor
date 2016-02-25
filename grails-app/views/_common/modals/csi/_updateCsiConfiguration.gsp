%{--
    Modal dialog to update CsiConfiguration in view via ajax.
--}%
<div id="updateCsiConfModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="updateCsiConfModalLabel" >
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" ria-hidden="true">×</button>
        <h3 id="updateCsiConfModalLabel"><g:message code="de.iteratec.osm.csi.configuration.update.heading" default="Update CSI configuration"/></h3>
    </div>
    <div class="modal-body">
        %{--container for errors --}%
        <div class="alert alert-error" id="errorUpdatingCsiConfiguration" style="display: none">
            <strong>
                <g:message code="de.iteratec.osm.csiConfiguration.updateErrorTitle"/>
            </strong>
            <p id="updatingCsiConfigurationErrors"></p>
        </div>
        %{--controls for attributes to update--}%
        <form class="form-horizontal">
            <div class="control-group">
                <label class="control-label" for="confLabelFromModal">
                    ${message(code: 'de.iteratec.osm.gui.label.notation', default: 'Label')}
                </label>
                <div class="controls">
                    <div id="spinner-position"></div>
                    <g:textField name="confLabelFromModal" id="confLabelFromModal"></g:textField>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="confDescriptionFromModal">
                    ${message(code: 'de.iteratec.osm.gui.description.notation', default: 'Description')}
                </label>
                <div class="controls">
                    <g:textArea name="confDescriptionFromModal" id="confDescriptionFromModal"></g:textArea>
                </div>
            </div>
        </form>
    </div>
    <div class="modal-footer">
        %{--buttons--}%
        <button class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="default.button.cancel.label" default="Cancel"/></button>
        <a href="#" class="btn btn-primary" onclick="updateCsiConfiguration()">
            <g:message code="de.iteratec.ism.ui.labels.save" default="Save"/>
        </a>
    </div>
</div>

<asset:script type="text/javascript">

    function updateCsiConfiguration(){

        var csiConfLabel = document.getElementById("confLabelFromModal").value;
        var csiConfDescription = document.getElementById("confDescriptionFromModal").value;

        var spinnerParent = document.getElementById('spinner-position');
        var spinner = POSTLOADED.getLargeSpinner('#000', '50%', '50%');
        spinnerParent.appendChild(spinner.el);


        $.ajax({
            type: 'POST',
            url: '${createLink(controller: 'csiConfiguration', action: 'updateConfiguration', absolute: true)}',
            data: {
                csiConfId: actualCsiConfigurationId,
                csiConfNewLabel: csiConfLabel,
                csiConfNewDescription: csiConfDescription
            },
            success: function(data) {
                spinner.stop();
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
                spinner.stop();
                window.scrollTo(0, 0);
                return false;
            }
        });
    }

</asset:script>