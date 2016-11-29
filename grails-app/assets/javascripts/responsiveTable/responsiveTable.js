/**
 * Created by marko on 27.09.16.
 */


var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.responsiveTable = OpenSpeedMonitor.responsiveTable || {

    sortBy: function(columnToSortByParameter)
    {
        if (OpenSpeedMonitor.responsiveTable.columnToSortBy == columnToSortByParameter) {
            console.log(OpenSpeedMonitor.responsiveTable.sortingDirection);
            OpenSpeedMonitor.responsiveTable.sortingDirection = OpenSpeedMonitor.responsiveTable.sortingDirection == "asc" ? "desc" : "asc";
            console.log(OpenSpeedMonitor.responsiveTable.sortingDirection);
        } else {
            OpenSpeedMonitor.responsiveTable.columnToSortBy = columnToSortByParameter;
            OpenSpeedMonitor.responsiveTable.sortingDirection = "asc";
        }
        OpenSpeedMonitor.responsiveTable.updateElementTable();
    },

    init: function(updateTableUrlParameter, i18nParameter, columnToSortByParameter) {
        OpenSpeedMonitor.responsiveTable.updateInProgress = false;
        OpenSpeedMonitor.responsiveTable.updateRequired = false;
        OpenSpeedMonitor.responsiveTable.updateTableUrl = updateTableUrlParameter;
        OpenSpeedMonitor.responsiveTable.columnToSortBy = columnToSortByParameter;
        OpenSpeedMonitor.responsiveTable.sortingDirection = "desc";
        OpenSpeedMonitor.responsiveTable.i18n = i18nParameter;
        OpenSpeedMonitor.responsiveTable.curr = 0;
        OpenSpeedMonitor.responsiveTable.max = $('#elementsPerPage').val();
        OpenSpeedMonitor.responsiveTable.lastFilter = $('#elementNameFilter').val();
        OpenSpeedMonitor.responsiveTable.offset = 0;
        OpenSpeedMonitor.responsiveTable.onlyActive = false;

        $('#elementsPerPage').on("input", function (element) {
            var max_temp = $('#elementsPerPage').val();
            if (max_temp > 0) {
                OpenSpeedMonitor.responsiveTable.max = max_temp;
                OpenSpeedMonitor.responsiveTable.curr = 0;
                OpenSpeedMonitor.responsiveTable.offset = 0;
                OpenSpeedMonitor.responsiveTable.updateElementTable();
            }
        });
        $('#elementFilter').on("input", function (element) {
            OpenSpeedMonitor.responsiveTable.lastFilter = $('#elementFilter').val();
            OpenSpeedMonitor.responsiveTable.curr = 0;
            OpenSpeedMonitor.responsiveTable.offset = 0;
            OpenSpeedMonitor.responsiveTable.updateElementTable();
        });

        OpenSpeedMonitor.responsiveTable.updateElementTable();
    },

    // Based on http://stackoverflow.com/questions/17390179/using-bootstrap-to-paginate-a-set-number-of-p-elements-on-a-webpage
    createPagination: function(size) {
        var perPage = OpenSpeedMonitor.responsiveTable.max;
        var numItems = size;
        var numPages = Math.ceil(numItems / perPage);

        var myNode = document.getElementById("elementPager");
        myNode.innerHTML = '';
        var i = 0;
        if (numPages < 10) {
            while (numPages > i) {
                if (i == OpenSpeedMonitor.responsiveTable.curr) {
                    $('<li><a style="background-color: rgb(219,219,219);" href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#elementPager');
                } else {
                    $('<li><a href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#elementPager');
                }
                i++;
            }
        } else {
            var max_i = 8 - OpenSpeedMonitor.responsiveTable.curr;
            if (OpenSpeedMonitor.responsiveTable.curr >= 5) {
                $('<li><a href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#elementPager');
                if (OpenSpeedMonitor.responsiveTable.curr >= 5) $('<li><a href="#" class="page_link">' + "..." + '</a></li>').appendTo('#elementPager');
                i = OpenSpeedMonitor.responsiveTable.curr - 3;
                max_i = 4
            }
            if (i + 7 >= numPages) i = numPages - 7;
            while (OpenSpeedMonitor.responsiveTable.curr + max_i > i && numPages > i) {
                if (i == OpenSpeedMonitor.responsiveTable.curr) {
                    $('<li><a style="background-color: rgb(219,219,219);" href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#elementPager');
                } else {
                    $('<li><a href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#elementPager');
                }
                i++;
            }
            if (i != numPages) {
                if (i + 1 != numPages)$('<li><a href="#" class="page_link">' + "..." + '</a></li>').appendTo('#elementPager');
                $('<li><a href="#" class="page_link">' + (numPages) + '</a></li>').appendTo('#elementPager');
            }
        }

        $('#elementPager .page_link:first').addClass('active');


        $('#elementPager li a').click(function () {
            if ($.isNumeric($(this).html())) {
                var clickedPage = $(this).html() - 1;
                goTo(clickedPage, perPage);
            }
        });
        $('<li><a href="#" class="prevLink">' + OpenSpeedMonitor.responsiveTable.i18n["previous"] + '</a></li>').click(function () {
            previous();
        }).prependTo('#elementPager');
        $('<li><a href="#" class="nextLink">' + OpenSpeedMonitor.responsiveTable.i18n["next"] + '</a></li>').click(function () {
            next();
        }).appendTo('#elementPager');

        function previous() {
            var goToPage = OpenSpeedMonitor.responsiveTable.curr - 1;
            if (goToPage >= 0)
                goTo(goToPage);
        }

        function next() {
            goToPage = OpenSpeedMonitor.responsiveTable.curr + 1;

            if (goToPage < numPages)
                goTo(goToPage);
        }

        function goTo(page) {
            var startAt = page * perPage,
                endOn = startAt + perPage;
            OpenSpeedMonitor.responsiveTable.offset = (page) * perPage;
            OpenSpeedMonitor.responsiveTable.curr = page;
            OpenSpeedMonitor.responsiveTable.updateElementTable()

        }
    },

    /**
     * updates the batchActivity table
     * @param updateTableUrl url to updateTableMethod within BatchActivityController
     */
    updateElementTable: function() {
        if (OpenSpeedMonitor.responsiveTable.updateInProgress) {
            OpenSpeedMonitor.responsiveTable.updateRequired = true;
        } else {
            OpenSpeedMonitor.responsiveTable.updateInProgress = true;
            jQuery.ajax({
                type: 'GET',
                url: OpenSpeedMonitor.responsiveTable.updateTableUrl,
                data: {
                    filter: OpenSpeedMonitor.responsiveTable.lastFilter,
                    offset: OpenSpeedMonitor.responsiveTable.offset,
                    max: OpenSpeedMonitor.responsiveTable.max,
                    onlyActive: OpenSpeedMonitor.responsiveTable.onlyActive,
                    sort: OpenSpeedMonitor.responsiveTable.columnToSortBy,
                    order: OpenSpeedMonitor.responsiveTable.sortingDirection,
                },
                success: function (content) {
                    var jsonResponse = JSON.parse(content);
                    $("#elementTable").html(jsonResponse.table);
                    OpenSpeedMonitor.responsiveTable.createPagination(jsonResponse.count)
                    $('#elementFilter').focus();
                    OpenSpeedMonitor.responsiveTable.updateInProgress = false;
                    if (OpenSpeedMonitor.responsiveTable.updateRequired) {
                        OpenSpeedMonitor.responsiveTable.updateRequired = false;
                        OpenSpeedMonitor.responsiveTable.updateElementTable();
                    }
                },
                error: function (content) {
                }
            });
        }
    }
};