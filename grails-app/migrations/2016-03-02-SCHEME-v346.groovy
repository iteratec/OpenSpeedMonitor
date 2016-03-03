databaseChangeLog = {

    changeSet(author: "marcus (generated)", id: "1456924811919-1") {
        createTable(tableName: "userspecific_csi_dashboard_graph_name_aliases") {
            column(name: "graph_name_aliases", type: "bigint")

            column(name: "graph_name_aliases_idx", type: "varchar(255)")

            column(name: "graph_name_aliases_elt", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1456924811919-2") {
        createTable(tableName: "userspecific_event_result_dashboard_graph_name_aliases") {
            column(name: "graph_name_aliases", type: "bigint")

            column(name: "graph_name_aliases_idx", type: "varchar(255)")

            column(name: "graph_name_aliases_elt", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1456935458451-1") {
        createTable(tableName: "userspecific_csi_dashboard_graph_colors") {
            column(name: "graph_colors", type: "bigint")

            column(name: "graph_colors_idx", type: "varchar(255)")

            column(name: "graph_colors_elt", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1456935458451-2") {
        createTable(tableName: "userspecific_event_result_dashboard_graph_colors") {
            column(name: "graph_colors", type: "bigint")

            column(name: "graph_colors_idx", type: "varchar(255)")

            column(name: "graph_colors_elt", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "nkuhn (generated)", id: "1457032106256-1") {
        dropColumn(columnName: "group_type", tableName: "job_group")
    }
}
