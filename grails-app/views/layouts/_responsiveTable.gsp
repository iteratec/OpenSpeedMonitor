<div class="row">
    <div class="col-md-3">
        <div class="form-group">
            <br>
            <input placeholder="${message(code: 'de.iteratec.osm.responsiveTable.filter.lable', default: 'Filter')}"
                   type="text" class="form-control" id="elementFilter">
        </div>
    </div>

    <div class="col-md-6">
        <div align="center"><ul id="elementPager" class="pagination"></ul></div>
    </div>

    <div class="col-md-3">
        <div align="right" class="form-group">
            <label for="elementsPerPage">${message(code: 'de.iteratec.osm.responsiveTable.elementsPerPage.lable', default: 'Elements per page:')}</label>
            <input type="Number" class="form-control" id="elementsPerPage" value=100 min=1>
        </div>
    </div>
</div>

<div id="elementTable"/>

