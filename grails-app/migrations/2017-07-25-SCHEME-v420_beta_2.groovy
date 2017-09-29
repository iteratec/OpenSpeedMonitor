databaseChangeLog = {
    changeSet(author: "mwg (generated)", id: "1500994551030-1") {
        createTable(tableName: "result_selection_information_user_timing_selection_infomation") {
            column(name: "result_selection_information_user_timings_id", type: "BIGINT")

            column(name: "user_timing_selection_infomation_id", type: "BIGINT")
        }
    }

    changeSet(author: "mwg (generated)", id: "1500994551030-2") {
        createTable(tableName: "user_timing_selection_infomation") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "user_timing_selection_infomationPK")
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

    changeSet(author: "mwg (generated)", id: "1500994551030-4") {
        addForeignKeyConstraint(baseColumnNames: "user_timing_selection_infomation_id", baseTableName: "result_selection_information_user_timing_selection_infomation", constraintName: "FK_8t25fbvunclmaot246v8td5mf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user_timing_selection_infomation")
    }

    changeSet(author: "mwg (generated)", id: "1500994551030-5") {
        addForeignKeyConstraint(baseColumnNames: "result_selection_information_user_timings_id", baseTableName: "result_selection_information_user_timing_selection_infomation", constraintName: "FK_jqpmjuikh10mknek4j5tqrlrr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "result_selection_information")
    }
}