databaseChangeLog = {
    changeSet(author: "nkuhn", id: "1513696035000-1") {
        createIndex(indexName: "forEventResultDashboard", tableName: "event_result") {
            column(name: "job_group_id")
            column(name: "page_id")
            column(name: "job_result_date")
            column(name: "connectivity_profile_id")
            column(name: "fully_loaded_time_in_millisecs")
            column(name: "median_value")
        }
    }
}
