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

}
