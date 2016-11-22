"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectJobGroupCard = (function() {
    var cardElement = $('#select-jobgroup-card');
    var tagToJobGroupNameMap = cardElement.data('tagToJobGroupNameMap') || {};
    var jobGroupSelectElement = $('#folderSelectHtmlId');
    var initialOptions = jobGroupSelectElement.find('option').clone();
    var currentOptions = initialOptions;
    var selectedTag = '';


    var init = function() {
        registerEvents();
    };

    var registerEvents = function() {
        cardElement.find(".filter-button").on('click', function() {
           filterByTag($(this).data('tag'));
        });
        jobGroupSelectElement.on("change", function() {
            cardElement.trigger("jobGroupSelectionChanged", [$(this).val()])
        });
    };

    var filterByTag = function(tag) {
        selectedTag = tag;
        jobGroupSelectElement.empty();
        var jobGroupNamesToShow = tagToJobGroupNameMap[tag];
        currentOptions.forEach(function (option) {
            if (tag === '' || $.inArray($(option).text(), jobGroupNamesToShow) > -1) {
                jobGroupSelectElement.append(option);
            }
        });
        if (!jobGroupSelectElement.children().length) {
            jobGroupSelectElement.append($("<option/>", {
                disabled: "disabled",
                text: "No results. Please select a different time frame."
            }));
        }
    };

    var updateJobGroups = function(jobGroups) {
        currentOptions = OpenSpeedMonitor.domUtils.createOptionsByIdAndName(jobGroups);
        filterByTag(selectedTag);
    };

    init();
    return {
        updateJobGroups: updateJobGroups
    }
})();