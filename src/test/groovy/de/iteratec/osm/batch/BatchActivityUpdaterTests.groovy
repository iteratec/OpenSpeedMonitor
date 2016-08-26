package de.iteratec.osm.batch

import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([BatchActivity])
class BatchActivityUpdaterTests extends Specification{


    def "test creation"(){
        when:"We create a fresh updater"
        String domain = "Domain"
        String name = "TestUpdater"
        int maxStages = 10
        BatchActivityUpdater updater = new BatchActivityUpdater(name, domain, Activity.CREATE,maxStages,10)

        then:"An equivalent BatchActivity should be persisted"
        updater.batchActivity != null
        BatchActivity.findByNameAndDomain(name,domain) != null

    }

    def "test threshold counter update on addProgress"(){
        given: "A new Updater"
        String domain = "Domain"
        String name = "TestUpdater"
        int maxStages = 10
        String stageDescription = "New Stage"
        int maximumStepsInStage = 13
        BatchActivityUpdater updater = new BatchActivityUpdater(name, domain, Activity.CREATE,maxStages,10)

        when:

        updater.beginNewStage(stageDescription,maximumStepsInStage)
        updater.addProgressToStage(1)

        then:
        updater.count == 1
    }

    def "test threshold counter update on addFailure"(){
        given: "A new Updater"
        String domain = "Domain"
        String name = "TestUpdater"
        int maxStages = 10
        String stageDescription = "New Stage"
        int maximumStepsInStage = 13
        BatchActivityUpdater updater = new BatchActivityUpdater(name, domain, Activity.CREATE,maxStages,10)

        when:
        updater.beginNewStage(stageDescription,maximumStepsInStage)
        updater.addFailures("That was unexpected!")

        then:
        updater.count == 1
    }

    def "test beginNewStage should reset threshold and save direct"(){
        given: "A new Updater"
        String domain = "Domain"
        String name = "TestUpdater"
        int maxStages = 10
        String stageDescription = "New Stage"
        String newStageDescription = "Second Stage"
        int maximumStepsInStage = 13
        BatchActivityUpdater updater = new BatchActivityUpdater(name, domain, Activity.CREATE,maxStages,10)

        when:
        updater.beginNewStage(stageDescription,maximumStepsInStage)
        updater.addProgressToStage()
        updater.addProgressToStage()
        updater.addProgressToStage()
        updater.beginNewStage(newStageDescription,maximumStepsInStage)

        then:
        updater.count == 0
        BatchActivity batchActivity = BatchActivity.findByNameAndDomain(name,domain)
        batchActivity != null
        batchActivity.stageDescription == newStageDescription
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

        then:"All attributes should be at the defined maximum"
        BatchActivity batchActivity = BatchActivity.findByNameAndDomain(name,domain)
        batchActivity.status == Status.DONE
        batchActivity.maximumStages == 3
        batchActivity.actualStage == 3
        batchActivity.stepInStage == 3
        batchActivity.endDate != null
    }
}
