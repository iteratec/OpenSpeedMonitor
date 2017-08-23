databaseChangeLog = {

    changeSet(author: "mwg (generated)", id: "1499681004343-7") {
        dropTable(tableName: "graphite_server_graphite_path")
    }

    changeSet(author: "mwg (generated)", id: "1499681004343-6") {
        dropTable(tableName: "graphite_path")
    }

    changeSet(author: "mwg (generated)", id: "1499690731693-3") {
        dropTable(tableName: "aggregator_type")
    }
}