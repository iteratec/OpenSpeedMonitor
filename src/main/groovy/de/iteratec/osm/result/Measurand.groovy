package de.iteratec.osm.result

enum Measurand{
    DOC_COMPLETE_TIME(MeasurandGroup.LOAD_TIMES,"docCompleteTimeInMillisecs","doc-complete"),
    DOM_TIME(MeasurandGroup.LOAD_TIMES,"domTimeInMillisecs","dom-time"),
    FIRST_BYTE(MeasurandGroup.LOAD_TIMES, "firstByteInMillisecs","ttfb"),
    FULLY_LOADED_REQUEST_COUNT(MeasurandGroup.REQUEST_COUNTS, "fullyLoadedRequestCount","requests-fully-loaded"),
    FULLY_LOADED_TIME(MeasurandGroup.LOAD_TIMES, "fullyLoadedTimeInMillisecs", "fully-loaded"),
    LOAD_TIME(MeasurandGroup.LOAD_TIMES, "loadTimeInMillisecs","load-time"),
    START_RENDER(MeasurandGroup.LOAD_TIMES, "startRenderInMillisecs", "start-render"),
    DOC_COMPLETE_INCOMING_BYTES(MeasurandGroup.REQUEST_SIZES,"docCompleteIncomingBytes","bytes-doc-complete"),
    DOC_COMPLETE_REQUESTS(MeasurandGroup.REQUEST_COUNTS, "docCompleteRequests", "requests-doc-complete"),
    FULLY_LOADED_INCOMING_BYTES(MeasurandGroup.REQUEST_SIZES, "fullyLoadedIncomingBytes", "bytes-fully-loaded"),
    SPEED_INDEX(MeasurandGroup.LOAD_TIMES, "speedIndex","speed-index"),
    VISUALLY_COMPLETE_85(MeasurandGroup.LOAD_TIMES, "visuallyComplete85InMillisecs", "visually-complete-85"),
    VISUALLY_COMPLETE_90(MeasurandGroup.LOAD_TIMES, "visuallyComplete90InMillisecs", "visually-complete-90"),
    VISUALLY_COMPLETE_95(MeasurandGroup.LOAD_TIMES, "visuallyComplete95InMillisecs", "visually-complete-95"),
    VISUALLY_COMPLETE_99(MeasurandGroup.LOAD_TIMES, "visuallyComplete99InMillisecs", "visually-complete-99"),
    VISUALLY_COMPLETE(MeasurandGroup.LOAD_TIMES, "visuallyCompleteInMillisecs", "visually-complete"),
    CS_BY_WPT_DOC_COMPLETE(MeasurandGroup.PERCENTAGES, "csByWptDocCompleteInPercent", "cs-by-wpt-doc-complete"),
    CS_BY_WPT_VISUALLY_COMPLETE(MeasurandGroup.PERCENTAGES, "csByWptVisuallyCompleteInPercent", "cs-by-wpt-visually-complete"),
    FIRST_INTERACTIVE(MeasurandGroup.LOAD_TIMES, "firstInteractiveInMillisecs", "first-interactive"),
    CONSISTENTLY_INTERACTIVE(MeasurandGroup.LOAD_TIMES, "consistentlyInteractiveInMillisecs", "consistently-interactive")

    private MeasurandGroup group
    private String eventResultField
    private String graphiteLabelSuffix

    private Measurand(MeasurandGroup value, String name, String label){
        group = value
        eventResultField = name
        graphiteLabelSuffix = label
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
}