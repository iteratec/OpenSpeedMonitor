package de.iteratec.osm.result

enum PerformanceAspectType {
    PAGE_CONSTRUCTION_STARTED(Measurand.START_RENDER, 'fas fa-hourglass-start', Unit.MILLISECONDS),
    PAGE_SHOWS_USEFUL_CONTENT(Measurand.VISUALLY_COMPLETE, 'fas fa-eye', Unit.MILLISECONDS),
    PAGE_IS_USABLE(Measurand.CONSISTENTLY_INTERACTIVE, 'fas fa-hand-pointer', Unit.MILLISECONDS)

    Measurand defaultMetric
    String icon
    Unit unit

    private PerformanceAspectType(Measurand value, String i, Unit u) {
        defaultMetric = value
        icon = i
        unit = u
    }
}