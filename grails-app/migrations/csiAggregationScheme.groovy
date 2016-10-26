databaseChangeLog = {

    changeSet(author: "marcus (generated)", id: "1473079647061-1") {
        addColumn(tableName: "csi_aggregation") {
            column(name: "browser_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1473079647061-2") {
        addColumn(tableName: "csi_aggregation") {
            column(name: "job_group_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1473079647061-3") {
        addColumn(tableName: "csi_aggregation") {
            column(name: "location_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1473079647061-4") {
        addColumn(tableName: "csi_aggregation") {
            column(name: "measured_event_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1473079647061-5") {
        addColumn(tableName: "csi_aggregation") {
            column(name: "page_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1473079647061-6") {
        addForeignKeyConstraint(baseColumnNames: "page_id", baseTableName: "csi_aggregation", constraintName: "FK_ed4k8n53a9o4f2eyjf1wwn5h0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page")
    }

    changeSet(author: "marcus (generated)", id: "1473079647061-7") {
        addForeignKeyConstraint(baseColumnNames: "measured_event_id", baseTableName: "csi_aggregation", constraintName: "FK_m2ihng827m0wdm7lhjw8wd0k8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "measured_event")
    }

    changeSet(author: "marcus (generated)", id: "1473079647061-8") {
        addForeignKeyConstraint(baseColumnNames: "job_group_id", baseTableName: "csi_aggregation", constraintName: "FK_pep8a7052p26u3gixxny6wmp7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group")
    }

    changeSet(author: "marcus (generated)", id: "1473079647061-9") {
        addForeignKeyConstraint(baseColumnNames: "browser_id", baseTableName: "csi_aggregation", constraintName: "FK_pj2t91buh552uxodjasjpxxjh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "browser")
    }

    changeSet(author: "marcus (generated)", id: "1473079647061-10") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "csi_aggregation", constraintName: "FK_rtpndsvi65apepp2ej6lkdns5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "location")
    }
}
