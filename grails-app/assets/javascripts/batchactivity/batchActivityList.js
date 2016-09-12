/**
 * Refreshes the table data every 2 seconds
 * @param updateTableUrl
 *          url to update complete table (method updateTableMethod within BatchActivityController)
 * @param checkUrl
 *          url to check if active BatchActivities exist (method checkForUpdate within BatchActivityController)
 * @param rowUpdateUrl
 *          url to update all rows (method getUpdate within BatchActivityController)
 */

function init(updateTableUrl, checkUrl, rowUpdateUrl,batchActivityCount) {
    curr = 0;
    max = $('#batchElementsPerPage').val();
    lastFilter = $('#batchNameFilter').val();
    offset = 0;

    $('#batchElementsPerPage').on("keyup input",function (element) {
        var max_temp = $('#batchElementsPerPage').val();
        if(max_temp > 0) {
            max = max_temp;
            curr = 0;
            offset = 0;
            updateBatchActivityTable(updateTableUrl);
        }
    });
    $('#batchNameFilter').on("keyup input",function (element) {
        lastFilter = $('#batchNameFilter').val();
        curr = 0;
        offset = 0;
        updateBatchActivityTable(updateTableUrl);
    });
    updateIfNecessary(updateTableUrl, checkUrl, rowUpdateUrl);
    createPagination(batchActivityCount,updateTableUrl);
}

// Based on http://stackoverflow.com/questions/17390179/using-bootstrap-to-paginate-a-set-number-of-p-elements-on-a-webpage
function createPagination(size,updateTableUrl){
    var perPage = max;
    var numItems = size;
    var numPages = Math.ceil(numItems/perPage);


    var myNode = document.getElementById("batchActivityPager");
    myNode.innerHTML = '';
    var i = 0;
    if (numPages < 10) {
        while (numPages > i) {
            if(i == curr) {
                $('<li><a style="background-color: rgb(219,219,219);" href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#batchActivityPager');
            }else{
                $('<li><a href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#batchActivityPager');
            }
            i++;
        }
    }else{
        var max_i = 8-curr;
        if(curr >=5 ) {
            $('<li><a href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#batchActivityPager');
            if( curr >= 5 ) $('<li><a href="#" class="page_link">' + "..." + '</a></li>').appendTo('#batchActivityPager');
            i = curr - 3;
            max_i = 4
        }
        if(i + 7 >=numPages) i= numPages -7;
        while (curr+max_i > i && numPages> i) {
            if(i == curr) {
                $('<li><a style="background-color: rgb(219,219,219);" href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#batchActivityPager');
            }else{
                $('<li><a href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#batchActivityPager');
            }
            i++;
        }
        if(i!= numPages) {
            if(i+1 != numPages)$('<li><a href="#" class="page_link">' + "..." + '</a></li>').appendTo('#batchActivityPager');
            $('<li><a href="#" class="page_link">' + (numPages) + '</a></li>').appendTo('#batchActivityPager');
        }
    }

    $('#batchActivityPager .page_link:first').addClass('active');


    $('#batchActivityPager li a').click(function(){
        if($.isNumeric( $(this).html())) {
            var clickedPage = $(this).html() - 1;
            goTo(clickedPage, perPage);
        }
    });
    $('<li><a href="#" class="prevLink">'+'Previous'+'</a></li>').click(function () {
        previous();
    }).prependTo('#batchActivityPager');
    $('<li><a href="#" class="nextLink">'+'Next'+'</a></li>').click(function(){
        next();
    }).appendTo('#batchActivityPager');

    function previous(){
        var goToPage = curr - 1;
        if(goToPage >= 0)
        goTo(goToPage);
    }

    function next(){
        goToPage = curr + 1;

        if(goToPage < numPages)
        goTo(goToPage);
    }

    function goTo(page){
        var startAt = page * perPage,
            endOn = startAt + perPage;
        offset = (page) * perPage;
        curr = page;
        updateBatchActivityTable(updateTableUrl)

    }
}

function updateIfNecessary(updateTableUrl, checkUrl, rowUpdateUrl) {
    setInterval(function () {
        var ids = collectActiveIds();
        jQuery.ajax({
            type: 'GET',
            url: checkUrl+"?activeCount="+ids.length,
            success: function (content) {
                if (content == "true") {
                    updateBatchActivityTable(updateTableUrl);
                }else {
                    if(ids.length > 0)updateRows(ids,rowUpdateUrl);
                }
            }
        });
    }, 2000);
}

/**
 * updates the batchActivity table
 * @param updateTableUrl url to updateTableMethod within BatchActivityController
 */
function updateBatchActivityTable(updateTableUrl) {
    jQuery.ajax({
        type: 'GET',
        url: updateTableUrl +"?filter="+lastFilter+"&offset="+offset +"&max="+max,
        success: function (content) {
            var jsonResponse = JSON.parse(content);
            $("#tabelle").html(jsonResponse.table);
            createPagination(jsonResponse.count,updateTableUrl)
        },
        error: function (content) {
        }
    });
}

/**
 * Updates all rows with the given ids
 * @param ids row ids to update
 * @param rowUpdateUrl url to get a row update
 */
function updateRows(ids, rowUpdateUrl) {

    jQuery.ajax({
        type: 'GET',
        url: rowUpdateUrl,
        traditional: true,
        data: {activeIds: ids},
        async: false,
        success: function (content) {
            $.each(content, function(i, update){
                updateRow(update);
            });
        },
        error: function (content) {
        }
    });

}
/**
 * Updates a single row with the given rowObject
 * @param rowObject([activity,endDate,htmlId,lastFailureMessage,lastUpdated,progress,startDate,status])
 */
function updateRow(rowObject) {
    var idxFieldActivity = 1;
    var idxFieldStatus = 2;
    var idxFieldStage = 3;
    var idxFieldStageProgress = 4;
    var idxFieldLastFailureMessage = 5;
    var idxFieldLastUpdated = 7;
    var idxFieldEndDate = 8;
    var idxFieldRemainingTime = 9;


    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldActivity+")").html(rowObject.activity);
    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldStatus+")").html(rowObject.status);
    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldStage+")").html(rowObject.stage);
    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldStageProgress+")").html(rowObject.progress);
    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldLastFailureMessage+")").html(rowObject.lastFailureMessage);
    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldLastUpdated+")").html(rowObject.lastUpdate);
    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldEndDate+")").html(rowObject.endDate);
    $("tr#" + rowObject.htmlId + " td:eq("+idxFieldRemainingTime+")").html(rowObject.remainingTime);
    $("tr#" + rowObject.htmlId).attr("status", rowObject.statusEN)
}
/**
 * Returns an array with all row ids where status = active
 * @returns {Array}
 */
function collectActiveIds() {
    var ids = [];
    $("[status='ACTIVE']").each(function (index, element) {
        ids.push([$(element).attr("id").replace("batchActivity_", "")]);
    });
    return ids;
}
