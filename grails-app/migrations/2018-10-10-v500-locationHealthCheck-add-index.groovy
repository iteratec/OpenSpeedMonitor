databaseChangeLog = {

    changeSet(author: "fwieczorek", id: "20181010-label-1") {

        createIndex(indexName: "date_and_location_id", tableName: "location_health_check") {
            column(name: "location_id")
            column(name: "date")
        }
    }
}
