databaseChangeLog = {
    changeSet(author: "jweiss", id: "1498121471-1") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "aggregation_type", tableName: "csi_aggregation")
    }
    changeSet(author: "jweiss", id: "1498121471-2") {
        dropNotNullConstraint(columnDataType: "int", columnName: "aggregator_id", tableName: "csi_aggregation")
    }
}
