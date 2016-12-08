"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectJobGroupCard = (function() {
    var cardElement = $('#select-jobgroup-card');
    var resetButtonElement = cardElement.find(".reset-selection");
    var tagToJobGroupNameMap = cardElement.data('tagToJobGroupNameMap') || {};
    var jobGroupSelectElement = $('#folderSelectHtmlId');
    var currentOptionValues = [];
    var selectedTag = '';
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
            var selectedValues = $(this).val();
            if (!selectedValues && selectedTag) {
                selectedValues = OpenSpeedMonitor.domUtils.getAllOptionValues(jobGroupSelectElement);
            }
            cardElement.trigger("jobGroupSelectionChanged", {
                ids: selectedValues,
                hasAllSelected: OpenSpeedMonitor.domUtils.hasAllOptionsSelected($(this)) && !selectedTag
            });
        });
        resetButtonElement.on("click", function () {
            OpenSpeedMonitor.domUtils.deselectAllOptions(jobGroupSelectElement, true);
            filterByTag('');
        })
    };

    var filterByTag = function(tag) {
        selectedTag = tag;
        var jobGroupNamesToShow = tagToJobGroupNameMap[tag];
        var optionsToShow = currentOptionValues.filter(function (value) {
            return (!tag || $.inArray(value.name, jobGroupNamesToShow) > -1);
        });
        OpenSpeedMonitor.domUtils.updateSelectOptions(jobGroupSelectElement, optionsToShow, noResultsText);
        jobGroupSelectElement.trigger("change");
    };

    var updateJobGroups = function(jobGroups) {
        currentOptionValues = jobGroups;
        filterByTag(selectedTag);
    };

    init();
    return {
        updateJobGroups: updateJobGroups
    }
})();