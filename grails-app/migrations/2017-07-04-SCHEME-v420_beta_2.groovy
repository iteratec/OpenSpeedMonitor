databaseChangeLog = {

    changeSet(author: "mwg (generated)", id: "1499160024483-1") {
        createTable(tableName: "graphite_path_csi_data") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "graphite_path_csi_dataPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "aggregation_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "graphite_server_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "prefix", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mwg (generated)", id: "1499160024483-2") {
        createTable(tableName: "graphite_path_raw_data") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "graphite_path_raw_dataPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cached_view", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "graphite_server_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "measurand", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "prefix", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mwg (generated)", id: "1499160024483-5") {
        addForeignKeyConstraint(baseColumnNames: "graphite_server_id", baseTableName: "graphite_path_raw_data", constraintName: "FK_fkuu07082lf6j649w0o9d48dq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_server")
    }

    changeSet(author: "mwg (generated)", id: "1499160024483-6") {
        addForeignKeyConstraint(baseColumnNames: "graphite_server_id", baseTableName: "graphite_path_csi_data", constraintName: "FK_rq4upfotoftedbqjva8pn276d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_server")
    }
}