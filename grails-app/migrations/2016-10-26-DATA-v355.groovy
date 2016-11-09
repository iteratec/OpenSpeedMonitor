databaseChangeLog = {
    // split eventResultTag
    changeSet(author: "mmi", id: "1472807664005-1") {
        grailsChange {
            change {
                sql.eachRow("select id, tag from event_result;") { csiAggregation ->
                    String currentTag = csiAggregation.tag

                    def jobGroupId = currentTag.split(";")[0]
                    def measuredEventId = currentTag.split(";")[1]
                    def pageId = currentTag.split(";")[2]
                    def browserId = currentTag.split(";")[3]
                    def locationId = currentTag.split(";")[4]

                    sql.executeUpdate("update event_result set job_group_id = :jobGroupId, measured_event_id = :measuredEventId, page_id = :pageId, browser_id = :browserId, location_id = :locationId where id = :currentId",
                            [currentId: csiAggregation.id, jobGroupId: jobGroupId, measuredEventId: measuredEventId, pageId: pageId, browserId: browserId, locationId: locationId])

                }
            }
        }
    }

    // split measuredEventTag
    changeSet(author: "mmi", id: "1472807664001-1") {
        grailsChange {
            change {
                sql.eachRow("select id, tag from csi_aggregation where aggregator_id = (select id from aggregator_type where name = 'measuredEvent');") { csiAggregation ->
                    String currentTag = csiAggregation.tag

                    def jobGroupId = currentTag.split(";")[0]
                    def measuredEventId = currentTag.split(";")[1]
                    def pageId = currentTag.split(";")[2]
                    def browserId = currentTag.split(";")[3]
                    def locationId = currentTag.split(";")[4]

                    sql.executeUpdate("update csi_aggregation set job_group_id = :jobGroupId, measured_event_id = :measuredEventId, page_id = :pageId, browser_id = :browserId, location_id = :locationId where id = :currentId",
                            [currentId: csiAggregation.id, jobGroupId: jobGroupId, measuredEventId: measuredEventId, pageId: pageId, browserId: browserId, locationId: locationId])

                }
            }
        }
    }

    // split pageTag
    changeSet(author: "mmi", id: "1472807664001-2") {
        grailsChange {
            change {
                sql.eachRow("select id, tag from csi_aggregation where aggregator_id = (select id from aggregator_type where name = 'page');") { csiAggregation ->
                    String currentTag = csiAggregation.tag

                    def jobGroupId = currentTag.split(";")[0]
                    def pageId = currentTag.split(";")[1]

                    sql.executeUpdate("update csi_aggregation set job_group_id = :jobGroupId, page_id = :pageId where id = :currentId",
                            [currentId: csiAggregation.id, jobGroupId: jobGroupId, pageId: pageId])

                }
            }
        }
    }

    // split shopTag
    changeSet(author: "mmi", id: "1472807664001-3") {
        grailsChange {
            change {
                sql.eachRow("select id, tag from csi_aggregation where aggregator_id = (select id from aggregator_type where name = 'shop');") { csiAggregation ->
                    String currentTag = csiAggregation.tag

                    def jobGroupId = currentTag.split(";")[0]

                    sql.executeUpdate("update csi_aggregation set job_group_id = :jobGroupId where id = :currentId",
                            [currentId: csiAggregation.id, jobGroupId: jobGroupId])

                }
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