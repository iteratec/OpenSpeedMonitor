databaseChangeLog = {
    changeSet(author: "jwi", id: "1569252298504-1") {
        addColumn(tableName: "event_result") {
            column(name: "first_paint", type: "integer")
        }
    }
}
