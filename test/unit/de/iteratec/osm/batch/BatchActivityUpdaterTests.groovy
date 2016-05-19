package de.iteratec.osm.batch

import grails.test.mixin.Mock
import spock.lang.Specification

/**
 * Since the BatchActivityUpdater uses another thread, all of this tests may have to wait until the other thread has finished something.
 * This will lead to various sleep() calls within this tests
 */
@Mock([BatchActivity])
class BatchActivityUpdaterTests extends Specification{


    def "test creation"(){
        when:"We create a fresh updater"
        String domain = "Domain"
        String name = "TestUpdater"
        int maxStages = 10
        BatchActivityUpdater updater = new BatchActivityUpdater(name, domain, Activity.CREATE,maxStages,10)
        sleep(500)//Make sure the other thread has enough time to save the object

        then:"An equivalent BatchActivity should be persisted"
        updater.batchActivity != null

        cleanup:
        updater.cleanup()
    }

    def "test updates shouldn't effect the actual domain without an update call"(){
        given: "A new Updater"
        String domain = "Domain"
        String name = "TestUpdater"
        int maxStages = 10
        String stageDescription = "New Stage"
        int maximumStepsInStage = 13
        BatchActivityUpdater updater = new BatchActivityUpdater(name, domain, Activity.CREATE,maxStages,10)
        sleep(1000) // Make sure other thread has saved the actual domain

        when:"We change some values on the updater, but didn't call update"
        updater.beginNewStage(stageDescription, maximumStepsInStage).addProgressToStage().addFailures()

        then:"An equivalent BatchActivity should be persisted"
        BatchActivity batchActivity = BatchActivity.findByNameAndDomain(name,domain)
        batchActivity.stepInStage == 0
        batchActivity.maximumStepsInStage == 0
        batchActivity.failures == 0
        batchActivity.stageDescription != stageDescription

        cleanup:
        updater.cleanup()
    }

    def "test updates should effect the actual domain with an update call"(){
        given: "A new Updater"
        String domain = "Domain"
        String name = "TestUpdater"
        int maxStages = 10
        String stageDescription = "New Stage"
        int updateInterval = 10
        int maximumStepsInStage = 13
        BatchActivityUpdater updater = new BatchActivityUpdater(name, domain, Activity.CREATE,maxStages,updateInterval)

        when:"We change some values on the updater and call update"
        updater.beginNewStage(stageDescription, maximumStepsInStage).addProgressToStage().addFailures().update()
        sleep(500) // Make sure the other thread got time to save the object

        then:"An equivalent BatchActivity should be persisted"
        BatchActivity batchActivity = BatchActivity.findByNameAndDomain(name,domain)
        batchActivity.stepInStage == 1
        batchActivity.failures == 1
        batchActivity.maximumStepsInStage == maximumStepsInStage
        batchActivity.stageDescription == stageDescription
    }

    def "test beginning a new stage should reset the step in stage counter"(){
        given: "A new Updater"
        String domain = "Domain"
        String name = "TestUpdater"
        int maxStages = 10
        String stageDescription = "Stage"
        String updatedStageDescription = "Second Stage"
        int updateInterval = 10
        int maximumStepsInStage = 13
        BatchActivityUpdater updater = new BatchActivityUpdater(name, domain, Activity.CREATE,maxStages,updateInterval)

        when:"We change some values on the updater and begin a new stage"
        updater.beginNewStage(stageDescription, maximumStepsInStage).addProgressToStage(10).update()
        sleep(updateInterval)
        updater.beginNewStage(updatedStageDescription, maximumStepsInStage).update()
        sleep(1000)

        then:"An equivalent BatchActivity should be persisted"
        BatchActivity batchActivity = BatchActivity.findByNameAndDomain(name,domain)
        batchActivity.stepInStage == 0
        batchActivity.stageDescription != stageDescription
        batchActivity.stageDescription == updatedStageDescription

        cleanup:
        updater.done()
    }

    def "finishing an activity"(){
        given: "A new Updater"
        String name = "Name"
        String domain = "Domain"
        BatchActivityUpdater updater = new BatchActivityUpdater(name, domain, Activity.CREATE,1,10)
        Date startDate = new Date()
        when:"We call done "
        sleep(1000) // make sure the next update happens with another timestamp than the activity was started
        updater.beginNewStage("stage", 10).addProgressToStage(10).done()
        sleep(1000) // make sure the timer run again

        then:"The status of the BatchActivity should be done and their should be an end date"
        BatchActivity batchActivity = updater.batchActivity
        batchActivity.status == Status.DONE
        batchActivity.endDate != null
        batchActivity.endDate > startDate
    }

    def "test complete run"(){
        given: "A new Updater"
        String name = "Name"
        String domain = "Domain"
        BatchActivityUpdater updater = new BatchActivityUpdater(name, domain, Activity.CREATE,3,600)

        when:"We call done"
        3.times {stageCount ->
            updater.beginNewStage("stage", 3).update()
            updater.addProgressToStage(3).update()
        }
        updater.done()
        sleep(2000) // make sure the timer ran again

        then:"All attributes should be at the defined maximum"
        BatchActivity batchActivity = BatchActivity.findByNameAndDomain(name,domain)
        batchActivity.status == Status.DONE
        batchActivity.maximumStages == 3
        batchActivity.actualStage == 3
        batchActivity.stepInStage == 3
        batchActivity.endDate != null
    }

    def "test timeout"(){
        given: "A new Updater"
        String name = "Name"
        String domain = "Domain"
        BatchActivityUpdater updater = new BatchActivityUpdater(name, domain, Activity.CREATE,1,100, 1)

        when:"We make an update but then exceed the timeout"
        sleep(4000)

        then:"The Updater should be cancelled and there should be no active timer"
        BatchActivity batchActivity = BatchActivity.findByNameAndDomainAndActivity(name, domain, Activity.CREATE)
        batchActivity.status == Status.CANCELLED

    }

}
