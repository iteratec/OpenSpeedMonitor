"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectJobGroupCard = (function() {
    var cardElement = $('#select-jobgroup-card');
    var tagToJobGroupNameMap = cardElement.data('tagToJobGroupNameMap') || {};
    var jobGroupSelectElement = $('#folderSelectHtmlId');
    var initialOptions = jobGroupSelectElement.find('option').clone();
    var currentOptions = initialOptions;
    var selectedTag = '';
    var triggerEventsEnabled = true;
    var noResultsText = "No results. Please select a different time frame."; // TODO(sburnicki): use i18n


    var init = function() {
        registerEvents();
    };

    var registerEvents = function() {
        cardElement.find(".filter-button").on('click', function() {
           filterByTag($(this).data('tag'));
        });
        jobGroupSelectElement.on("change", function() {
            if (triggerEventsEnabled) {
                cardElement.trigger("jobGroupSelectionChanged", [$(this).val()]);
            }
        });
    };

    var filterByTag = function(tag) {
        selectedTag = tag;
        var oldSelection = jobGroupSelectElement.val();
        jobGroupSelectElement.empty();
        var jobGroupNamesToShow = tagToJobGroupNameMap[tag];
        currentOptions.forEach(function (option) {
            if (tag === '' || $.inArray($(option).text(), jobGroupNamesToShow) > -1) {
                jobGroupSelectElement.append(option);
            }
        });
        if (!jobGroupSelectElement.children().length) {
            jobGroupSelectElement.append($("<option/>", { disabled: "disabled", text: noResultsText }));
        }
        jobGroupSelectElement.val(oldSelection);
    };

    var enableEventTrigger = function(enable) {
        var oldValue = triggerEventsEnabled;
        triggerEventsEnabled = enable;
        return oldValue;
    };

    var updateJobGroups = function(jobGroups) {
        var wasTriggerEventsEnabled = enableEventTrigger(false);

        currentOptions = OpenSpeedMonitor.domUtils.createOptionsByIdAndName(jobGroups);
        filterByTag(selectedTag);

        enableEventTrigger(wasTriggerEventsEnabled);
    };

    init();
    return {
        updateJobGroups: updateJobGroups
    }
})();