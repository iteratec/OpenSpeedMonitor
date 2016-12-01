databaseChangeLog = {
    changeSet(author: "sburnicki", id: "1477485207405-1") {
        dropColumn(columnName: "initial_chart_width_in_pixels", tableName: "osm_configuration")
    }
    changeSet(author: "marko (generated)", id: "1478707539184-2") {
        dropColumn(columnName: "weight", tableName: "browser")
    }
    changeSet(author: "marko (generated)", id: "1478711739662-2") {
        dropColumn(columnName: "weight", tableName: "page")
    }

    // ### BEGIN Refactoring of tag attribute ###
    changeSet(author: "marcus (generated)", id: "1473397000359-1") {
        addColumn(tableName: "event_result") {
            column(name: "browser_id", type: "bigint")
            column(name: "job_group_id", type: "bigint")
            column(name: "location_id", type: "bigint")
            column(name: "page_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1473079647061-1") {
        addColumn(tableName: "csi_aggregation") {
            column(name: "browser_id", type: "bigint")
            column(name: "job_group_id", type: "bigint")
            column(name: "location_id", type: "bigint")
            column(name: "measured_event_id", type: "bigint")
            column(name: "page_id", type: "bigint")
        }
    }

    // ### END Refactoring of tag attribute ###
    changeSet(author: "sburnicki (generated)", id: "1480607666994-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'result_selection_information')
            }
        }
        createTable(tableName: "result_selection_information") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "result_selection_informationPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "browser_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "connectivity_profile_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "custom_connectivity_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "job_group_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "job_result_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "location_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "measured_event_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "no_traffic_shaping_at_all", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "page_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }
}
