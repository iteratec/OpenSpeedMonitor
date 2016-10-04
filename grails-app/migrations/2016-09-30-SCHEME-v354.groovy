databaseChangeLog = {
    changeSet(author: "nkuhn (generated)", id: "1475229633466-1") {
        addColumn(tableName: "event_result") {
            column(name: "one_based_step_index_in_journey", type: "integer")
        }
    }
}