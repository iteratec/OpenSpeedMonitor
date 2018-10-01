databaseChangeLog = {
    changeSet(author: "fho", id: "20181001-label-1") {
        dropNotNullConstraint(columnDataType: "varchar(255)", tableName: "job", columnName: "label")
    }
}