<script type="text/javascript">

    var OpenSpeedMonitor = OpenSpeedMonitor || {};
    OpenSpeedMonitor.urls = OpenSpeedMonitor.urls || {};
    OpenSpeedMonitor.i18n = OpenSpeedMonitor.i18n || {};
    OpenSpeedMonitor.user = OpenSpeedMonitor.user || {};

    var language = determineLanguage();

    function determineLanguage() {
        var lang = '${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}';
        if (!lang) {
            lang = navigator.language.substr(0, 2);
            if (lang != "en" || lang != "de") {
                lang = "en";
            }
        }
        return lang;
    };

    OpenSpeedMonitor.i18n = {
        lang: language,
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
            resultCount: "${createLink(controller: 'resultSelection', action: 'getResultCount')}",
            userTimings: "${createLink(controller: 'resultSelection', action: 'getUserTimings')}"
        },
        jobTags: {
            getTagsForJobs: "${createLink(controller: 'job', action: 'getTagsForJobs')}",
            removeTag: "${createLink(controller: 'job', action: 'removeTag')}",
            addTagToJobs: "${createLink(controller: 'job', action: 'addTagToJobs')}"
        },
        eventResultDashboardShowAll: "${createLink(action: 'showAll', controller: 'eventResultDashboard')}",
        pageAggregationShow: "${createLink(action: 'show', controller: 'pageAggregation')}",
        pageAggregationGetData: "${createLink(controller: 'pageAggregation', action: 'getBarchartData')}",
        jobGroupAggregationShow: "${createLink(action: 'show', controller: 'jobGroupAggregation')}",
        jobGroupAggregationGetData: "${createLink(controller: 'jobGroupAggregation', action: 'getBarchartData')}",
        tabularResultPresentation: "${createLink(action: 'listResults', controller: 'tabularResultPresentation')}",
        getPagesForMeasuredEvents: "${createLink(action: 'getPagesForMeasuredEvents', controller: 'page')}",
        detailAnalysisShow: "${createLink(action: 'show', controller: 'detailAnalysis')}",
        distributionChartShow: "${createLink(action: 'show', controller: 'distributionChart')}",
        pageComparisonShow: "${createLink(action: 'show', controller: 'pageComparison')}",
        pageComparisonGetData: "${createLink(controller: 'pageComparison', action: 'getBarchartData')}",
        pageComparisonGetPages: "${createLink(controller: 'resultSelection', action: 'getPages')}",
        pageComparisonGetJobGroupToPagesMap: "${createLink(controller: 'resultSelection', action: 'getJobGroupToPagesMap')}",
        getScriptNames: "${createLink(controller: 'measurementSetup', action: 'getScriptNames')}",
        getJobNames: "${createLink(controller: 'measurementSetup', action: 'getJobNames')}",
        cronExpressionNextExecution: "${createLink(controller: 'cronExpression', action: 'nextExecutionTime')}"
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
    OpenSpeedMonitor.i18n.measurands = {
        DOC_COMPLETE_TIME: '${message(code: 'de.iteratec.isr.measurand.DOC_COMPLETE_TIME')}',
        DOM_TIME: '${message(code: 'de.iteratec.isr.measurand.DOM_TIME')}',
        FIRST_BYTE: '${message(code: 'de.iteratec.isr.measurand.FIRST_BYTE')}',
        FULLY_LOADED_REQUEST_COUNT: '${message(code: 'de.iteratec.isr.measurand.FULLY_LOADED_REQUEST_COUNT')}',
        FULLY_LOADED_TIME: '${message(code: 'de.iteratec.isr.measurand.FULLY_LOADED_TIME')}',
        LOAD_TIME: '${message(code: 'de.iteratec.isr.measurand.LOAD_TIME')}',
        START_RENDER: '${message(code: 'de.iteratec.isr.measurand.START_RENDER')}',
        DOC_COMPLETE_INCOMING_BYTES: '${message(code: 'de.iteratec.isr.measurand.DOC_COMPLETE_INCOMING_BYTES')}',
        DOC_COMPLETE_REQUESTS: '${message(code: 'de.iteratec.isr.measurand.DOC_COMPLETE_REQUESTS')}',
        FULLY_LOADED_INCOMING_BYTES: '${message(code: 'de.iteratec.isr.measurand.FULLY_LOADED_INCOMING_BYTES')}',
        SPEED_INDEX: '${message(code: 'de.iteratec.isr.measurand.SPEED_INDEX')}',
        VISUALLY_COMPLETE_85: '${message(code: 'de.iteratec.isr.measurand.VISUALLY_COMPLETE_85')}',
        VISUALLY_COMPLETE_90: '${message(code: 'de.iteratec.isr.measurand.VISUALLY_COMPLETE_90')}',
        VISUALLY_COMPLETE_95: '${message(code: 'de.iteratec.isr.measurand.VISUALLY_COMPLETE_95')}',
        VISUALLY_COMPLETE_99: '${message(code: 'de.iteratec.isr.measurand.VISUALLY_COMPLETE_99')}',
        VISUALLY_COMPLETE: '${message(code: 'de.iteratec.isr.measurand.VISUALLY_COMPLETE')}',
        CS_BY_WPT_DOC_COMPLETE: '${message(code: 'de.iteratec.isr.measurand.CS_BY_WPT_DOC_COMPLETE')}',
        CS_BY_WPT_VISUALLY_COMPLETE: '${message(code: 'de.iteratec.isr.measurand.CS_BY_WPT_VISUALLY_COMPLETE')}',
        FIRST_INTERACTIVE: '${message(code: 'de.iteratec.isr.measurand.FIRST_INTERACTIVE')}',
        CONSISTENTLY_INTERACTIVE: '${message(code: 'de.iteratec.isr.measurand.CONSISTENTLY_INTERACTIVE')}'
    };
    OpenSpeedMonitor.i18n.thresholdButtons = {
        submit: '${message(code: 'job.threshold.submit')}',
        edit: '${message(code: 'job.threshold.edit')}',
        save: '${message(code: 'job.threshold.save')}',
        discard: '${message(code: 'job.threshold.discard')}',
        delete: '${message(code: 'job.threshold.delete')}',
        remove: '${message(code: 'job.threshold.remove')}'
    }

    OpenSpeedMonitor.user.loggedIn = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_SUPER_ADMIN") ? 'true': 'false'};
</script>
