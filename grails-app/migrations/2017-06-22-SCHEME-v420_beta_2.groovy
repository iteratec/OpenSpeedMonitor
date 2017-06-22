databaseChangeLog = {
    changeSet(author: "jweiss", id: "1498121471-1") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "aggregation_type", tableName: "csi_aggregation")
    }
}
