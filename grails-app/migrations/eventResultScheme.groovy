databaseChangeLog = {

    changeSet(author: "marcus (generated)", id: "1473397000359-1") {
        addColumn(tableName: "event_result") {
            column(name: "browser_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1473397000359-2") {
        addColumn(tableName: "event_result") {
            column(name: "job_group_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1473397000359-3") {
        addColumn(tableName: "event_result") {
            column(name: "location_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1473397000359-4") {
        addColumn(tableName: "event_result") {
            column(name: "page_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1473397000359-5") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "event_result", constraintName: "FK_1lqpe6ckkhtlm17wdcqpp0bf4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "location")
    }

    changeSet(author: "marcus (generated)", id: "1473397000359-6") {
        addForeignKeyConstraint(baseColumnNames: "browser_id", baseTableName: "event_result", constraintName: "FK_3a0bt55f0gmtigcetbyg2bovk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "browser")
    }

    changeSet(author: "marcus (generated)", id: "1473397000359-7") {
        addForeignKeyConstraint(baseColumnNames: "page_id", baseTableName: "event_result", constraintName: "FK_5sk7tn8qu8oyb6h4kxs555lvd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page")
    }

    changeSet(author: "marcus (generated)", id: "1473397000359-8") {
        addForeignKeyConstraint(baseColumnNames: "job_group_id", baseTableName: "event_result", constraintName: "FK_nd8wwj4cql52rwjpo11njys5r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group")
    }
}
