package de.iteratec.osm.result

enum PerformanceAspectType {
    PAGE_CONSTRUCTION_STARTED(Measurand.START_RENDER, 'hourglass-start'),
    PAGE_SHOWS_USEFUL_CONTENT(Measurand.VISUALLY_COMPLETE, 'eye'),
    PAGE_IS_USABLE(Measurand.CONSISTENTLY_INTERACTIVE, 'hand-pointer')

    Measurand defaultMetric
    String icon

    private PerformanceAspectType(Measurand value, String i) {
        defaultMetric = value
        icon = i
    }
}