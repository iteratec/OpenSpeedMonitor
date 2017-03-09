databaseChangeLog = {
    changeSet(author: "marcus (generated)", id: "1487856496625-1") {
        addColumn(tableName: "batch_activity") {
            column(name: "comment", type: "varchar(255)")
        }
    }
}
