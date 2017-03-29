databaseChangeLog = {

    changeSet(author: "mmi", id: "1490080349980-1") {
        addColumn(tableName: "graphite_server") {
            column(name: "report_protocol", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "nkuhn (generated)", id: "1490624232534-1") {
        createTable(tableName: "location_health_check") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "location_health_checkPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "location_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "number_of_agents", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "number_of_currently_pending_jobs", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "number_of_currently_running_jobs", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "number_of_errors_last_hour", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "number_of_event_results_last_hour", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "number_of_event_results_next_hour", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "number_of_job_results_last_hour", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "number_of_job_results_next_hour", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "number_of_pending_jobs_in_wpt", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "nkuhn (generated)", id: "1490624232534-2") {
        addColumn(tableName: "osm_configuration") {
            column(defaultValue: "30", defaultValueNumeric: "30", name: "internal_monitoring_storage_time_in_days", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

}
