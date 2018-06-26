databaseChangeLog = {
    changeSet(author: "mmi", id: "1473339145000-1") {
        sql('''
            UPDATE graphite_server SET report_csi_aggregations_to_graphite_server=0;
        ''')
    }

    changeSet(author: "mmi", id: "1473339145000-2") {
        sql('''
            UPDATE graphite_server SET report_event_results_to_graphite_server=1;
        ''')
    }

    // There was a bug with nightly calculation of csiAggregations while reporting to graphite server, so they have to be calculated again
    // This changeset only runs in production if some csi_aggregations already exist. In test environment with h2 database
    // it wouldn't run successfully, because h2 doesn't have a DATE function.
    changeSet(author: "mmi", id: "1473339145000-3") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', 'select count(id)>0 from csi_aggregation;')
        }
        grailsChange {
            change {
                sql.eachRow("select id from csi_aggregation where aggregator_id in (select id from aggregator_type where name in ('page', 'shop', 'csiSystem')) and interval_id in (select id from csi_aggregation_interval where name in ('daily', 'weekly')) and cs_by_wpt_doc_complete_in_percent is null and started > DATE('2016-07-14');") { csiAggregationId ->
                    sql.executeUpdate("update csi_aggregation set closed_and_calculated = 0 where id = :currentId", [currentId: csiAggregationId.id])
                    sql.executeInsert("insert into csi_aggregation_update_event (version, date_of_update, csi_aggregation_id, update_cause) values (0, NOW(), :currentId, 'OUTDATED')", [currentId: csiAggregationId.id])
                }
            }
        }
    }
}
