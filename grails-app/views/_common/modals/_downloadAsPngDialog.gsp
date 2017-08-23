<!-- Modal dialog -->
<div id="downloadAsPngModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="ModalLabel"
     aria-hidden="true">
    <div id="downloadAsPngDialog" class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 id="ModalLabel" class="modal-title"><g:message code="de.iteratec.osm.pngDownloader.modal.title"
                                                                   default="Download as PNG"/></h4>
            </div>
            <form class="form-horizontal modal-body">
                <div class="form-group">
                    <label for="inputFilename" class="col-sm-2 control-label"><g:message code="de.iteratec.osm.pngDownloader.modal.filename"
                                                                                         default="filename"/></label>
                    <div class="col-sm-10">
                        <input type="text" class="form-control" id="inputFilename" value="download.png">
                    </div>
                </div>

                <div class="form-group">
                    <label for="pngWidth" class="col-sm-2 control-label"><g:message code="de.iteratec.chart.width.name"
                                                                                    default="width"/></label>

                    <div class="col-sm-4">
                        <input type="number" id="pngWidth" min="0" step="1" data-bind="value:replyNumber"
                               value="400"/>
                    </div>
                    <label for="pngHeight" class="col-sm-2 control-label"><g:message code="de.iteratec.chart.height.name"
                                                                                     default="height"/></label>

                    <div class="col-sm-4">
                        <input type="number" id="pngHeight" min="0" step="1" data-bind="value:replyNumber"
                               value="200"/>
                    </div>
                </div>
            </form>

            <div class="modal-footer">
                <div class="row">
                    <div class="col-md-2 col-md-offset-9">
                        <button href="#" class="btn btn-primary" id="downloadConfirm"
                                onclick="downloadPNG('${chartContainerID}')">
                            <g:message code="de.iteratec.osm.pngDownloader.modal.confirmButton" default="download"/>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<asset:script type="text/javascript">
    $(window).load(function () {
        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="/bower_components/saveSvgAsPng/saveSvgAsPng.js"/>', 'pngDownloader');
    });

    function downloadPNG(chartContainerID) {
        var svgNode = document.querySelector("#" + chartContainerID + " svg");
        var filename = $("#inputFilename").val();
        filename = filename.endsWith(".png") ? filename : filename += ".png";
        var width = $("#pngWidth").val();
        var height = $("#pngHeight").val();

        saveSvgAsPng(svgNode, filename, {width: width, height: height});
        $('#downloadAsPngModal').modal('hide');
    }
    
    function setDefaultValues(chartContainerID) {
        var svg = $("#" + chartContainerID + " svg");
        $("#pngWidth").val(svg.width());
        $("#pngHeight").val(svg.height());
    }
</asset:script>
