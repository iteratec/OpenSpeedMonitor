databaseChangeLog = {
    changeSet(author: "fho", id: "20181001-label-1") {
        dropNotNullConstraint(columnDataType: "varchar(255)", tableName: "job", columnName: "label")
    }
    changeSet(author: "fho", id: "20181008-label-1") {
        addColumn(tableName: "job") {
            column(name: "label_temp", type: "varchar(255)")
        }
        sql("UPDATE job SET label_temp = label")
        dropColumn(tableName: "job", columnName: "label")
        addColumn(tableName: "job") {
            column(name: "label", type: "varchar(255)")
        }
        sql("UPDATE job SET label = label_temp")
        dropColumn(tableName: "job", columnName: "label_temp")
    }
}