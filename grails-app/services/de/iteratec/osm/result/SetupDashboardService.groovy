package de.iteratec.osm.result

import de.iteratec.osm.measurement.schedule.Job
import grails.transaction.Transactional
import org.hibernate.criterion.CriteriaSpecification

@Transactional
class SetupDashboardService {

    def getPagesForActiveJobGroups() {
        def scriptsWithJobGroup = Job.createCriteria().list {
            eq('active', true)
            isNotNull('script')
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            createAlias('script', 'script')
            createAlias('jobGroup', 'jobGroup')
            projections {
                distinct('jobGroup.id', 'script.id')
                property('jobGroup.id', 'jobGroupId')
                property('jobGroup.name', 'jobGroupName')
                property('script', 'script')
            }
        }

        def jobGroupsWithPages = scriptsWithJobGroup.collect{ scriptWithJobGroup ->
            [
                    'id': scriptWithJobGroup.jobGroupId,
                    'name': scriptWithJobGroup.jobGroupName,
                    'pages': []
            ]
        }.unique()

        scriptsWithJobGroup.each { scriptWithJobGroup ->
            def jobGroupWithPages = jobGroupsWithPages.find { wantedJobGroupWithPages -> wantedJobGroupWithPages.id == scriptWithJobGroup.jobGroupId}
            jobGroupWithPages.pages.add(scriptWithJobGroup.script.testedPages)
            jobGroupWithPages.pages = jobGroupWithPages.pages.flatten()
            jobGroupWithPages.pages = jobGroupWithPages.pages.unique()
        }

        return jobGroupsWithPages
    }
}
