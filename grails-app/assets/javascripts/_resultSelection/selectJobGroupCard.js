"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectJobGroupCard = (function() {
    var cardElement = $('#select-jobgroup-card');
    var tagToJobGroupNameMap = cardElement.data('tagToJobGroupNameMap') || {};
    var jobGroupSelectElement = $('#folderSelectHtmlId');
    var currentOptionValues = [];
    var selectedTag = '';
    var triggerEventsEnabled = true;
    var noResultsText = "No results. Please select a different time frame."; // TODO(sburnicki): use i18n


    var init = function() {
        currentOptionValues = $.map(jobGroupSelectElement.find('option'), function(option) {
            return { id: option.value, name: option.text };
        });

        registerEvents();
    };

    var registerEvents = function() {
        cardElement.find(".filter-button").on('click', function() {
           filterByTag($(this).data('tag'));
        });
        jobGroupSelectElement.on("change", function() {
            if (triggerEventsEnabled) {
                cardElement.trigger("jobGroupSelectionChanged", {
                    ids: $(this).val(),
                    hasAllSelected: OpenSpeedMonitor.domUtils.hasAllOptionsSelected($(this))
                });
            }
        });
    };

    var filterByTag = function(tag) {
        selectedTag = tag;
        var jobGroupNamesToShow = tagToJobGroupNameMap[tag];
        var optionsToShow = currentOptionValues.filter(function (value) {
            return (tag === '' || $.inArray(value.name, jobGroupNamesToShow) > -1);
        });
        OpenSpeedMonitor.domUtils.updateSelectOptions(jobGroupSelectElement, optionsToShow, noResultsText);
    };

    var enableEventTrigger = function(enable) {
        var oldValue = triggerEventsEnabled;
        triggerEventsEnabled = enable;
        return oldValue;
    };

    var updateJobGroups = function(jobGroups) {
        var wasTriggerEventsEnabled = enableEventTrigger(false);

        currentOptionValues = jobGroups;
        filterByTag(selectedTag);

        enableEventTrigger(wasTriggerEventsEnabled);
    };

    init();
    return {
        updateJobGroups: updateJobGroups
    }
})();