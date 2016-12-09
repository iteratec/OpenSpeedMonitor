"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectJobGroupCard = (function() {
    var cardElement = $('#select-jobgroup-card');
    var resetButtonElement = cardElement.find(".reset-selection");
    var tagToJobGroupNameMap = cardElement.data('tagToJobGroupNameMap') || {};
    var jobGroupSelectElement = $('#folderSelectHtmlId');
    var currentOptionValues = [];
    var selectedTag = '';

    var init = function() {
        currentOptionValues = $.map(jobGroupSelectElement.find('option'), function(option) {
            return { id: option.value, name: option.text };
        });

        registerEvents();
    };

    var registerEvents = function() {
        var filterButtons = cardElement.find(".filter-button");
        filterButtons.on('click', function() {
            var wasActive = $(this).hasClass("active");
            filterButtons.removeClass("active");
            if (wasActive) {
                filterByTag('');
            } else {
                $(this).addClass("active");
                filterByTag($(this).data('tag'));
            }
        });
        jobGroupSelectElement.on("change", function() {
            cardElement.trigger("jobGroupSelectionChanged", getJobGroupSelection());
        });
        resetButtonElement.on("click", function () {
            OpenSpeedMonitor.domUtils.deselectAllOptions(jobGroupSelectElement, true);
            cardElement.find(".filter-button").removeClass("active");
            filterByTag('');
        })
    };

    var filterByTag = function(tag) {
        selectedTag = tag;
        var jobGroupNamesToShow = tagToJobGroupNameMap[tag];
        var optionsToShow = currentOptionValues.filter(function (value) {
            return (!tag || $.inArray(value.name, jobGroupNamesToShow) > -1);
        });
        OpenSpeedMonitor.domUtils.updateSelectOptions(jobGroupSelectElement, optionsToShow, OpenSpeedMonitor.postLoaded["i18n_noResultsMsg"]);
        jobGroupSelectElement.trigger("change");
    };

    var updateJobGroups = function(jobGroups) {
        currentOptionValues = jobGroups;
        filterByTag(selectedTag);
    };

    var getJobGroupSelection = function () {
        var selectedValues = jobGroupSelectElement.val();
        if (!selectedValues && selectedTag) {
            selectedValues = OpenSpeedMonitor.domUtils.getAllOptionValues(jobGroupSelectElement);
        }
        return {
            ids: selectedValues,
            hasAllSelected: OpenSpeedMonitor.domUtils.hasAllOptionsSelected(jobGroupSelectElement) && !selectedTag
        };
    };

    init();
    return {
        updateJobGroups: updateJobGroups,
        getJobGroupSelection: getJobGroupSelection
    }
})();