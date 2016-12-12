"use strict";

OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.SelectWithSelectAllCheckBox = function(selectElement, checkBoxElement) {
    checkBoxElement = $(checkBoxElement);
    selectElement = $(selectElement);

    if (!checkBoxElement.length || !selectElement.length) {
        return;
    }

    checkBoxElement.on('change', function(event) {
        if(event.currentTarget.checked == true) {
            selectElement.css({opacity: 0.5});
            selectElement.find("option").prop('selected', false);
            selectElement.trigger("chosen:updated");
        } else {
            selectElement.css({opacity: 1});
        }
        selectElement.trigger("change");
    });

    selectElement.on('change', function() {
        var hasSelection = selectElement.find('option:selected').length;
        checkBoxElement.prop('checked', !hasSelection);
        selectElement.css({opacity: hasSelection ? 1.0 : 0.5});
        selectElement.trigger("chosen:updated");
    });

    if(checkBoxElement.is(':checked')){
        selectElement.css({opacity: 0.5});
    }
};

