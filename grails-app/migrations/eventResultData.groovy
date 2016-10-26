databaseChangeLog = {
    // section measuredEventTag
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
}