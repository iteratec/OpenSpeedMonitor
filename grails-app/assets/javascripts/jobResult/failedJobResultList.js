"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.jobResult = (function (url) {

    var jobResultUrl = url,
        $noResultsHint = $("#no-failed-results-hint"),
        $jobResultTable = $("#jobResultTable"),
        $jobLabel = $("#job-label"),
        $jobResultPanel = $("#job-result-panel"),
        spinner;

    var init = function () {
        spinner = OpenSpeedMonitor.Spinner("#jobResultTable");

        // add on change listener to select element
        $("#selectedJob").change(function () {
            getJobResults($(this).val());
        });
    };

    var clearView = function () {
        $jobResultPanel.removeClass("hidden");

        if (!$noResultsHint.hasClass("hidden"))
            $noResultsHint.addClass("hidden");

        if (!$jobResultTable.hasClass("hidden"))
            $jobResultTable.addClass("hidden");

        $jobLabel.text("");
        $jobResultTable.find("tbody").empty();
    };

    var fillTable = function (result) {
        var resultJSON = jQuery.parseJSON(result);
        $jobLabel.text(resultJSON["jobLabel"]);
        var jobResults = resultJSON["jobResults"];
        if (!jobResults || jobResults.length <= 0) {
            $noResultsHint.removeClass("hidden");
            return;
        }

        $jobResultTable.removeClass("hidden");
        jobResults.forEach(function (jobResult) {
            $jobResultTable.find("tbody").append('<tr><td><a href="' + jobResult['testUrl'] + '">' + jobResult['testId'] + '</a></td> + ' + // testID
                '<td>' + new Date(jobResult['date']).toUTCString() + '</td>' + // date
                '<td>' + jobResult['httpStatusCode'] + '</td>' + // http status
                '<td>' + jobResult['wptStatus'] + '</td>' + // wpt status
                '<td>' + jobResult['description'] + '</td></tr>'); // description
        });
    };

    var getJobResults = function (jobId) {
        spinner.start();
        clearView();

        $.ajax({
            url: jobResultUrl,
            data: {jobId: jobId}
        }).done(function (result) {
            spinner.stop();
            console.log(result);
            fillTable(result);
        });
    };

    init();

    return {}
});