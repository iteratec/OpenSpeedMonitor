databaseChangeLog = {
    changeSet(author: "fho", id: "20181130-1") {
        dropColumn(columnName: "test_details_waterfallurl", tableName: "event_result")
    }
}