databaseChangeLog = {
//   full hour in HourOfDay is no longer unique
    changeSet(author: "marcus (generated)", id: "1452076094323-1") {
        dropIndex(indexName: "full_hour", tableName: "hour_of_day")
    }
}