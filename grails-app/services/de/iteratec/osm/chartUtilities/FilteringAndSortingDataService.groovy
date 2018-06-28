package de.iteratec.osm.chartUtilities

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobDaoService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.PlaceholdersUtility
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.script.ScriptParser
import de.iteratec.osm.result.PageService
import grails.gorm.transactions.Transactional

@Transactional
class FilteringAndSortingDataService {

    JobDaoService jobDaoService
    PageService pageService

    /**
     * Creates Filter Rules for given Pages and JobGroups.
     * The filterRules can filter the data by testedPages in {@link de.iteratec.osm.measurement.script.Script}
     * @return a map which maps the {@link de.iteratec.osm.measurement.script.Script}-name to a list of page-jobGroup-name-combinations (e.g. ["script1" : ["page1 / jobGroup1"]])
     */
    public Map<String, List<String>> createFilterRules(List<Page> pages, List<JobGroup> jobGroups) {
        Map<String, List<String>> result = [:].withDefault { [] }

        // Get all scripts
        List<Job> jobList = jobDaoService.getJobs(jobGroups)
        List<Script> allScripts = jobList*.script.unique()

        allScripts.each { currentScript ->
            List<String> filterRule = []
            List<Page> testedPages = []
            List<List<Page>> testedPagesPerJob = [].withDefault { [] }

            jobList.findAll { it.script == currentScript }.each { j ->
                testedPagesPerJob << new ScriptParser(pageService, PlaceholdersUtility.getParsedNavigationScript(currentScript.navigationScript, j.variables)).getTestedPages().unique()
            }

            // if all lists are equal take any
            // else merge lists
            if (testedPagesPerJob.every { it.equals(testedPagesPerJob[0]) }) {
                testedPages = testedPagesPerJob[0]
            } else {
                testedPages = mergeLists(testedPagesPerJob)
            }

            testedPages.each { p ->
                if (pages.contains(p)) {
                    jobGroups.each {
                        filterRule << "${p.name} / ${it.name}"
                    }
                }
            }

            if (filterRule)
                result.put(currentScript.label, filterRule)
        }

        return result
    }

    /**
     * Merges multiple lists of pages by collecting all pages of each list and keeping the order
     * E.g.:
     * List1 = ["a", "b",       "c"]
     * List2 = ["a", "c",       "c"]
     * List2 = ["a", "b",       "d"]
     * Result = ["a", "b", "c", "c", "d"]
     * @param listOfPages
     * @return
     */
    private List<Page> mergeLists(List<List<Page>> listOfPages) {
        List<Page> result = []

        for (int i = 0; i < listOfPages*.size().max(); i++) {
            List<Page> l = listOfPages*.getAt(i).unique()
            l.removeAll([null])
            result.addAll(l)
        }

        return result
    }
}
