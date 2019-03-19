package de.iteratec.osm.result

enum PerformanceAspectType {
    PAGE_CONSTRUCTION_STARTED(Measurand.START_RENDER),
    PAGE_SHOWS_USEFUL_CONTENT(Measurand.VISUALLY_COMPLETE),
    PAGE_IS_USABLE(Measurand.CONSISTENTLY_INTERACTIVE)

    private Measurand defaultMetric

    private PerformanceAspectType(Measurand value){
        defaultMetric = value
    }
}