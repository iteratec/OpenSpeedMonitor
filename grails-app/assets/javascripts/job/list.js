/*
 * OpenSpeedMonitor (OSM)
 * Copyright 2014 iteratec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
JobStatusUpdater = function () {
    var getJobsUrl = '';
    var getLastRunUrl = '';
    var cancelJobUrl = '';

    var finishedJobs = [];
    var repeatFnTimer = null;

    function cancelJobRun(jobId, testId) {
        jQuery.ajax({
            type: 'POST',
            url: JobStatusUpdater.cancelJobUrl,
            data: {jobId: jobId, testId: testId},
            success: function () {
                $(this).remove();
            }
        });
    }

    function updateDateLastRun(jobId) {
        jQuery.ajax({
            type: 'POST',
            url: JobStatusUpdater.getLastRunUrl,
            data: {jobId: jobId},
            success: function (timeagoHtml) {
                var parentTableRow = $('[name="selected.' + jobId + '"]').parent().parent();
                var lastRunTableCell = $($('abbr.timeago', parentTableRow)[0]).parent();
                lastRunTableCell.html(timeagoHtml);
                $('abbr.timeago', lastRunTableCell).timeago();
            }
        });
    }

    // from http://stackoverflow.com/a/19519701
    var vis = (function () {
        var stateKey, eventKey, keys = {
            hidden: "visibilitychange",
            webkitHidden: "webkitvisibilitychange",
            mozHidden: "mozvisibilitychange",
            msHidden: "msvisibilitychange"
        };
        for (stateKey in keys) {
            if (stateKey in document) {
                eventKey = keys[stateKey];
                break;
            }
        }
        return function (c) {
            if (c) document.addEventListener(eventKey, c);
            return !document[stateKey];
        }
    })();

    function repeatFn(firstRun) {
        jQuery.ajax({
            type: 'POST',
            url: JobStatusUpdater.getJobsUrl,
            success: function (result, textStatus) {
                $('#serverdown').hide();
                $('[id^="runningstatus-"]').html('');
                $.each(result, function (jobId, data) {
                    for (var i = 0; i < data.length; i++) {
                        var cssClass = data[i].terminated ? 'done' : 'running';
                        var cancelLink = data[i].cancelLinkHtml ? data[i].cancelLinkHtml : '';
                        var status = data[i].status < 400 ? '<a href="' + data[i].testUrl + '">' + data[i].message + '</a>' : data[i].message;
                        // detect whether job just terminated
                        if (data[i].terminated && JobStatusUpdater.finishedJobs.indexOf(data[i].testId) === -1) {
                            JobStatusUpdater.finishedJobs.push(data[i].testId);
                            if (!firstRun) {
                                updateDateLastRun(jobId);
                            }
                        }

                        $('#runningstatus-' + jobId).append('<span class="status ' + cssClass + '">' + status + cancelLink + '</span>');
                    }
                });
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                $('[id^="runningstatus-"]').html('');
                $('#serverdown').show();
            }
        });
    }

    function initLoop(getJobsUrl, cancelJobUrl, getLastRunUrl, repeatAfterMs) {
        this.getJobsUrl = getJobsUrl;
        this.getLastRunUrl = getLastRunUrl;
        this.cancelJobUrl = cancelJobUrl;
        this.finishedJobs = [];
        repeatFn(true);
        this.repeatFnTimer = setInterval('JobStatusUpdater.repeatFn(false)', repeatAfterMs);

        // Poll server only when current page is visible, else pause:
        vis(function () {
            if (vis() && !JobStatusUpdater.repeatFnTimer) {
                JobStatusUpdater.repeatFnTimer = window.setInterval('JobStatusUpdater.repeatFn(false)', repeatAfterMs);
            } else if (!vis() && JobStatusUpdater.repeatFnTimer) {
                window.clearInterval(JobStatusUpdater.repeatFnTimer);
                JobStatusUpdater.repeatFnTimer = null;
            }
        });
    }

    return {
        initLoop: initLoop,
        cancelJobRun: cancelJobRun,
        repeatFn: repeatFn
    }
}();

function initTable(nextExecutionLink) {
    FutureOnlyTimeago.init($('abbr.timeago'), nextExecutionLink);
    updatePrettyCrons();
    //The server will send the next run time with the server timezone.
    //The mouseenter makes sure to convert it into the locale timezone.
    $(".timeago").on('mouseenter', function () {
        if($(this).attr("converted") != "true"){
            $(this).attr("title",moment($(this).attr("title")).format("D. MMM YYYY HH:mm"));
            $(this).attr("converted", true);// Save state, so we won't try to change the format again
        }
    })
}

InactiveJobLoader = function (listLink, nextExecutionLink) {

    var listJobsLink = listLink;
    var nextJobExecutionLink = nextExecutionLink;

    this.loadJobs = function () {
        var opts = {
            lines: 15, // The number of lines to draw
            length: 20, // The length of each line
            width: 10, // The line thickness
            radius: 30, // The radius of the inner circle
            corners: 1, // Corner roundness (0..1)
            rotate: 0, // The rotation offset
            direction: 1, // 1: clockwise, -1: counterclockwise
            color: '#000', // #rgb or #rrggbb or array of colors
            speed: 1, // Rounds per second
            trail: 60, // Afterglow percentage
            shadow: true, // Whether to render a shadow
            hwaccel: false, // Whether to use hardware acceleration
            className: 'spinner', // The CSS class to assign to the spinner
            zIndex: 2e9, // The z-index (defaults to 2000000000)
            top: 'auto', // Top position relative to parent in px
            left: '50%' // Left position relative to parent in px
        };

        var spinnerParent = document.getElementById('spinner-joblist');
        var spinner = new Spinner(opts).spin();
        spinnerParent.appendChild(spinner);

        jQuery.ajax({
            type: 'POST',
            url: listJobsLink,
            success: function (result) {
                //result = result.replace("<html>","").replace("<body>","").replace("</html>","").replace("</body>","");
                $('#jobtable tbody').empty();
                $('#jobtable tbody').replaceWith(result);
                initTable(nextJobExecutionLink);
                // to prevent flickering:
                JobStatusUpdater.repeatFn(false);
                spinner.stop();

                var o = $('#jobtable');
                var $win = $(window)
                    , $head = $('thead.header', o)
                    , isFixed = 0;
                o.find('thead.header > tr > th').each(function (i, h) {
                    var w = $(h).width();
                });

            },
            error: function (result) {
                console.log(result);
                spinner.stop();
            }
        });
    }
};

/**
 * Called on jquerys DOM-ready.
 * Initializes DOM-nodes and registers events.
 */
function doOnDomReady(getRunningAndRecentlyFinishedJobsLink,
                      cancelJobRunLink,
                      getLastRunLink,
                      nextExecutionLink) {

    $('[data-toggle="popover"]').popover();
    $('#checkAll').on('click', function () {
        // set checked attribute on fixed-header
        $('#checkAll').prop("checked", this.checked);
        $('#checkAll-copy').prop("checked", this.checked);
        $('.jobCheckbox').filter(function (index, elem) {
            return $(elem).parent().parent().is(':visible')
        }).prop('checked', this.checked);
    });

    // pass along filter settings when sorting columns:
    $('thead a').on('click', function (e) {
        $('form').attr('action', $(this).attr('href')).trigger('submit');
        return false;
    });

    initTable(nextExecutionLink);

    JobStatusUpdater.initLoop(
        getRunningAndRecentlyFinishedJobsLink,
        cancelJobRunLink,
        getLastRunLink,
        5000
    );

    $("#jobtable").stickyTableHeaders(0);

    initEventHandlers();

    function initEventHandlers() {

        $("#actionForSelected").on('click', function () {
            $("#remove-tag-select .tagLink").remove();
            $.ajax({
                url: OpenSpeedMonitor.urls.jobTags.getTagsForJobs,
                data: {jobIds: JSON.stringify(getSelectedJobIds())}
            }).done(function (result) {
                fillDropdown(result.tags);
            });
        });
        $("#add-tag-confirm-button").on('click', function () {
            var tag = $("#add-tag-input").val();
            $.ajax({
                url: OpenSpeedMonitor.urls.jobTags.addTagToJobs,
                data: {
                    jobIds: JSON.stringify(getSelectedJobIds()),
                    tag: tag
                }
            }).done(function () {
                addTag(tag);
            })
        });

        $('.chosen-select').on('change', function(evt, params) {
            removeTag(params.selected);
        });
    }


    function getSelectedJobIds() {
        var ids = [];
        $(".jobCheckbox:checked").each(function () {
            ids.push($(this).attr("id").split(".")[1]);
        });
        return ids;
    }

    function fillDropdown(tags) {
        var selectContainer = $("#remove-tag-select");
        tags.forEach(function (tag) {
            var tagElem = $("<option class='tagLink'><a href='#'>" + tag + "</a></option>");
            tagElem.on('click', removeTag);
            selectContainer.append(tagElem);
        });
        $("#remove-tag-select").val("0");
        $("#remove-tag-select").parent().on('click', function (e) {
            e.stopPropagation();
        });
        $(".chosen-select").chosen({search_contains: true});
        $('.chosen-select').trigger('chosen:updated');
    }

    function unhighlightRows() {
        $(".highlight").each(function () {
            $(this).removeClass("highlight success failure");
        });
    }

    function removeTag(selectedTag) {
        $.ajax({
            url: OpenSpeedMonitor.urls.jobTags.removeTag,
            data: {
                jobIds: JSON.stringify(getSelectedJobIds()),
                tag: selectedTag
            }

        }).done(function () {
            unhighlightRows();
            $(".jobCheckbox:checked").each(function () {
                // append tag to data-tag-attribute
                var row = $(this).closest("tr");
                if (row.attr("data-tags")) {
                    row.attr("data-tags", row.attr("data-tags").replace(selectedTag, '').replace(",,", ","));
                }
                row.addClass("highlight success");
                row.find(".tags li").filter(function () {
                    return $(this).text() === selectedTag;
                }).each(function () {
                    $(this).remove();
                });
            });
            OpenSpeedMonitor.jobListFilter.filter();
        });
        $("#remove-tag-select").val("0");
        $('.chosen-select').trigger('chosen:updated');
        $("#actionForSelectedContainer").removeClass("open");
    }

    function addTag(tag) {
        unhighlightRows();

        $(".jobCheckbox:checked").each(function () {
            // append tag to data-tag-attribute
            var row = $(this).closest("tr");
            if (!row.attr("data-tags") || row.attr("data-tags") === "") {
                row.attr("data-tags", tag)
            } else {
                row.attr("data-tags", row.attr("data-tags") + "," + tag);
            }
            row.addClass("highlight success");
            var tagAlreadyExists = row.find(".tags li").filter(function () {
                    return $(this).text() === tag
                }).length >= 1;
            if (!tagAlreadyExists) {
                row.find(".tags").append("<li>" + tag + "</li>");
            }
        });

        OpenSpeedMonitor.jobListFilter.filter();
    }

}

function doOnWindowLoad(listLink, nextExecutionLink) {

    if ($("tr.highlight").length > 0) {
        $('html, body').animate({
            scrollTop: $("tr.highlight").offset().top - 100
        }, 1000);
    }

}
