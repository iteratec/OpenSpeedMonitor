<div id="chart-container">
    <div class="in-chart-buttons">
        <a href="#downloadAsPngModal" id="download-as-png-button"
           data-toggle="modal" role="button" onclick="setDefaultValues('svg-container')"
           title="${message(code: 'de.iteratec.ism.ui.button.save.name', default:'Download as PNG')}">
            <i class="fa fa-download"></i>
        </a>
    </div>

    <div id="svg-container">
    </div>
</div>
<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/pageComparison/pageComparisonChart.js" />', 'pageComparisonChart');
    });
</asset:script>
