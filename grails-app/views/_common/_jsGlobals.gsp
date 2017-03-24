<script type="text/javascript">

    var OpenSpeedMonitor = OpenSpeedMonitor || {};
    OpenSpeedMonitor.urls = OpenSpeedMonitor.urls || {};
    OpenSpeedMonitor.i18n = OpenSpeedMonitor.i18n || {};

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
        getNamesOfDefaultMappings: '${createLink(controller: 'csiConfigIO', action: 'getNamesOfDefaultCsiMappings')}',
        validateDeletionOfCsiConfiguration: '${createLink(controller: 'csiConfiguration', action: 'validateDeletion')}',
        getJobGroupsUsingCsiConfiguration: '${createLink(controller: 'csiConfiguration', action: 'getJobGroupsUsingCsiConfiguration')}',
        CsiConfigurationSaveCopy: '${createLink(controller: 'csiConfiguration', action: 'saveCopy')}',
        CsiConfigurationConfigurations: '${createLink(controller: 'csiConfiguration', action: 'configurations')}',
        CsiConfigurationDeletion: '${createLink(controller: 'csiConfiguration', action: 'deleteCsiConfiguration')}',
        resultSelection: {
            jobGroups: "${createLink(controller: 'resultSelection', action: 'getJobGroups')}",
            pages: "${createLink(controller: 'resultSelection', action: 'getMeasuredEvents')}",
            browsers: "${createLink(controller: 'resultSelection', action: 'getLocations')}",
            connectivity: "${createLink(controller: 'resultSelection', action: 'getConnectivityProfiles')}",
            resultCount: "${createLink(controller: 'resultSelection', action: 'getResultCount')}"
        },
        jobTags: {
            getTagsForJobs: "${createLink(controller: 'job', action: 'getTagsForJobs')}",
            removeTag: "${createLink(controller: 'job', action: 'removeTag')}",
            addTagToJobs: "${createLink(controller: 'job', action: 'addTagToJobs')}"
        },
        eventResultDashboardShowAll: "${createLink(action: 'showAll', controller: 'eventResultDashboard')}",
        pageAggregationShow: "${createLink(action: 'show', controller: 'pageAggregation')}",
        jobGroupAggregationShow: "${createLink(action: 'show', controller: 'jobGroupAggregation')}",
        tabularResultPresentation: "${createLink(action: 'listResults', controller: 'tabularResultPresentation')}",
        getPagesForMeasuredEvents: "${createLink(action: 'getPagesForMeasuredEvents', controller: 'page')}",
        detailAnalysisShow: "${createLink(action: 'show', controller: 'detailAnalysis')}",
        distributionChartShow: "${createLink(action: 'show', controller: 'distributionChart')}",
        pageComparisonShow: "${createLink(action: 'show', controller: 'pageComparison')}",
        pageComparisonGetPages: "${createLink(controller: 'resultSelection', action: 'getPages')}",
        getScriptNames: "${createLink(controller: 'measurementSetup', action: 'getScriptNames')}"
    };
    OpenSpeedMonitor.i18n.measurandLabels = {
        docCompleteTimeInMillisecs: '${message(code: 'de.iteratec.isr.measurand.short.docCompleteTimeInMillisecs')}',
        domTimeInMillisecs: '${message(code: 'de.iteratec.isr.measurand.short.domTimeInMillisecs')}',
        firstByteInMillisecs: '${message(code: 'de.iteratec.isr.measurand.short.firstByteInMillisecs')}',
        fullyLoadedRequestCount: '${message(code: 'de.iteratec.isr.measurand.short.fullyLoadedRequestCount')}',
        fullyLoadedTimeInMillisecs: '${message(code: 'de.iteratec.isr.measurand.short.fullyLoadedTimeInMillisecs')}',
        loadTimeInMillisecs: '${message(code: 'de.iteratec.isr.measurand.short.loadTimeInMillisecs')}',
        startRenderInMillisecs: '${message(code: 'de.iteratec.isr.measurand.short.startRenderInMillisecs')}',
        docCompleteIncomingBytes: '${message(code: 'de.iteratec.isr.measurand.short.docCompleteIncomingBytes')}',
        docCompleteRequests: '${message(code: 'de.iteratec.isr.measurand.short.docCompleteRequests')}',
        fullyLoadedIncomingBytes: '${message(code: 'de.iteratec.isr.measurand.short.fullyLoadedIncomingBytes')}',
        csByWptDocCompleteInPercent: '${message(code: 'de.iteratec.isr.measurand.short.csByWptDocCompleteInPercent')}',
        speedIndex: '${message(code: 'de.iteratec.isr.measurand.short.speedIndex')}',
        visuallyCompleteInMillisecs: '${message(code: 'de.iteratec.isr.measurand.short.visuallyCompleteInMillisecs')}',
        csByWptVisuallyCompleteInPercent: '${message(code: 'de.iteratec.isr.measurand.short.csByWptVisuallyCompleteInPercent')}'
    };
</script>