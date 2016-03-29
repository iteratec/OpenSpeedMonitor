package de.iteratec.osm.persistence

import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.result.detail.Asset
import de.iteratec.osm.result.detail.AssetGroup
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Test
import spock.lang.Specification

@TestFor(DbCleanupService)
@Mock([AssetGroup, Asset, BatchActivity])
class HarCleanupTests extends Specification{

    public void setup(){
        service.batchActivityService = new BatchActivityService()
        service.batchActivityService.metaClass.runningBatch = {Class c, long l -> false}
        service.batchActivityService.metaClass.getActiveBatchActivity = {Class c, long idWithinDomain, Activity activity, String name, boolean observe -> new BatchActivity()}
    }

    @Test
    public void testDelete(){
        given: "2 assets groups which are too old and 3 which are still in our time frame"
            AssetGroup toDelete1 = TestDataUtil.createAssetGroup(new Date(1459254288000)).save(failOnError: true)
            AssetGroup toDelete2 = TestDataUtil.createAssetGroup(new Date(1459254288000)).save(failOnError: true)
            AssetGroup notToDelete1 =  TestDataUtil.createAssetGroup(new Date(1459254289000)).save(failOnError: true)
            AssetGroup notToDelete2 =  TestDataUtil.createAssetGroup(new Date(1459254289000)).save(failOnError: true)
            AssetGroup notToDelete3 =  TestDataUtil.createAssetGroup(new Date(1459254289000)).save(failOnError: true)
            List<Long> remainingId = [notToDelete1.id,notToDelete2.id,notToDelete3.id]
            List<Long> deletedIds = [toDelete1.id,toDelete2.id]
            Date maximumDate = new Date(1459254289000)
        when: "We trigger the delete"
            service.deleteHarDataBefore(maximumDate, false)
        then: "There should be only the 3 AssetGroups remaining"
            AssetGroup.count() == 3
            remainingId.each {AssetGroup.get(it) != null}
            deletedIds.each {AssetGroup.get(it) == null}
    }

}
