databaseChangeLog = {
    changeSet(author: "jweiss", id: "1498063979-1") {
        sql('''
            UPDATE csi_aggregation
            SET aggregation_type = 'MEASURED_EVENT'
            WHERE aggregator_id = (
                SELECT id
                FROM aggregator_type
                WHERE name = 'measuredEvent'
            );
        ''')
    }
    changeSet(author: "jweiss", id: "1498063979-2") {
        sql('''
            UPDATE csi_aggregation
            SET aggregation_type = 'PAGE'
            WHERE aggregator_id = (
                SELECT id
                FROM aggregator_type
                WHERE name = 'page'
            );
        ''')
    }
    changeSet(author: "jweiss", id: "1498063979-3") {
        sql('''
            UPDATE csi_aggregation
            SET aggregation_type = 'JOB_GROUP'
            WHERE aggregator_id = (
                SELECT id
                FROM aggregator_type
                WHERE name = 'shop'
            );
        ''')
    }
    changeSet(author: "jweiss", id: "1498063979-4") {
        sql('''
            UPDATE csi_aggregation
            SET aggregation_type = 'CSI_SYSTEM'
            WHERE aggregator_id = (
                SELECT id
                FROM aggregator_type
                WHERE name = 'csiSystem'
            );
        ''')
    }
}
