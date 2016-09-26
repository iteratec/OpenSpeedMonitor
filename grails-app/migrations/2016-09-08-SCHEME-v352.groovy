databaseChangeLog = {

    changeSet(author: "mmi (generated)", id: "1473338973494-1") {
        addColumn(tableName: "graphite_server") {
            column(name: "report_event_results_to_graphite_server", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mmi (generated)", id: "1473338973494-2") {
        addColumn(tableName: "graphite_server") {
            column(name: "report_csi_aggregations_to_graphite_server", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "marko (generated)", id: "1473774165719-2") {
        createTable(tableName: "registration_code") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "registration_codePK")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "token", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "username", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "marko (generated)", id: "1474536696588-1") {
        addColumn(tableName: "user") {
            column(name: "email", type: "varchar(255)")
        }
    }
}
