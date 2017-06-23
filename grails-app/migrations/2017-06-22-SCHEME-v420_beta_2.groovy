databaseChangeLog = {
    changeSet(author: "jweiss", id: "1498121471-1") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "aggregation_type", tableName: "csi_aggregation")
    }

    changeSet(author: "jweiss", id: "1498225787079-1") {
        dropForeignKeyConstraint(baseTableName: "csi_aggregation", constraintName: "FKCC54EB877896741")
    }

    changeSet(author: "jweiss", id: "1498225787079-2") {
        dropColumn(columnName: "aggregator_id", tableName: "csi_aggregation")
    }
}
