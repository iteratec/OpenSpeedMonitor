function initPngDownloadModal() {
    var downloadContainer = document.getElementById("download-chart-container");
    var originalContainer = document.getElementById("svg-container");
    var resizeHandle = document.getElementById("resize-east");
    var modalDialog = document.getElementById("downloadAsPngDialog");
    var widthField = document.getElementById("pngWidth");
    var svgElement = originalContainer.getElementsByTagName("svg")[0];
    var placeholder = svgElement.cloneNode(true);
    var width = originalContainer.offsetWidth;
    //OpenSpeedMonitor.ChartComponents.common.ComponentMargin is only defined for bar charts.
    //A sideMargin of 25px looks good for distribution charts
    var sideMargin = OpenSpeedMonitor.ChartComponents && OpenSpeedMonitor.ChartComponents.common &&
                        OpenSpeedMonitor.ChartComponents.common.ComponentMargin ?
                            OpenSpeedMonitor.ChartComponents.common.ComponentMargin + 10 : 25;
    var minWidthEstimate = 200 + 2 * sideMargin;
    var startX;
    var origCursor = document.body.style.cursor;
    var origSvgCursor = svgElement.style.cursor;

    placeholder.id = "svg-placeholder";
    downloadContainer.appendChild(svgElement);
    originalContainer.appendChild(placeholder);

    function getSvgMaxWidth() {
        //We need to encapsulated this inside a function and can't store it in a variable because the modalDialog
        //is hidden at the beginning when initPngDownloadModal() executes and it therefore has width 0.
        var resizeHandleMargin = parseInt(window.getComputedStyle(resizeHandle).marginLeft.slice(0, -2));
        return modalDialog.offsetWidth - sideMargin - resizeHandleMargin;
    }

    function getValidatedWidth(width) {
        return Math.min(Math.max(width, minWidthEstimate), getSvgMaxWidth())
    }

    function setContainerWidth(containerWidth) {
        downloadContainer.style.width = getValidatedWidth(containerWidth) + "px";
        widthField.value = downloadContainer.offsetWidth;
        window.dispatchEvent(new Event("resize"));
    }

    function reactToModalDialogResize() {
        if (downloadContainer.offsetWidth > getSvgMaxWidth()) {
            setContainerWidth(0);
            widthField.max = getSvgMaxWidth();
        }
    }

    function doDrag(e) {
        setContainerWidth(width + e.clientX - startX);
    }

    function stopDrag() {
        document.documentElement.removeEventListener("mousemove", doDrag);
        document.documentElement.removeEventListener("mouseup", stopDrag);
        document.body.style.cursor = origCursor;
        svgElement.style.cursor = origSvgCursor;
    }

    function initDrag(e) {
        document.body.style.cursor = svgElement.style.cursor = "ew-resize";
        startX = e.clientX;
        width = downloadContainer.offsetWidth;
        document.documentElement.addEventListener("mousemove", doDrag);
        document.documentElement.addEventListener("mouseup", stopDrag);
    }

    function onWidthFieldInput() {
        setContainerWidth(widthField.value);
    }

    downloadContainer.style.width = width + "px";
    widthField.value = width;
    widthField.min = minWidthEstimate;
    widthField.max = getSvgMaxWidth();
    window.addEventListener("resize", reactToModalDialogResize);
    resizeHandle.addEventListener("mousedown", initDrag);
    widthField.addEventListener("change", onWidthFieldInput);

    $("#downloadAsPngModal").on("hide.bs.modal", function restoreSvg() {
        window.removeEventListener("resize", reactToModalDialogResize);
        widthField.removeEventListener("onchange", setContainerWidth);
        originalContainer.removeChild(placeholder);
        originalContainer.appendChild(svgElement);
        window.dispatchEvent(new Event("resize"));
        $("#downloadAsPngModal").off("hide.bs.modal", restoreSvg);
    });
}