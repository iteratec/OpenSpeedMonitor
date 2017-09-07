databaseChangeLog = {
    changeSet(author: "mwg (generated)", id: "1504709901699-1") {
        createTable(tableName: "result_selection_information_user_timing_selection_information") {
            column(name: "result_selection_information_user_timings_id", type: "BIGINT")

            column(name: "user_timing_selection_information_id", type: "BIGINT")
        }
    }

    changeSet(author: "mwg (generated)", id: "1504709901699-2") {
        createTable(tableName: "user_timing_selection_information") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "user_timing_selection_informationPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mwg (generated)", id: "1504709901699-4") {
        addForeignKeyConstraint(baseColumnNames: "result_selection_information_user_timings_id", baseTableName: "result_selection_information_user_timing_selection_information", constraintName: "FK_8p7m36a6lgf6m7jh1g0r3xupk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "result_selection_information")
    }

    changeSet(author: "mwg (generated)", id: "1504709901699-5") {
        addForeignKeyConstraint(baseColumnNames: "user_timing_selection_information_id", baseTableName: "result_selection_information_user_timing_selection_information", constraintName: "FK_c9hd6loeulr7h9yfaqwtlyad0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user_timing_selection_information")
    }

    changeSet(author: "mwg (generated)", id: "1504709901699-6") {
        dropForeignKeyConstraint(baseTableName: "result_selection_information_user_timing_selection_infomation", constraintName: "FK_8t25fbvunclmaot246v8td5mf")
    }

    changeSet(author: "mwg (generated)", id: "1504709901699-7") {
        dropForeignKeyConstraint(baseTableName: "result_selection_information_user_timing_selection_infomation", constraintName: "FK_jqpmjuikh10mknek4j5tqrlrr")
    }

    changeSet(author: "mwg (generated)", id: "1504709901699-8") {
        dropTable(tableName: "result_selection_information_user_timing_selection_infomation")
    }

    changeSet(author: "mwg (generated)", id: "1504709901699-9") {
        dropTable(tableName: "user_timing_selection_infomation")
    }
}