databaseChangeLog = {
    changeSet(author: "sburnicki", id: "1538037412000-1") {
        dropTable(tableName: "customer_frustration")
        dropColumn(columnName: "csi_transformation", tableName: "osm_configuration")
    }
}
