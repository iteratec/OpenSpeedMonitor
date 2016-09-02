databaseChangeLog = {
    changeSet(author: "mmi", id: "1471947883000-1") {
        sql('''
            UPDATE job SET deleted=0;
        ''')
    }

    changeSet(author: "mmi", id: "1472133616000-1") {
        sql('''
            UPDATE job_group SET persist_detail_data=0;
        ''')
    }

    // There was a bug with nightly calculation of csiAggregations, so they have to be calculated again
    changeSet(author: "mmi", id: "1472807664000-1") {
        grailsChange {
            change {
                sql.eachRow("select id from csi_aggregation where aggregator_id in (select id from aggregator_type where name in ('page', 'shop', 'csiSystem')) and interval_id in (select id from csi_aggregation_interval where name in ('daily', 'weekly')) and cs_by_wpt_doc_complete_in_percent is null and started > DATE('2016-07-14') and closed_and_calculated=1;") { csiAggregationId ->
                    sql.executeUpdate("update csi_aggregation set closed_and_calculated = 0 where id = :currentId", [currentId: csiAggregationId.id])
                    sql.executeInsert("insert into csi_aggregation_update_event (version, date_of_update, csi_aggregation_id, update_cause) values (0, NOW(), :currentId, 'OUTDATED')", [currentId: csiAggregationId.id])
                }
            }
        }
    }
}
