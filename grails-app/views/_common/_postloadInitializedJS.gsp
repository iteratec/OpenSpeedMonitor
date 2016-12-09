<script type="text/javascript">
    $( window ).load(function() {

        window.addEventListener("PostLoadedScriptArrived",function(){

            var idOfItemToBeDeleted = ${item ? item.id : params.id ?: 'null'};

            OpenSpeedMonitor.postLoaded.i18n_duplicatePrompt = '${message(code: 'de.iteratec.actions.duplicate.prompt')}';
            OpenSpeedMonitor.postLoaded.i18n_duplicateSuffix = '${message(code: 'de.iteratec.actions.duplicate.copy')}';
            OpenSpeedMonitor.postLoaded.i18n_deletionConfirmMessage = '${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}';
            OpenSpeedMonitor.postLoaded.i18n_loadTimeIntegerError = '${message(code: 'de.iteratec.osm.csi.csvErrors.loadTimeIntegerError')}';
            OpenSpeedMonitor.postLoaded.i18n_customerFrustrationDoubleError = '${message(code: 'de.iteratec.osm.csi.csvErrors.customerFrustrationDoubleError')}';
            OpenSpeedMonitor.postLoaded.i18n_defaultMappingFormatError = '${message(code: 'de.iteratec.osm.csi.csvErrors.defaultMappingFormatError')}';
            OpenSpeedMonitor.postLoaded.i18n_defaultMappingNotAllvaluesError = '${message(code: 'de.iteratec.osm.csi.csvErrors.defaultMappingNotAllvaluesError')}';
            OpenSpeedMonitor.postLoaded.i18n_percentagesBetween0And1Error = '${message(code: 'de.iteratec.osm.csi.csvErrors.percentagesBetween0And1Error')}';
            OpenSpeedMonitor.postLoaded.i18n_customerSatisfactionNotInPercentError = ${message(code: 'de.iteratec.osm.csi.csvErrors.customerSatisfactionNotInPercentError')}';
            OpenSpeedMonitor.postLoaded.i18n_deletePageMappingConfirmation = '${message(code: 'de.iteratec.osm.csi.configuration.remove-page-mapppings.cofirmation.msg', default: 'Do you really want to delete mapping of the following page?')}';
            OpenSpeedMonitor.postLoaded.i18n_deletePageMappingProcessing = '${message(code: 'de.iteratec.osm.csi.configuration.remove-page-mapppings.processing.msg', default: 'The mapping of the following page will be deleted')}';
            OpenSpeedMonitor.postLoaded.i18n_nameAlreadyExistMsg = '${message(code: 'de.iteratec.osm.csiConfiguration.nameAlreadyExists', default: ' Name already exists')}';
            OpenSpeedMonitor.postLoaded.i18n_overwritingWarning = '${message(code: 'de.iteratec.osm.csi.csvWarning.overwriting')}';
            OpenSpeedMonitor.postLoaded.i18n_deleteCsiConfigurationConfirmation = '${message(code: 'de.iteratec.osm.csiConfiguration.sureDelete.js', default: 'The following CSI Configuration will be deleted')}';
            OpenSpeedMonitor.postLoaded.i18n_deleteCsiConfigurationWarning = '${message(code: 'de.iteratec.osm.csiConfiguration.overwriteWarning.js', default: 'Overwriting')}';
            OpenSpeedMonitor.postLoaded.i18n_showMsg = '${message(code: 'de.iteratec.osm.csi.ui.show.label', default: 'Show')}';
            OpenSpeedMonitor.postLoaded.i18n_noResultsMsg = '${message(code: 'de.iteratec.osm.resultSelection.noResults', default: 'No Results in selected time frame')}';
            OpenSpeedMonitor.postLoaded.i18n_getNamesOfDefaultMappings = '${createLink(controller: 'csiConfigIO', action: 'getNamesOfDefaultCsiMappings', absolute: true)}';
            OpenSpeedMonitor.postLoaded.i18n_validateDeletionOfCsiConfiguration = '${createLink(controller: 'csiConfiguration', action: 'validateDeletion', absolute: true)}';
            OpenSpeedMonitor.postLoaded.i18n_getJobGroupsUsingCsiConfiguration = '${createLink(controller: 'csiConfiguration', action: 'getJobGroupsUsingCsiConfiguration', absolute: true)}';
            OpenSpeedMonitor.postLoaded.i18n_CsiConfigurationSaveCopy = '${createLink(absolute: true, controller: 'csiConfiguration', action: 'saveCopy')}';
            OpenSpeedMonitor.postLoaded.i18n_CsiConfigurationConfigurations = '${createLink(absolute: true, controller: 'csiConfiguration', action: 'configurations')}';
            OpenSpeedMonitor.postLoaded.i18n_CsiConfigurationDeletion = '${createLink(absolute: true, controller: 'csiConfiguration', action: 'deleteCsiConfiguration')}';
            OpenSpeedMonitor.postLoaded.idOfItemToDelete = idOfItemToBeDeleted ? idOfItemToBeDeleted : 'not relevant on this page';
        });

        OpenSpeedMonitor.postLoader.loadJavascript(
            '<g:assetPath src="postload/application-postload.js" absolute="true"/>',
            true,
                "postload"
        )

    });
</script>