databaseChangeLog = {
    changeSet(author: "mwg (generated)", id: "1499681004343-3") {
        dropForeignKeyConstraint(baseTableName: "graphite_path", constraintName: "FK2FE067F885F60EF5")
    }

    changeSet(author: "mwg (generated)", id: "1499681004343-4") {
        dropForeignKeyConstraint(baseTableName: "graphite_server_graphite_path", constraintName: "FKBDE6F5CF425FDD59")
    }

    changeSet(author: "mwg (generated)", id: "1499681004343-5") {
        dropForeignKeyConstraint(baseTableName: "graphite_server_graphite_path", constraintName: "FKBDE6F5CFE64D9BEB")
    }

    changeSet(author: "mwg (generated)", id: "1499681004343-6") {
        dropTable(tableName: "graphite_path")
    }

    changeSet(author: "mwg (generated)", id: "1499681004343-7") {
        dropTable(tableName: "graphite_server_graphite_path")
    }
}