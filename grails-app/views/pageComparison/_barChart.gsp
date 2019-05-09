<div id="chart-container">
    <div id="filter-dropdown-group" class="btn-group">
        <div class="btn-group pull-left perc-element" data-toggle="buttons" id="aggregationValueSwitch">
            <label class="btn btn-sm btn-default active" id="averageButton"><input type="radio" name="aggregationValue"
                                                                                   value="avg"
                                                                                   checked>Average</label>
        <label class="btn btn-sm btn-default" id="percentileButton"><input type="radio" name="aggregationValue"
                                                                       value="percentile">
            ${g.message(code: 'de.iteratec.isocsi.aggregationChart.percentile.label', 'default': 'Percentile')}</label>
        </div>
        <input class="btn btn-sm btn-default perc-element"
               id="percentageField"
               type="number"
               placeholder="%"
               value="50"
               min="0" max="100"/>
        <input class="form-control perc-element perc-slider"
               id="percentageSlider"
               type="range"
               min="0" max="100"
               step="5"/>
    </div>
    <div class="in-chart-buttons">
        <a href="#downloadAsPngModal" id="download-as-png-button"
           data-toggle="modal" role="button" onclick="initPngDownloadModal()"
           title="${message(code: 'de.iteratec.ism.ui.button.save.name', default:'Download as PNG')}">
            <i class="fas fa-download"></i>
        </a>
    </div>

    <div id="svg-container">
        <svg id="page-comparison-svg" class="d3chart" width="100%"></svg>
    </div>
</div>
