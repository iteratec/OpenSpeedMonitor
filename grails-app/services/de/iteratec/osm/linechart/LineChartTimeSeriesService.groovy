package de.iteratec.osm.linechart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.barchart.BarchartAggregation
import de.iteratec.osm.barchart.GetBarchartCommand
import de.iteratec.osm.barchart.PageComparisonAggregation
import de.iteratec.osm.csi.Page
import de.iteratec.osm.d3Data.GetPageComparisonDataCommand
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.DeviceType
import de.iteratec.osm.result.OperatingSystem
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.EventResultQueryBuilder
import de.iteratec.osm.util.I18nService
import grails.gorm.transactions.Transactional

@Transactional
class LineChartTimeSeriesService {
    List<LinechartTimeSeries> getLinechartTimeSeriesFor(GetLinechartCommand cmd) {
        Date from = cmd.from.toDate()
        Date to = cmd.to.toDate()

        List<SelectedMeasurand> measurands = cmd.measurands.collect {
            new SelectedMeasurand(it, CachedView.UNCACHED)
        }

        List<JobGroup> jobGroups = null
        if (cmd.jobGroups) {
            jobGroups = JobGroup.findAllByIdInList(cmd.jobGroups)
        }

        EventResultQueryBuilder queryBuilder = new EventResultQueryBuilder()
                .withJobResultDateBetween(from, to)
                .withSelectedMeasurands(measurands)
                .withJobGroupIn(jobGroups)

        if(cmd.pages) {
            List<Page> pages = Page.findAllByIdInList(cmd.pages)
            queryBuilder.withPageIn(pages)
        }

        List<EventResultProjection> eventResultProjections = queryBuilder.getRawData()
        List <LinechartTimeSeries> result = []
        eventResultProjections.each { eventResult ->
            result += ['date': eventResult.jobResultDate,
                       'value': eventResult.docCompleteTimeInMillisecs]
        }

        return result
    }
}