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
import de.iteratec.osm.result.PerformanceAspectType
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
        List<SelectedMeasurand> measurands = cmd.measurands.collect {
            new SelectedMeasurand(it, CachedView.UNCACHED)
        }

        List<PerformanceAspectType> performanceAspectTypes = []
        if (cmd.performanceAspectTypes) {
            performanceAspectTypes = cmd.performanceAspectTypes.collect{it.toString().toUpperCase() as PerformanceAspectType}
        }

        List<EventResultProjection> distributions = getResultProjecions(cmd, from, to, measurands, performanceAspectTypes)
        return buildDTO(distributions, jobGroups, pages, measurands, performanceAspectTypes)
    }

    private List<EventResultProjection> getResultProjecions(GetViolinchartCommand cmd, Date from, Date to, List<SelectedMeasurand>  measurands, List<PerformanceAspectType> performanceAspectTypes) {
        List<EventResultProjection> distributions =  new EventResultQueryBuilder()
                .withJobResultDateBetween(from, to)
                .withJobGroupIdsIn(cmd.jobGroups as List)
                .withPageIdsIn(cmd.pages as List)
                .withSelectedMeasurands(measurands)
                .withPerformanceAspects(performanceAspectTypes)
                .getRawData(EventResultQueryBuilder.MetaDataSet.NONE)
        return distributions
    }

    private ViolinChartDTO buildDTO(List<EventResultProjection> distributions, List<JobGroup> allJobGroups, List<Page> allPages, List<SelectedMeasurand> measurands, List<PerformanceAspectType> performanceAspectTypes) {
        ViolinChartDTO violinChartDTO = new ViolinChartDTO()
        performanceLoggingService.logExecutionTime(DEBUG, "create DTO for DistributionChart", 1) {
            distributions.each {EventResultProjection eventResultProjection ->
                JobGroup jobGroup = (JobGroup) allJobGroups.find { jobGroup -> jobGroup.id == eventResultProjection.jobGroupId }
                Page page = (Page) allPages.find{page -> page.id == eventResultProjection.pageId}
                String identifier = "${page} | ${jobGroup.name}"
                Violin violin = violinChartDTO.series.find({ it.identifier == identifier })
                if (measurands){
                    String measurandName = measurands.first().getDatabaseRelevantName()
                    Double value = (Double) eventResultProjection.projectedProperties."$measurandName"
                    if (!violin) {
                        violin = new Violin(identifier: identifier, jobGroup: jobGroup.name, page: page);
                        violinChartDTO.series.add(violin);
                    }
                    violin.data.add(value)
                }

                if (performanceAspectTypes) {
                    String performanceAspectType = performanceAspectTypes.first();
                    Double value = (Double) eventResultProjection.projectedProperties."$performanceAspectType"
                    if (!violin) {
                        violin = new Violin(identifier: identifier, jobGroup: jobGroup.name, page: page);
                        violinChartDTO.series.add(violin);
                    }
                    violin.data.add(value)
                }
            }
        }
        return violinChartDTO
    }

}

