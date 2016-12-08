<div id="chart-container">
    <a href="#adjustBarchartModal" id="adjust-barchart-modal" data-toggle="modal" data-target="#adjustBarchartModal"
       class="hidden" onclick="initModalDialogValues()">
        <i class="fa fa-sliders"></i>
    </a>

    <div id="svg-container">
    </div>
</div>
<g:render template="/pageAggregation/adjustBarchartModal"/>
<asset:script type="text/javascript">
    $(window).load(function() {
      OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/dimple/barchart.js" absolute="true"/>',true,'barchart')
    });
</asset:script>
