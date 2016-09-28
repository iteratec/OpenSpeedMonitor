/**
 * Created by marko on 27.09.16.
 */

function sortBy(columnToSortByParameter){
    if(columnToSortBy == columnToSortByParameter){
        sortingDirection = sortingDirection=="asc"?"desc":"asc";
    }else{
        columnToSortBy = columnToSortByParameter;
        sortingDirection = "desc";
    }
    updateElementTable(updateTableUrl);
}

function init(updateTableUrlParameter, i18nParameter, columnToSortByParameter) {
    updateInProgress = false;
    updateRequired = false;
    updateTableUrl=updateTableUrlParameter;
    columnToSortBy = columnToSortByParameter;
    sortingDirection = "desc";
    i18n = i18nParameter;
    curr = 0;
    max = $('#elementsPerPage').val();
    lastFilter = $('#elementNameFilter').val();
    offset = 0;
    limitResults = true;
    onlyActive = false;

    $('#elementsPerPage').on("input",function (element) {
        var max_temp = $('#elementsPerPage').val();
        if(max_temp > 0) {
            max = max_temp;
            curr = 0;
            offset = 0;
            updateElementTable(updateTableUrl);
        }
    });
    $('#elementFilter').on("input",function (element) {
        lastFilter = $('#elementFilter').val();
        curr = 0;
        offset = 0;
        updateElementTable(updateTableUrl);
    });

    $('#limitResultsCheckbox').change(function () {
        console.log($('#limitResultsCheckbox').is(":checked"));
        limitResults = $('#limitResultsCheckbox').is(":checked");
        updateElementTable(updateTableUrlParameter);
    });
    // updateIfNecessary(updateTableUrl, checkUrl, rowUpdateUrl);
    updateElementTable(updateTableUrl);
}

// Based on http://stackoverflow.com/questions/17390179/using-bootstrap-to-paginate-a-set-number-of-p-elements-on-a-webpage
function createPagination(size,updateTableUrl){
    var perPage = max;
    var numItems = size;
    var numPages = Math.ceil(numItems/perPage);
    console.log(numItems);
    if (numItems >= 1000){
        var limitResultsCheckboxContainer = document.getElementById("limitResultsCheckboxContainer");
        limitResultsCheckboxContainer.style.display = 'block';
    }

    var myNode = document.getElementById("elementPager");
    myNode.innerHTML = '';
    var i = 0;
    if (numPages < 10) {
        while (numPages > i) {
            if(i == curr) {
                $('<li><a style="background-color: rgb(219,219,219);" href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#elementPager');
            }else{
                $('<li><a href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#elementPager');
            }
            i++;
        }
    }else{
        var max_i = 8-curr;
        if(curr >=5 ) {
            $('<li><a href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#elementPager');
            if( curr >= 5 ) $('<li><a href="#" class="page_link">' + "..." + '</a></li>').appendTo('#elementPager');
            i = curr - 3;
            max_i = 4
        }
        if(i + 7 >=numPages) i= numPages -7;
        while (curr+max_i > i && numPages> i) {
            if(i == curr) {
                $('<li><a style="background-color: rgb(219,219,219);" href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#elementPager');
            }else{
                $('<li><a href="#" class="page_link">' + (i + 1) + '</a></li>').appendTo('#elementPager');
            }
            i++;
        }
        if(i!= numPages) {
            if(i+1 != numPages)$('<li><a href="#" class="page_link">' + "..." + '</a></li>').appendTo('#elementPager');
            $('<li><a href="#" class="page_link">' + (numPages) + '</a></li>').appendTo('#elementPager');
        }
    }

    $('#elementPager .page_link:first').addClass('active');


    $('#elementPager li a').click(function(){
        if($.isNumeric( $(this).html())) {
            var clickedPage = $(this).html() - 1;
            goTo(clickedPage, perPage);
        }
    });
    $('<li><a href="#" class="prevLink">'+i18n["previous"]+'</a></li>').click(function () {
        previous();
    }).prependTo('#elementPager');
    $('<li><a href="#" class="nextLink">'+i18n["next"]+'</a></li>').click(function(){
        next();
    }).appendTo('#elementPager');

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
        updateElementTable(updateTableUrl)

    }
}

/**
 * updates the batchActivity table
 * @param updateTableUrl url to updateTableMethod within BatchActivityController
 */
function updateElementTable(updateTableUrl) {
    if(updateInProgress){
        updateRequired = true;
    } else {
        updateInProgress = true;
        jQuery.ajax({
            type: 'GET',
            url: updateTableUrl,
            data: {
                filter: lastFilter,
                offset: offset,
                max: max,
                onlyActive: onlyActive,
                sort: columnToSortBy,
                order: sortingDirection,
                limitResults:limitResults
            },
            success: function (content) {
                var jsonResponse = JSON.parse(content);
                $("#elementTable").html(jsonResponse.table);
                createPagination(jsonResponse.count, updateTableUrl)
                $('#elementFilter').focus();
                updateInProgress = false;
                if(updateRequired){
                    updateRequired=false;
                    updateElementTable(updateTableUrl);
                }
            },
            error: function (content) {
            }
        });
    }
}
