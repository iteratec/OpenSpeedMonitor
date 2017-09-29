databaseChangeLog = {

    changeSet(author: "mwg (generated)", id: "1501155218685-1") {
        createTable(tableName: "event_result_user_timing") {
            column(name: "event_result_user_timings_id", type: "BIGINT")

            column(name: "user_timing_id", type: "BIGINT")
        }
    }

    changeSet(author: "mwg (generated)", id: "1501155218685-2") {
        addForeignKeyConstraint(baseColumnNames: "user_timing_id", baseTableName: "event_result_user_timing", constraintName: "FK_1smt9m208np9jc9fmh8ajr5o8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user_timing")
    }

    changeSet(author: "mwg (generated)", id: "1501155218685-3") {
        addForeignKeyConstraint(baseColumnNames: "event_result_user_timings_id", baseTableName: "event_result_user_timing", constraintName: "FK_r687wbyvg5g8r74j95n2a5f1a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "event_result")
    }

    changeSet(author: "mwg", id: "1501155218685-4") {
       sql('''
            INSERT INTO event_result_user_timing ( user_timing_id, event_result_user_timings_id)
            SELECT id, event_result_id
            FROM user_timing;
        ''')
    }

    changeSet(author: "mwg (generated)", id: "1501155218685-5") {
        dropForeignKeyConstraint(baseTableName: "user_timing", constraintName: "FK_p92fh99dsvko4silxmbpjxypn")
    }

    changeSet(author: "mwg (generated)", id: "1501155218685-6") {
        dropColumn(columnName: "event_result_id", tableName: "user_timing")
    }
}