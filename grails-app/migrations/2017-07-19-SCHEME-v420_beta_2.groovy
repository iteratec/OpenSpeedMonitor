databaseChangeLog = {
    changeSet(author: "mwg (generated)", id: "1500449693455-1") {
        createTable(tableName: "user_timing") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "user_timingPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "duration", type: "DOUBLE precision")

            column(name: "event_result_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "start_time", type: "DOUBLE precision") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mwg (generated)", id: "1500449693455-3") {
        addForeignKeyConstraint(baseColumnNames: "event_result_id", baseTableName: "user_timing", constraintName: "FK_p92fh99dsvko4silxmbpjxypn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "event_result")
    }
}