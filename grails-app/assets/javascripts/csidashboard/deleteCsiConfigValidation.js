/**
 * You are allowed to delete a csi configuration only if
 *  there is at least one other csiConfiguration left after deleting
 */
function validatedDeletion(label, sureDeleteMessage, overwriteWarningMessage) {
    var deletingAllowed = false;

    $.ajax({
        type: 'POST',
        dataType: "json",
        async: false,
        url: POSTLOADED.validateDeletionOfCsiConfiguration,
        success: function (response) {
            if(response.errorMessages.length > 0) {
                $("#errorDeletingCsiConfiguration").show();
                $("#deletingCsiConfiguratioinErrors").text("");
                for(var i = 0; i < response.errorMessages.length; i++) {
                    $("#deletingCsiConfiguratioinErrors").append(response.errorMessages[i]);
                }
                window.scrollTo(0,0);
                deletingAllowed = false;
            } else {
                deletingAllowed = getUserConfirmation(label, sureDeleteMessage, overwriteWarningMessage);
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            $("#errorDeletingCsiConfiguration").show();
            $("#deletingCsiConfiguratioinErrors").text("");
            $("#deletingCsiConfiguratioinErrors").append(textStatus + ": " + errorThrown + "<br/>");
            deletingAllowed = false;
        }
    });

    return deletingAllowed;
}

/**
 * If there are jobGroups using the csiConfiguration to delete
 * a warning is shown
 */
function getUserConfirmation(label, sureDeleteMessage, overwriteWarningMessage) {
    var userConfirmation = false;

    // Get jobgroups which are using csi configuration to delete
    $.ajax({
        type: 'POST',
        dataType: "json",
        async: false,
        data: {csiConfigurationLabel: label},
        url: POSTLOADED.getJobGroupsUsingCsiConfiguration,
        success: function (response) {
            var confirmationMessage = sureDeleteMessage + "\n\n";
            if(response.jobGroupNames && response.jobGroupNames.length > 0) {
                confirmationMessage = confirmationMessage.concat(overwriteWarningMessage + "\n");
                for(var i = 0; i < response.jobGroupNames.length; i++) {
                    confirmationMessage = confirmationMessage.concat("- " + response.jobGroupNames[i] + "\n");
                }
            }
            userConfirmation = userConfirmation = confirm(confirmationMessage);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            $("#errorDeletingCsiConfiguration").show();
            $("#deletingCsiConfiguratioinErrors").text("");
            $("#deletingCsiConfiguratioinErrors").append(textStatus + ": " + errorThrown + "<br/>");
            userConfirmation = false;
        }
    });

    return userConfirmation;
}