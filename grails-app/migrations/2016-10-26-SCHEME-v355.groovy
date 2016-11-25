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
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "event_result", constraintName: "FK_1lqpe6ckkhtlm17wdcqpp0bf4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "location")
        addForeignKeyConstraint(baseColumnNames: "browser_id", baseTableName: "event_result", constraintName: "FK_3a0bt55f0gmtigcetbyg2bovk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "browser")
        addForeignKeyConstraint(baseColumnNames: "page_id", baseTableName: "event_result", constraintName: "FK_5sk7tn8qu8oyb6h4kxs555lvd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page")
        addForeignKeyConstraint(baseColumnNames: "job_group_id", baseTableName: "event_result", constraintName: "FK_nd8wwj4cql52rwjpo11njys5r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group")
    }

    changeSet(author: "marcus (generated)", id: "1473079647061-1") {
        addColumn(tableName: "csi_aggregation") {
            column(name: "browser_id", type: "bigint")
            column(name: "job_group_id", type: "bigint")
            column(name: "location_id", type: "bigint")
            column(name: "measured_event_id", type: "bigint")
            column(name: "page_id", type: "bigint")
        }
        addForeignKeyConstraint(baseColumnNames: "page_id", baseTableName: "csi_aggregation", constraintName: "FK_ed4k8n53a9o4f2eyjf1wwn5h0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page")
        addForeignKeyConstraint(baseColumnNames: "measured_event_id", baseTableName: "csi_aggregation", constraintName: "FK_m2ihng827m0wdm7lhjw8wd0k8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "measured_event")
        addForeignKeyConstraint(baseColumnNames: "job_group_id", baseTableName: "csi_aggregation", constraintName: "FK_pep8a7052p26u3gixxny6wmp7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group")
        addForeignKeyConstraint(baseColumnNames: "browser_id", baseTableName: "csi_aggregation", constraintName: "FK_pj2t91buh552uxodjasjpxxjh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "browser")
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "csi_aggregation", constraintName: "FK_rtpndsvi65apepp2ej6lkdns5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "location")
    }

    // ### END Refactoring of tag attribute ###
}
