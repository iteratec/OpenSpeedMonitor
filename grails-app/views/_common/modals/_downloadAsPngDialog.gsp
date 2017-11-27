<!-- Modal dialog -->
<div id="downloadAsPngModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="ModalLabel"
     aria-hidden="true">
    <div id="downloadAsPngDialog" class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 id="ModalLabel" class="modal-title"><g:message code="de.iteratec.osm.pngDownloader.modal.title"
                                                                   default="Download as PNG"/></h4>
            </div>

            <form class="form-horizontal modal-body">
                <div class="form-group">
                    <label for="inputFilename" class="col-xs-2 control-label"><g:message code="de.iteratec.osm.pngDownloader.modal.filename"
                                                                                         default="filename"/></label>
                    <div class="col-xs-4">
                        <input type="text" class="form-control" id="inputFilename" value="download.png">
                    </div>
                    <label for="pngWidth" class="col-xs-2 control-label"><g:message code="de.iteratec.chart.width.name"/></label>
                    <div class="col-xs-2">
                        <input type="number" id="pngWidth" min="0" step="1"/>
                    </div>
                </div>
            </form>
            <div id="resize-east">
                <div id="not-resize-east">
                    <div id="download-chart-container"></div>
                    <div class="ui-resizable-handle ui-resizable-se ui-icon ui-icon-grip-diagonal-se"></div>
                </div>
            </div>
            <div class="modal-footer">
                <button href="#" class="btn btn-primary pull-right" id="downloadConfirm"
                        onclick="downloadPNG()">
                    <g:message code="de.iteratec.osm.pngDownloader.modal.confirmButton" default="download"/>
                </button>
            </div>
        </div>
    </div>
</div>

<asset:script type="text/javascript">
    $(window).load(function () {
        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/pngDownloader.js" />', 'pngDownloadModal');
        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/bower_components/saveSvgAsPng/saveSvgAsPng.js"/>', 'pngDownloader');
    });

    function downloadPNG() {
        var svgNode = document.querySelector("#download-chart-container svg");
        var filename = $("#inputFilename").val();
        filename = filename.endsWith(".png") ? filename : filename += ".png";

        saveSvgAsPng(svgNode, filename);
        $('#downloadAsPngModal').modal('hide');
    }
</asset:script>
