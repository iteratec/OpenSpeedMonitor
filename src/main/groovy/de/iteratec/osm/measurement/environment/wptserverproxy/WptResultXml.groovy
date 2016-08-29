package de.iteratec.osm.measurement.environment.wptserverproxy

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.CachedView;
import de.iteratec.osm.result.WptXmlResultVersion
import groovy.util.slurpersupport.GPathResult

/**
 * Represents webpagetests response from xmlResult.php api function.
 * @author nkuhn
 * @see https://sites.google.com/a/webpagetest.org/docs/advanced-features/webpagetest-restful-apis#TOC-XML-response
 */
class WptResultXml {
    /**
     * Base node of results xml.
     */
    GPathResult responseNode
    /**
     * Version of webpagetests result xml.
     * @see {@link WptXmlResultVersion}
     */
    WptXmlResultVersion version

    public WptResultXml(GPathResult baseNode) {
        this.responseNode = baseNode
        version = WptXmlResultVersion.BEFORE_MULTISTEP
        if (!this.responseNode.data.median.firstView.testStep.isEmpty()) {
            version = WptXmlResultVersion.MULTISTEP_1
        }
        if (!this.responseNode.webPagetestVersion.isEmpty()) {
            version = WptXmlResultVersion.VERSION_2_19
        }
    }

    String getLabel() {
        return responseNode.data.label.toString()
    }

    String getLocation() {
        return responseNode.data.location.toString()
    }

    Integer getRunCount() {
        if (!responseNode.data.runs.toString().isInteger())
            throw new IllegalArgumentException('data/runs missing or no integer in XML result')
        return responseNode.data.runs.toInteger()
    }

    String getTestId() {
        return responseNode.data.testId.toString()
    }

    String getTestAgent() {
        return responseNode.data.tester.toString()
    }

    Integer getStatusCodeOfWholeTest() {
        return responseNode.statusCode.toInteger()
    }

    Date getCompletionDate() {
        return responseNode.data.completed.isEmpty() ? new Date() : new Date(responseNode.data.completed.toString())
    }

    Integer getBwDown() {
        return responseNode.data.bwDown.toInteger()
    }

    Integer getBwUp() {
        return responseNode.data.bwUp.toInteger()
    }

    Integer getLatency() {
        return responseNode.data.latency.toInteger()
    }

    Integer getPacketLossRate() {
        return responseNode.data.plr.toInteger()
    }

    /**
     * Provides count of steps in this test. For results of wptservers before multistep 1 is returned for successful
     * results and 0 is returned for failed results.
     * @return Number of steps in this test.
     */
    Integer getTestStepCount() {

        if (version == null) throw new IllegalStateException("Version of result xml isn't specified!")

        switch (version) {
            case WptXmlResultVersion.BEFORE_MULTISTEP:
                return responseNode.data.median.isEmpty() ? 0 : 1
                break
            case WptXmlResultVersion.MULTISTEP_1:
                return responseNode.data.median.firstView.testStep.size()
            case WptXmlResultVersion.VERSION_2_19:
                return responseNode.data.run.getAt(0).firstView.numSteps.toString() as Integer
        }
    }

    String getEventName(Job job, Integer testStepZeroBasedIndex) {

        if (version == null) throw new IllegalStateException("Version of result xml isn't specified!")

        String measuredEventName
        if (version == WptXmlResultVersion.BEFORE_MULTISTEP) {
            measuredEventName = job.getEventNameIfUnknown();
            if (!measuredEventName) measuredEventName = job.getLabel()
        } else if (version == WptXmlResultVersion.MULTISTEP_1) {
            measuredEventName = responseNode.data.median.firstView.testStep.getAt(testStepZeroBasedIndex).eventName.toString();
        } else {
            measuredEventName = responseNode.data.run.getAt(0).firstView.step.getAt(testStepZeroBasedIndex).eventName.toString();
        }
        return measuredEventName
    }

    def getRunNodes() {
        return responseNode.data.run
    }

    GPathResult getResultNodeForRunAndView(runZeroBasedIndex, cachedView) {
        GPathResult runNode = getRunNodes()[runZeroBasedIndex]
        if (cachedView == CachedView.UNCACHED) {
            return runNode.firstView
        } else if (cachedView == CachedView.CACHED) {
            return runNode.repeatView
        } else {
            throw new IllegalArgumentException("Argument cachedView should be UNCACHED or CACHED, but was ${cachedView}!")
        }
    }

    Boolean resultExistForRunAndView(runZeroBasedIndex, cachedView) {
        return !getResultNodeForRunAndView(runZeroBasedIndex, cachedView).isEmpty()
    }
    /**
     * Provides the xml node which contains the measured values of this result (like requestsDoc, docTime or score_cdn) for
     * given runZeroBasedIndex, cachedView and testStepZeroBasedIndex.
     * @param runZeroBasedIndex
     * 	Zero based index of the run.
     * @param cachedView
     * 	CachedView.CACHED or CachedView.UNCACHED.
     * @param testStepZeroBasedIndex
     * 	Zero based index of the teststep. For results of wptservers before multistep param testStepZeroBasedIndex isn't involved at all.
     * @return Xml node which contains the measured values of this result.
     */
    GPathResult getResultsContainingNode(runZeroBasedIndex, cachedView, testStepZeroBasedIndex) {

        if (version == null) throw new IllegalStateException("Version of result xml isn't specified!")

        GPathResult viewNode = getResultNodeForRunAndView(runZeroBasedIndex, cachedView)
        if (version == WptXmlResultVersion.BEFORE_MULTISTEP) {
            return viewNode.results
        } else if (version == WptXmlResultVersion.MULTISTEP_1) {
            return viewNode.results.testStep.getAt(testStepZeroBasedIndex)
        } else {
            return viewNode.step.getAt(testStepZeroBasedIndex).results
        }
    }

    /**
     * Determines if a node is a median node
     *
     * @param viewNode expects a view node of a run: (response -> data -> run -> firstView or repeatedView)
     * @param currentRun
     * @return
     */
    private boolean isMedian(Integer runZeroBasedIndex, CachedView cachedView, Integer testStepZeroBasedIndex) {

        if (version == null) throw new IllegalStateException("Version of result xml isn't specified!")

        Integer medianRunNumber = getRunNumberOfMedianViewNode(cachedView, testStepZeroBasedIndex)
        if (runZeroBasedIndex + 1 == medianRunNumber) {
            return true
        } else {
            return false
        }

    }

    Integer getRunNumberOfMedianViewNode(CachedView cachedView, Integer testStepZeroBasedIndex) {
        boolean firstView = cachedView == CachedView.UNCACHED

        switch (version) {
            case WptXmlResultVersion.BEFORE_MULTISTEP:
                return firstView ? responseNode.data.median.firstView.run.getAt(0).toInteger() : responseNode.data.median.repeatView.run.getAt(0).toInteger()
            case WptXmlResultVersion.MULTISTEP_1:
                return firstView ? responseNode.data.median.firstView.testStep.getAt(testStepZeroBasedIndex).run.getAt(0).toInteger() : responseNode.data.median.repeatView.testStep.getAt(testStepZeroBasedIndex).run.getAt(0).toInteger()
            case WptXmlResultVersion.VERSION_2_19:
                return firstView ? responseNode.data.median.firstView.run.getAt(0).toInteger() : responseNode.data.median.repeatView.run.getAt(0).toInteger()
            default:
                throw new IllegalArgumentException("Unknown WptXmlResultVersion: ${version}")
        }

    }

}
