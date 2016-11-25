import org.apache.commons.logging.LogFactory

databaseChangeLog = {
    // split eventResultTag
    changeSet(author: "mmi", id: "1472807664005-1") {
        grailsChange {
            change {
                int batchSize = 10000
                int count = sql.firstRow("SELECT COUNT(1) FROM event_result").getAt(0)
                def log = LogFactory.getLog("liquibase")

                def jobGroupIds = sql.rows("SELECT DISTINCT id FROM job_group").collect { it[0] as Integer }
                def measuredEventIds = sql.rows("SELECT DISTINCT id FROM measured_event").collect { it[0] as Integer }
                def pageIdS = sql.rows("SELECT DISTINCT id FROM page").collect { it[0] as Integer }
                def browserIds = sql.rows("SELECT DISTINCT id FROM browser").collect { it[0] as Integer }
                def locationIds = sql.rows("SELECT DISTINCT id FROM location").collect { it[0] as Integer }

                log.debug("split eventResultTag: Migrating event_result table (${count} rows, batchSize ${batchSize}")

                for (int offset = 0; offset < count; offset += batchSize) {
                    int limit = offset + batchSize

                    sql.eachRow("select id, tag from event_result LIMIT ${offset},${limit};") { csiAggregation ->
                        def tags = csiAggregation.tag.split(";").collect { it as Integer }
                        def jobGroupId = tags[0] in jobGroupIds ? tags[0] : null
                        def measuredEventId = tags[1] in measuredEventIds ? tags[1] : null
                        def pageId = tags[2] in pageIdS ? tags[2] : null
                        def browserId = tags[3] in browserIds ? tags[3] : null
                        def locationId = tags[4] in locationIds ? tags[4] : null

                        sql.executeUpdate("update event_result set job_group_id = :jobGroupId, measured_event_id = :measuredEventId, page_id = :pageId, browser_id = :browserId, location_id = :locationId where id = :currentId",
                                [currentId: csiAggregation.id, jobGroupId: jobGroupId, measuredEventId: measuredEventId, pageId: pageId, browserId: browserId, locationId: locationId])

                    }

                    float percent = ((float) limit) / count * 100
                    log.debug("Migrated ${percent} percent (${limit}/${count} rows)... ")
                }
                log.debug("split eventResultTag migration finished")
            }
        }
    }

    // split measuredEventTag
    changeSet(author: "mmi", id: "1472807664001-1") {
        grailsChange {
            change {
                int batchSize = 10000
                int count = sql.firstRow("SELECT COUNT(1) FROM  csi_aggregation where aggregator_id = (select id from aggregator_type where name = 'measuredEvent')").getAt(0)
                def log = LogFactory.getLog("liquibase")

                def jobGroupIds = sql.rows("SELECT DISTINCT id FROM job_group").collect { it[0] as Integer }
                def measuredEventIds = sql.rows("SELECT DISTINCT id FROM measured_event").collect { it[0] as Integer }
                def pageIdS = sql.rows("SELECT DISTINCT id FROM page").collect { it[0] as Integer }
                def browserIds = sql.rows("SELECT DISTINCT id FROM browser").collect { it[0] as Integer }
                def locationIds = sql.rows("SELECT DISTINCT id FROM location").collect { it[0] as Integer }

                log.debug("split measuredEventTag: Migrating csi_aggregation table (${count} rows, batchSize ${batchSize}")
                for (int offset = 0; offset < count; offset += batchSize) {
                    int limit = offset + batchSize

                    sql.eachRow("select id, tag from csi_aggregation where aggregator_id = (select id from aggregator_type where name = 'measuredEvent') LIMIT ${offset},${limit};") { csiAggregation ->
                        def tags = csiAggregation.tag.split(";").collect { it as Integer }
                        def jobGroupId = tags[0] in jobGroupIds ? tags[0] : null
                        def measuredEventId = tags[1] in measuredEventIds ? tags[1] : null
                        def pageId = tags[2] in pageIdS ? tags[2] : null
                        def browserId = tags[3] in browserIds ? tags[3] : null
                        def locationId = tags[4] in locationIds ? tags[4] : null

                        sql.executeUpdate("update csi_aggregation set job_group_id = :jobGroupId, measured_event_id = :measuredEventId, page_id = :pageId, browser_id = :browserId, location_id = :locationId where id = :currentId",
                                [currentId: csiAggregation.id, jobGroupId: jobGroupId, measuredEventId: measuredEventId, pageId: pageId, browserId: browserId, locationId: locationId])
                    }

                    float percent = ((float) limit) / count * 100
                    log.debug("Migrated ${percent} percent (${limit}/${count} rows)... ")
                }
                log.debug("split measuredEventTag migration finished")
            }
        }
    }

    // split pageTag
    changeSet(author: "mmi", id: "1472807664001-2") {
        grailsChange {
            change {
                int batchSize = 10000
                int count = sql.firstRow("SELECT COUNT(1) FROM  csi_aggregation where aggregator_id = (select id from aggregator_type where name = 'page')").getAt(0)
                def log = LogFactory.getLog("liquibase")

                def jobGroupIds = sql.rows("SELECT DISTINCT id FROM job_group").collect { it[0] as Integer }
                def pageIds = sql.rows("SELECT DISTINCT id FROM page").collect { it[0] as Integer }

                log.debug("split pageTag: Migrating csi_aggregation table (${count} rows, batchSize ${batchSize}")
                for (int offset = 0; offset < count; offset += batchSize) {
                    int limit = offset + batchSize

                    sql.eachRow("select id, tag from csi_aggregation where aggregator_id = (select id from aggregator_type where name = 'page') LIMIT ${offset},${limit};") { csiAggregation ->
                        String currentTag = csiAggregation.tag

                        def tags = csiAggregation.tag.split(";").collect { it as Integer }
                        def jobGroupId = tags[0] in jobGroupIds ? tags[0] : null
                        def pageId = tags[1] in pageIds ? tags[1] : null

                        sql.executeUpdate("update csi_aggregation set job_group_id = :jobGroupId, page_id = :pageId where id = :currentId",
                                [currentId: csiAggregation.id, jobGroupId: jobGroupId, pageId: pageId])

                    }

                    float percent = ((float) limit) / count * 100
                    log.debug("Migrated ${percent} percent (${limit}/${count} rows)... ")
                }
                log.debug("split pageTag migration finished")
            }
        }
    }

    // split shopTag
    changeSet(author: "mmi", id: "1472807664001-3") {
        grailsChange {
            change {
                sql.executeUpdate("update csi_aggregation set job_group_id = tag where aggregator_id = (select id from aggregator_type where name = 'shop') AND tag in (SELECT distinct id from job_group)")
            }
        }
    }


    changeSet(author: "mmi", id: "1473397000359-9") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "browser_id", tableName: "event_result")
    }

    changeSet(author: "mmi", id: "1473397000359-10") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "job_group_id", tableName: "event_result")
    }

    changeSet(author: "mmi", id: "1473397000359-11") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "location_id", tableName: "event_result")
    }

    changeSet(author: "mmi", id: "1473397000359-12") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "page_id", tableName: "event_result")
    }

    changeSet(author: "mmi", id: "1473397000359-13") {
        dropColumn(columnName: "tag", tableName: "event_result")
    }
    changeSet(author: "mmi", id: "1473397000359-14") {
        dropColumn(columnName: "tag", tableName: "csi_aggregation")
    }

    // Delete csiAggregationUpdateEvents which belong to already deleted csiAggregations.
    changeSet(author: "mmi", id: "1477505809000-1") {
        sql('''
            delete from csi_aggregation_update_event where csi_aggregation_id not in (
                select id from csi_aggregation);
        ''')
    }
}