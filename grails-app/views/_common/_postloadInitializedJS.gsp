<script>
    $( window ).load(function() {

        window.addEventListener("PostLoadedScriptArrived",function(){

            var idOfItemToBeDeleted = ${item ? item.id : params.id}

            POSTLOADED = new PostLoaded({
                i18n_duplicatePrompt: '${message(code: 'de.iteratec.actions.duplicate.prompt')}',
                i18n_duplicateSuffix: '${message(code: 'de.iteratec.actions.duplicate.copy')}',
                deletionConfirmMessage: '${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}',
                idOfItemToDelete: idOfItemToBeDeleted ? idOfItemToBeDeleted : 'not relevant on this page',
                getNamesOfDefaultMappings: '${createLink(controller: 'csiConfigIO', action: 'getNamesOfDefaultCsiMappings', absolute: true)}',
                overwritingWaring: '${message(code: 'de.iteratec.osm.csi.csvWarning.overwriting')}',
                loadTimeIntegerError: '${message(code: 'de.iteratec.osm.csi.csvErrors.loadTimeIntegerError')}',
                customerFrustrationDoubleError: '${message(code: 'de.iteratec.osm.csi.csvErrors.customerFrustrationDoubleError')}',
                defaultMappingFormatError: '${message(code: 'de.iteratec.osm.csi.csvErrors.defaultMappingFormatError')}',
                defaultMappingNotAllvaluesError: '${message(code: 'de.iteratec.osm.csi.csvErrors.defaultMappingNotAllvaluesError')}',
                customerSatisfactionNotInPercentError:'${message(code: 'de.iteratec.osm.csi.csvErrors.customerSatisfactionNotInPercentError')}',
                percentagesBetween0And1Error: '${message(code: 'de.iteratec.osm.csi.csvErrors.percentagesBetween0And1Error')}'
            });
        });

        var loader = new PostLoader();
        loader.loadJavascript('<g:assetPath src="postload/application-postload.js" absolute="true"/>');

    });
</script>