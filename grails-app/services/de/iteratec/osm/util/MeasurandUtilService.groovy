package de.iteratec.osm.util

import de.iteratec.osm.report.chart.MeasurandGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.ResultCsiAggregationService
import grails.transaction.Transactional

@Transactional
class MeasurandUtilService {
    public final
    static Map<CachedView, Map<String, List<String>>> AGGREGATOR_GROUP_VALUES = ResultCsiAggregationService.getAggregatorMapForOptGroupSelect()

    I18nService i18nService

    String getDimensionalUnit(String measurand) {
        measurand = parseMeasurandString(measurand)

        def aggregatorGroup = AGGREGATOR_GROUP_VALUES.get(CachedView.UNCACHED)
        if (aggregatorGroup.get(MeasurandGroup.LOAD_TIMES).contains(measurand)) {
            return "ms"
        } else if (aggregatorGroup.get(MeasurandGroup.PERCENTAGES).contains(measurand)) {
            return "%"
        } else if (aggregatorGroup.get(MeasurandGroup.REQUEST_COUNTS).contains(measurand)) {
            return "#"
        } else if (aggregatorGroup.get(MeasurandGroup.REQUEST_SIZES).contains(measurand)) {
            return "kb"
        } else {
            return ""
        }
    }

    String getAxisLabel(String measurand) {
        measurand = parseMeasurandString(measurand)

        def aggregatorGroup = AGGREGATOR_GROUP_VALUES.get(CachedView.UNCACHED)
        if (aggregatorGroup.get(MeasurandGroup.LOAD_TIMES).contains(measurand)) {
            return i18nService.msg("de.iteratec.osm.measurandGroup.loadTimes.yAxisLabel", "Loading Time [ms]")
        } else if (aggregatorGroup.get(MeasurandGroup.PERCENTAGES).contains(measurand)) {
            return i18nService.msg("de.iteratec.osm.measurandGroup.percentages.yAxisLabel", "Percent [%]")
        } else if (aggregatorGroup.get(MeasurandGroup.REQUEST_COUNTS).contains(measurand)) {
            return i18nService.msg("de.iteratec.osm.measurandGroup.requestCounts.yAxisLabel", "Amount")
        } else if (aggregatorGroup.get(MeasurandGroup.REQUEST_SIZES).contains(measurand)) {
            return i18nService.msg("de.iteratec.osm.measurandGroup.requestSize.yAxisLabel", "Size [kb]")
        } else {
            return ""
        }

    }

    String getI18nMeasurand(String measurand) {
        measurand = parseMeasurandString(measurand)
        return i18nService.msg("de.iteratec.isr.measurand.${measurand}", measurand)
    }

    private String parseMeasurandString(String measurand) {
        if (!measurand.endsWith("Uncached") && !measurand.endsWith("Cached")) measurand += "Uncached"
        else if (measurand.endsWith("Cached")) measurand = measurand.replace("Cached", "Uncached")
        return measurand
    }
}
