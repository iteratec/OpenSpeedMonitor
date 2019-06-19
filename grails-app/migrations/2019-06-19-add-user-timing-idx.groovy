databaseChangeLog = {
    changeSet(author: "nkuhn (generated)", id: "1560938180861-3") {
        createIndex(indexName: "name_idx", tableName: "user_timing") {
            column(name: "name")
        }
    }
}
