<script type="text/javascript">
    $( window ).load(function() {

        window.addEventListener("PostLoadedScriptArrived",function(){

            var idOfItemToBeDeleted = ${item ? item.id : params.id ?: 'null'};

            POSTLOADED = new PostLoaded({
                i18n_duplicatePrompt: '${message(code: 'de.iteratec.actions.duplicate.prompt')}',
                i18n_duplicateSuffix: '${message(code: 'de.iteratec.actions.duplicate.copy')}',
                i18n_deletionConfirmMessage: '${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}',
                i18n_overwritingWaring: '${message(code: 'de.iteratec.osm.csi.csvWarning.overwriting')}',
                i18n_loadTimeIntegerError: '${message(code: 'de.iteratec.osm.csi.csvErrors.loadTimeIntegerError')}',
                i18n_customerFrustrationDoubleError: '${message(code: 'de.iteratec.osm.csi.csvErrors.customerFrustrationDoubleError')}',
                i18n_defaultMappingFormatError: '${message(code: 'de.iteratec.osm.csi.csvErrors.defaultMappingFormatError')}',
                i18n_defaultMappingNotAllvaluesError: '${message(code: 'de.iteratec.osm.csi.csvErrors.defaultMappingNotAllvaluesError')}',
                i18n_percentagesBetween0And1Error: '${message(code: 'de.iteratec.osm.csi.csvErrors.percentagesBetween0And1Error')}',
                i18n_customerSatisfactionNotInPercentError:'${message(code: 'de.iteratec.osm.csi.csvErrors.customerSatisfactionNotInPercentError')}',
                i18n_deletePageMappingConfirmation: '${message(code: 'de.iteratec.osm.csi.configuration.remove-page-mapppings.cofirmation.msg', default: 'Do you really want to delete mapping of the following page?')}',
                i18n_deletePageMappingProcessing: '${message(code: 'de.iteratec.osm.csi.configuration.remove-page-mapppings.processing.msg', default: 'The mapping of the following page will be deleted')}',
                i18n_nameAlreadyExistMsg: '${message(code: 'de.iteratec.osm.csiConfiguration.nameAlreadyExists', default: ' Name already exists')}',
                i18n_overwritingWaring: '${message(code: 'de.iteratec.osm.csi.csvWarning.overwriting')}',
                link_getNamesOfDefaultMappings: '${createLink(controller: 'csiConfigIO', action: 'getNamesOfDefaultCsiMappings', absolute: true)}',
                link_validateDeletionOfCsiConfiguration: '${createLink(controller: 'csiConfiguration', action: 'validateDeletion', absolute: true)}',
                link_getJobGroupsUsingCsiConfiguration: '${createLink(controller: 'csiConfiguration', action: 'getJobGroupsUsingCsiConfiguration', absolute: true)}',
                link_CsiConfigurationSaveCopy: '${createLink(absolute: true, controller: 'csiConfiguration', action: 'saveCopy')}',
                link_CsiConfigurationConfigurations: '${createLink(absolute: true, controller: 'csiConfiguration', action: 'configurations')}',
                idOfItemToDelete: idOfItemToBeDeleted ? idOfItemToBeDeleted : 'not relevant on this page'
            });
        });

        var loader = new PostLoader();
        loader.loadJavascript('<g:assetPath src="postload/application-postload.js" absolute="true"/>');

    });
</script>