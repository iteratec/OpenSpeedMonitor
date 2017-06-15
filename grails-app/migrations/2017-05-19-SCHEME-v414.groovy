databaseChangeLog = {
    changeSet(author: "dkl", id: "1495182423000-2") {
        dropColumn(columnName: "bodies", tableName: "job")
    }
}
