databaseChangeLog = {
//   full hour in HourOfDay is no longer unique
    changeSet(author: "marcus (generated)", id: "1452076094323-1") {
        dropIndex(indexName: "full_hour", tableName: "hour_of_day")
    }

// nightlyDatabaseCleanups can be de-/activated by REST-Call
    changeSet(author: "bka (generated)", id: "1452546683118-1") {
        addColumn(tableName: "api_key") {
            column(name: "allowed_for_nightly_database_cleanup_activation", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }
}