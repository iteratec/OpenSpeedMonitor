/**
 * Starts the process of validation a csv file for defaultMappings
 * @param file the csv file
 */
function validateDefaultMappingCsv(file) {
    var errorList;
    var reader = new FileReader();
    reader.onload = function (theFile) {
        var csv = theFile.target.result;
        errorList = validate(csv);
        finishedValidation(errorList, csv)
    };
    reader.readAsText(file);
}

/**
 * A csv file for DefaultMappings is correct if:
 *  - all rows have three entries
 *  - all rows have the format {String;Integer;Double}
 *  - for each name there are values between 0 and 20000ms in 20ms steps
 *  - customer satisfactions are between 0 and 100
 *  - at least one customer satisfaction has to be above 1 (to prevent the user from using values between 0 and 1 as percentages)
 * @param csv the csv file for validation
 * @returns {Array} containing error messages
 */
function validate(csv) {
    var allTextLines = csv.split(/\r\n|\n/);
    var nameLoadTimesMap = {};
    var allCustomerSatisfactions = [];
    var errorList = [];
    // Get I18N error messages
    var formatError = POSTLOADED.i18n_defaultMappingFormatError;
    var intError = POSTLOADED.i18n_loadTimeIntegerError;
    var doubleError = POSTLOADED.i18n_customerFrustrationDoubleError;
    var notEveryValueError = POSTLOADED.i18n_defaultMappingNotAllvaluesError;
    var customerSatisfactionNotInPercentError = POSTLOADED.i18n_customerSatisfactionNotInPercentError;
    var percentagesBetween0And1Error = POSTLOADED.i18n_percentagesBetween0And1Error;

    for (var i = 1; i < allTextLines.length; i++) {
        // exclude empty lines
        if (allTextLines[i] == "") {
            continue;
        }

        // get data for this row
        var data = allTextLines[i].split(';');
        var name = data[0];
        var loadTimeInMs = parseInt(data[1]);
        var customerSatisfaction = parseFloat(data[2]);

        if (data.length != 3 && errorList.indexOf(formatError) < 0) {
            errorList.push(formatError);
            continue;
        }

        // regEx for integer and double
        var intRegex = /^[0-9]+$/;
        var doubleRegex = /^[0-9]+(\.[0-9]+)?$/;

        // check correct types
        if (!data[1].match(intRegex) && errorList.indexOf(intError) < 0) {
            errorList.push(intError)
        }
        if (!data[2].match(doubleRegex) && errorList.indexOf(doubleError) < 0) {
            errorList.push(doubleError)
        }

        if ((typeof name !== 'string') || loadTimeInMs == NaN || customerSatisfaction == NaN) {
            if (errorList.indexOf(formatError) < 0) {
                errorList.push(formatError);
            }
        }

        // Create Map in the form of <Name, List<CustomerSatisfaction>>
        if (!nameLoadTimesMap[name]) {
            nameLoadTimesMap[name] = [loadTimeInMs];
        } else {
            nameLoadTimesMap[name].push(loadTimeInMs);
        }

        // add customer satisfaction to list of all customer satisfactions
        allCustomerSatisfactions.push(customerSatisfaction);
    }

    // check if there are values for each name between 0 and 20000ms in 20 ms steps
    for (var name in nameLoadTimesMap) {
        var loadTimes = nameLoadTimesMap[name];

        if (loadTimes.length > (20000 / 20 + 1) && errorList.indexOf(notEveryValueError) < 0) {
            errorList.push(notEveryValueError)
        }

        for (var j = 0; j <= 20000; j += 20) {
            if (loadTimes.indexOf(j) < 0 && errorList.indexOf(notEveryValueError) < 0) {
                errorList.push(notEveryValueError)
            }
        }
    }
    // check if all customer satisfactions are in range of 0 and 100
    var oneSatisfactionAbove1 = false;
    for (var x = 0; x < allCustomerSatisfactions.length; x++) {
        if (allCustomerSatisfactions[x] < 0 || allCustomerSatisfactions[x] > 100) {
            if (errorList.indexOf(customerSatisfactionNotInPercentError) < 0) {
                errorList.push(customerSatisfactionNotInPercentError);
            }
        }
        if (allCustomerSatisfactions[x] > 1) {
            oneSatisfactionAbove1 = true;
        }
    }
    if (!oneSatisfactionAbove1 && errorList.indexOf(percentagesBetween0And1Error) < 0) {
        errorList.push(percentagesBetween0And1Error);
    }

    return errorList;
}

/**
 * Does ajax call to check if there are names of the csv file already in the database
 * @param csv the csv file
 */
function checkForNeedToOverwrite(csv) {
    // get Names from Controller
    $.ajax({
        type: 'POST',
        dataType: "json",
        url: POSTLOADED.link_getNamesOfDefaultMappings,
        success: function (response) {
            checkOverwrite(response, csv);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            $("#errorBoxDefaultMappingCsv").show();
            $("#defaultMappingCsvErrors").text("");
            $("#defaultMappingCsvErrors").append(textStatus + ": " + errorThrown + "<br/>");
        }
    });
}

/**
 * Gets all names which were overwritten.
 * Shows warning on page.
 * @param names the names which are already in the database
 * @param csv the csv file
 */
function checkOverwrite(names, csv) {
    var existingNames = searchForNamesInCsv(names, csv);

    if (existingNames.length > 0) {
        $("#warnAboutOverwritingBox").show();
        $("#warningsOverwriting").text("");
        for (var y in existingNames) {
            var warning = existingNames[y];
            $("#warningsOverwriting").append(warning + " " + POSTLOADED.i18n_overwritingWarning + "<br/>");
        }
    }
    $("#defaultMappingUploadButton").prop("disabled", false);
}

function searchForNamesInCsv(names, csv) {
    var allTextLines = csv.split(/\r\n|\n/);
    // get all Names in csv
    for (var i = 0; i < allTextLines.length; i++) {
        allTextLines[i] = allTextLines[i].replace(/;(.*)/g, '');
    }

    var existingNames = [];

    for (var x in names) {
        var name = names[x];
        if (allTextLines.indexOf(name) >= 0) {
            if (existingNames.indexOf(name) < 0) {
                existingNames.push(name);
            }
        }
    }

    return existingNames;
}

/**
 * Callback Method when validation is done.
 * Shows error messages if exist, otherwise continues with checkForNeedToOverwrite
 * @param errorList the list of error messages, not null
 * @param csv the csv file
 */
function finishedValidation(errorList, csv) {
    if (errorList.length > 0) {
        $("#errorBoxDefaultMappingCsv").show();
        $("#defaultMappingCsvErrors").text("");
        for (var y in errorList) {
            var error = errorList[y];
            $("#defaultMappingCsvErrors").append(error + "<br/>");
        }

    } else {
        checkForNeedToOverwrite(csv)
    }
}