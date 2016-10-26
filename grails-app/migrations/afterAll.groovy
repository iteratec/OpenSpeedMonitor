databaseChangeLog = {
    changeSet(author: "mmi", id: "1473397000359-9") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "browser_id", tableName: "event_result")
    }

    changeSet(author: "mmi", id: "1473397000359-10") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "job_group_id", tableName: "event_result")
    }

    changeSet(author: "mmi", id: "1473397000359-11") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "location_id", tableName: "event_result")
    }

    changeSet(author: "mmi", id: "1473397000359-12") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "page_id", tableName: "event_result")
    }

    changeSet(author: "mmi", id: "1473397000359-13") {
        dropColumn(columnName: "tag", tableName: "event_result")
    }
    changeSet(author: "mmi", id: "1473397000359-14") {
        dropColumn(columnName: "tag", tableName: "csi_aggregation")
    }

    // Delete csiAggregationUpdateEvents which belong to already deleted csiAggregations.
    changeSet(author: "mmi", id: "1477505809000-1") {
        sql('''
            delete from csi_aggregation_update_event where csi_aggregation_id not in (
                select id from csi_aggregation);
        ''')
    }
}
