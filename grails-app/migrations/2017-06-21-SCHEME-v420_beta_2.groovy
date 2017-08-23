databaseChangeLog = {
    changeSet(author: "jweiss", id: "1498059027-1") {
        addColumn(tableName: "csi_aggregation") {
            column(name: "aggregation_type", type: "varchar(255)")
        }
    }
}
