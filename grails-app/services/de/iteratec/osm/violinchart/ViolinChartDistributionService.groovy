package de.iteratec.osm.violinchart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.distributionData.GetViolinchartCommand
import de.iteratec.osm.distributionData.Violin
import de.iteratec.osm.distributionData.ViolinChartDTO
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
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
        SelectedMeasurand selectedMeasurand = new SelectedMeasurand(cmd.measurand, CachedView.UNCACHED)
        List<EventResultProjection> distributions = getResultProjecions(cmd, from, to, selectedMeasurand)
        return buildDTO(distributions, jobGroups, pages, selectedMeasurand)
    }

    private List<EventResultProjection> getResultProjecions(GetViolinchartCommand cmd, Date from, Date to, selectedMeasurand) {
        EventResultQueryBuilder queryBuilder = (EventResultQueryBuilder) performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - with builder', 1) {
             return new EventResultQueryBuilder()
                    .withJobResultDateBetween(from, to)
                    .withJobGroupIdsIn(cmd.jobGroups as List)
                    .withPageIdsIn(cmd.pages as List)
                    .withSelectedMeasurands([selectedMeasurand])
        }
        List<EventResultProjection> distributions = (List<EventResultProjection>) performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - actually query the data', 2) {
            queryBuilder.getRawData(EventResultQueryBuilder.MetaDataSet.NONE)
        }
        return distributions
    }

    private ViolinChartDTO buildDTO(List<EventResultProjection> distributions, List<JobGroup> allJobGroups, List<Page> allPages, selectedMeasurand) {
        ViolinChartDTO violinChartDTO = new ViolinChartDTO()
        if(distributions.any {it."${selectedMeasurand.getDatabaseRelevantName()}" != null}){
            performanceLoggingService.logExecutionTime(DEBUG, "create DTO for DistributionChart", 1) {
                distributions.each {EventResultProjection eventResultProjection ->
                    if(eventResultProjection."${selectedMeasurand.getDatabaseRelevantName()}"){

                        JobGroup jobGroup = allJobGroups.find{jobGroup -> jobGroup.id == eventResultProjection.jobGroupId}
                        Page page = allPages.find{page -> page.id == eventResultProjection.pageId}
                        String identifier = "${page} | ${jobGroup.name}"
                        Violin violin = violinChartDTO.series.find({ it.identifier == identifier })
                        if (!violin) {
                            violin = new Violin(identifier: identifier, jobGroup: jobGroup.name, page: page);
                            violinChartDTO.series.add(violin);
                        }
                        double value = selectedMeasurand.normalizeValue(eventResultProjection."${selectedMeasurand.getDatabaseRelevantName()}");
                        violin.data.add(value)
                    }
                }
            }
            return violinChartDTO
        }else {
            return null
        }
    }
}