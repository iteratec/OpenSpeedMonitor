databaseChangeLog = {
    changeSet(author: "nkuhn (generated)", id: "1475229633466-1") {
        addColumn(tableName: "event_result") {
            column(name: "one_based_step_index_in_journey", type: "integer")
        }
    }
    changeSet(author: "nkuhn (generated)", id: "1475704059884-1") {
        addColumn(tableName: "web_page_test_server") {
            column(name: "api_key", type: "varchar(255)")
        }
    }
}