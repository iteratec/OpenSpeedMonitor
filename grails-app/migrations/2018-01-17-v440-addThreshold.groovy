databaseChangeLog = {

    changeSet(author: "owe (generated)", id: "1504617777917-1") {
        createTable(tableName: "threshold") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "thresholdPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "job_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "measured_event_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "measurand", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "lower_boundary", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "upper_boundary", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "owe (generated)", id: "1504617777917-2") {
        addForeignKeyConstraint(baseColumnNames: "measured_event_id", baseTableName: "threshold", constraintName: "FK_1d13t597gdxfeaca8cr5aj2ts", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "measured_event")
    }

    changeSet(author: "owe (generated)", id: "1504617777917-3") {
        addForeignKeyConstraint(baseColumnNames: "job_id", baseTableName: "threshold", constraintName: "FK_qa9xa7ccl6ggj3bwoe4apj7e7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job")
    }
}
