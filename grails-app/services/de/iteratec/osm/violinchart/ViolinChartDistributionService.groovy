package de.iteratec.osm.violinchart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.distributionData.GetViolinchartCommand
import de.iteratec.osm.distributionData.Violin
import de.iteratec.osm.distributionData.ViolinChartDTO
import de.iteratec.osm.distributionData.ViolinDataPoint
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.EventResultQueryBuilder
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.transactions.Transactional

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG

@Transactional
class ViolinChartDistributionService {

    PerformanceLoggingService performanceLoggingService

    ViolinChartDTO getDistributionFor(GetViolinchartCommand cmd) {
        Date from = cmd.from.toDate()
        Date to = cmd.to.toDate()
        List<JobGroup> jobGroups = null
        if (cmd.jobGroups) {
            jobGroups = JobGroup.findAllByIdInList(cmd.jobGroups)
        }
        List<Page> pages = null
        if (cmd.pages) {
            pages = Page.findAllByIdInList(cmd.pages)
        }
        List<MeasuredEvent> measuredEvents = null
        if (cmd.measuredEvents) {
            measuredEvents = MeasuredEvent.findAllByIdInList(cmd.measuredEvents)
        }
        List<SelectedMeasurand> measurands = cmd.measurands.collect {
            new SelectedMeasurand(it, CachedView.UNCACHED)
        }

        List<EventResultProjection> eventResultProjections = getResultProjecions(cmd, from, to, measurands)
        return buildDTO(eventResultProjections, jobGroups, pages, measuredEvents)
    }

    private List<EventResultProjection> getResultProjecions(GetViolinchartCommand cmd, Date from, Date to, List<SelectedMeasurand> measurands) {
        EventResultQueryBuilder queryBuilder = (EventResultQueryBuilder) performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - with builder', 1) {
             return new EventResultQueryBuilder()
                    .withJobResultDateBetween(from, to)
                    .withJobGroupIdsIn(cmd.jobGroups as List)
                    .withPageIdsIn(cmd.pages as List)
                    .withLocationIdsIn(cmd.locations as List)
                    .withBrowserIdsIn(cmd.browsers as List)
                    .withMeasuredEventIdsIn(cmd.measuredEvents as List)
                    .withSelectedMeasurands(measurands)

            /*performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - append connectivities', 2) {
                appendConnectivity(queryBuildera, queryParams)
            }
            performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - append trims', 2) {
                appendTrims(queryBuildera, queryParams)
            }*/
        }
        List<EventResultProjection> eventResultProjections = (List<EventResultProjection>) performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - actually query the data', 2) {
            queryBuilder.getRawData()
        }
        return eventResultProjections
    }

    private ViolinChartDTO buildDTO(List<EventResultProjection> eventResultProjections, List<JobGroup> jobGroups, List<Page> pages, List<MeasuredEvent> measuredEvents) {
        ViolinChartDTO violinChartDTO = new ViolinChartDTO()
        performanceLoggingService.logExecutionTime(DEBUG, "create DTO for ViolinChart", 1) {
            eventResultProjections.each {EventResultProjection eventResultProjection ->
                MeasuredEvent measuredEvent
                Date date = (Date) eventResultProjection.jobResultDate;
                Double value = (Double) eventResultProjection.docCompleteTimeInMillisecs;
                JobGroup jobGroup = (JobGroup) jobGroups.find{jobGroup -> jobGroup.id == eventResultProjection.jobGroupId}
                String identifier = "${jobGroup.name}"
                if (measuredEvents) {
                    measuredEvent = (MeasuredEvent) measuredEvents.find{measuredEventEntry -> measuredEventEntry.id == eventResultProjection.measuredEventId}
                    identifier += " | ${measuredEvent?.name}"
                } else if (pages) {
                    measuredEvent = (MeasuredEvent) MeasuredEvent.findById(eventResultProjection.measuredEventId)
                    identifier += " | ${measuredEvent?.name}"
                }
                Violin violin = violinChartDTO.series.find({ it.identifier == identifier })
                if (!violin) {
                    violin = new Violin(identifier: identifier, jobGroup: jobGroup.name)
                    if (measuredEvent) {
                        violin.setMeasuredEvent(measuredEvent.name)
                    }
                    violinChartDTO.series.add(violin)
                }
                ViolinDataPoint violinDataPoint = new ViolinDataPoint(date: date, value: value)
                violin.data.add(violinDataPoint)
            }
        }

        return violinChartDTO
    }
}