package de.iteratec.osm.result

enum Measurand{
    DOC_COMPLETE_TIME(MeasurandGroup.LOAD_TIMES,"docCompleteTimeInMillisecs","doc-complete", "docTime"),
    DOM_TIME(MeasurandGroup.LOAD_TIMES,"domTimeInMillisecs","dom-time", "domTime"),
    FIRST_BYTE(MeasurandGroup.LOAD_TIMES, "firstByteInMillisecs","ttfb", "TTFB"),
    FULLY_LOADED_REQUEST_COUNT(MeasurandGroup.REQUEST_COUNTS, "fullyLoadedRequestCount","requests-fully-loaded", "requests"),
    FULLY_LOADED_TIME(MeasurandGroup.LOAD_TIMES, "fullyLoadedTimeInMillisecs", "fully-loaded", "fullyLoaded"),
    LOAD_TIME(MeasurandGroup.LOAD_TIMES, "loadTimeInMillisecs","load-time", "loadTime"),
    START_RENDER(MeasurandGroup.LOAD_TIMES, "startRenderInMillisecs", "start-render","render"),
    DOC_COMPLETE_INCOMING_BYTES(MeasurandGroup.REQUEST_SIZES,"docCompleteIncomingBytes","bytes-doc-complete", "bytesInDoc"),
    DOC_COMPLETE_REQUESTS(MeasurandGroup.REQUEST_COUNTS, "docCompleteRequests", "requests-doc-complete","requestsDoc"),
    FULLY_LOADED_INCOMING_BYTES(MeasurandGroup.REQUEST_SIZES, "fullyLoadedIncomingBytes", "bytes-fully-loaded","bytesIn"),
    SPEED_INDEX(MeasurandGroup.LOAD_TIMES, "speedIndex","speed-index", "SpeedIndex"),
    VISUALLY_COMPLETE_85(MeasurandGroup.LOAD_TIMES, "visuallyComplete85InMillisecs", "visually-complete-85","visualComplete85"),
    VISUALLY_COMPLETE_90(MeasurandGroup.LOAD_TIMES, "visuallyComplete90InMillisecs", "visually-complete-90", "visualComplete90"),
    VISUALLY_COMPLETE_95(MeasurandGroup.LOAD_TIMES, "visuallyComplete95InMillisecs", "visually-complete-95", "visualComplete95"),
    VISUALLY_COMPLETE_99(MeasurandGroup.LOAD_TIMES, "visuallyComplete99InMillisecs", "visually-complete-99", "visualComplete99"),
    VISUALLY_COMPLETE(MeasurandGroup.LOAD_TIMES, "visuallyCompleteInMillisecs", "visually-complete", "visualComplete"),
    CS_BY_WPT_DOC_COMPLETE(MeasurandGroup.PERCENTAGES, "csByWptDocCompleteInPercent", "cs-by-wpt-doc-complete", null),
    CS_BY_WPT_VISUALLY_COMPLETE(MeasurandGroup.PERCENTAGES, "csByWptVisuallyCompleteInPercent", "cs-by-wpt-visually-complete", null),
    FIRST_INTERACTIVE(MeasurandGroup.LOAD_TIMES, "firstInteractiveInMillisecs", "first-interactive", "FirstInteractive"),
    CONSISTENTLY_INTERACTIVE(MeasurandGroup.LOAD_TIMES, "consistentlyInteractiveInMillisecs", "consistently-interactive", "TimeToInteractive"),
    JS_TOTAL(MeasurandGroup.REQUEST_SIZES, "jsTotal", "javascript-total", "js"),
    IMAGE_TOTAL(MeasurandGroup.REQUEST_SIZES, "imageTotal", "image-total", "image"),
    CSS_TOTAL(MeasurandGroup.REQUEST_SIZES, "cssTotal", "css-total", "css"),
    HTML_TOTAL(MeasurandGroup.REQUEST_SIZES, "htmlTotal", "html-total", "html")

    private MeasurandGroup group
    private String eventResultField
    private String graphiteLabelSuffix
    private String tagInResultXml

    private Measurand(MeasurandGroup value, String name, String label, String tag){
        group = value
        eventResultField = name
        graphiteLabelSuffix = label
        tagInResultXml = tag
    }

    MeasurandGroup getMeasurandGroup(){
        return group
    }
    String getEventResultField(){
        return eventResultField
    }
    String getGrapthiteLabelSuffix(){
        return graphiteLabelSuffix
    }
    String getTagInResultXml(){
        return tagInResultXml
    }
}