databaseChangeLog = {
    changeSet(author: "jweiss", id: "1499269597-1") {
        renameColumn(tableName: "osm_configuration", oldColumnName: "min_doc_complete_time_in_millisecs", newColumnName: "min_valid_loadtime", columnDataType: "int")
    }
    changeSet(author: "jweiss", id: "1499269597-2") {
        renameColumn(tableName: "osm_configuration", oldColumnName: "max_doc_complete_time_in_millisecs", newColumnName: "max_valid_loadtime", columnDataType: "int")
    }
}
