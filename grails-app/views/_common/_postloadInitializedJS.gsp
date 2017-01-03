<script type="text/javascript">
    OpenSpeedMonitor = OpenSpeedMonitor || {};
    OpenSpeedMonitor.i18n = {
        duplicatePrompt: '${message(code: 'de.iteratec.actions.duplicate.prompt')}',
        duplicateSuffix: '${message(code: 'de.iteratec.actions.duplicate.copy')}',
        deletionConfirmMessage: '${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}',
        loadTimeIntegerError: '${message(code: 'de.iteratec.osm.csi.csvErrors.loadTimeIntegerError')}',
        customerFrustrationDoubleError: '${message(code: 'de.iteratec.osm.csi.csvErrors.customerFrustrationDoubleError')}',
        defaultMappingFormatError: '${message(code: 'de.iteratec.osm.csi.csvErrors.defaultMappingFormatError')}',
        defaultMappingNotAllvaluesError: '${message(code: 'de.iteratec.osm.csi.csvErrors.defaultMappingNotAllvaluesError')}',
        percentagesBetween0And1Error: '${message(code: 'de.iteratec.osm.csi.csvErrors.percentagesBetween0And1Error')}',
        customerSatisfactionNotInPercentError: '${message(code: 'de.iteratec.osm.csi.csvErrors.customerSatisfactionNotInPercentError')}',
        deletePageMappingConfirmation: '${message(code: 'de.iteratec.osm.csi.configuration.remove-page-mapppings.cofirmation.msg', default: 'Do you really want to delete mapping of the following page?')}',
        deletePageMappingProcessing: '${message(code: 'de.iteratec.osm.csi.configuration.remove-page-mapppings.processing.msg', default: 'The mapping of the following page will be deleted')}',
        nameAlreadyExistMsg: '${message(code: 'de.iteratec.osm.csiConfiguration.nameAlreadyExists', default: ' Name already exists')}',
        overwritingWarning: '${message(code: 'de.iteratec.osm.csi.csvWarning.overwriting')}',
        deleteCsiConfigurationConfirmation: '${message(code: 'de.iteratec.osm.csiConfiguration.sureDelete.js', default: 'The following CSI Configuration will be deleted')}',
        deleteCsiConfigurationWarning: '${message(code: 'de.iteratec.osm.csiConfiguration.overwriteWarning.js', default: 'Overwriting')}',
        showMsg: '${message(code: 'de.iteratec.osm.csi.ui.show.label', default: 'Show')}',
        noResultsMsg: '${message(code: 'de.iteratec.osm.resultSelection.noResults', default: 'No Results in selected time frame')}'
    };
    OpenSpeedMonitor.urls = {
        getNamesOfDefaultMappings: '${createLink(controller: 'csiConfigIO', action: 'getNamesOfDefaultCsiMappings', absolute: true)}',
        validateDeletionOfCsiConfiguration: '${createLink(controller: 'csiConfiguration', action: 'validateDeletion', absolute: true)}',
        getJobGroupsUsingCsiConfiguration: '${createLink(controller: 'csiConfiguration', action: 'getJobGroupsUsingCsiConfiguration', absolute: true)}',
        CsiConfigurationSaveCopy: '${createLink(absolute: true, controller: 'csiConfiguration', action: 'saveCopy')}',
        CsiConfigurationConfigurations: '${createLink(absolute: true, controller: 'csiConfiguration', action: 'configurations')}',
        CsiConfigurationDeletion: '${createLink(absolute: true, controller: 'csiConfiguration', action: 'deleteCsiConfiguration')}',
        resultSelection: {
            jobGroups: "${createLink(absolute: true, controller: 'resultSelection', action: 'getJobGroups')}",
            pages: "${createLink(absolute: true, controller: 'resultSelection', action: 'getMeasuredEvents')}",
            browsers: "${createLink(absolute: true, controller: 'resultSelection', action: 'getLocations')}",
            connectivity: "${createLink(absolute: true, controller: 'resultSelection', action: 'getConnectivityProfiles')}",
            resultCount: "${createLink(absolute: true, controller: 'resultSelection', action: 'getResultCount')}"
        },
        jobTags: {
            getTagsForJobs: "${createLink(absolute: true, controller: 'job', action: 'getTagsForJobs')}",
            removeTag: "${createLink(absolute: true, controller: 'job', action: 'removeTag')}",
            addTagToJobs: "${createLink(absolute: true, controller: 'job', action: 'addTagToJobs')}"
        }
    };
    $(window).load(function () {
        window.addEventListener("PostLoadedScriptArrived", function () {
            OpenSpeedMonitor.postLoaded.idOfItemToDelete = ${item ? item.id : params.id ?: 'null'};
        });

        OpenSpeedMonitor.postLoader.loadJavascript(
            '<g:assetPath src="postload/application-postload.js" absolute="true"/>',
            true,
            "postload"
        )

    });
</script>