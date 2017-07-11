databaseChangeLog = {
    changeSet(author: "mwg", id: "1499164819-1") {
        sql('''
            INSERT INTO graphite_path_csi_data ( prefix, graphite_server_id, aggregation_type, version)
            SELECT a.prefix AS prefix, 
                   a.graphite_server_id AS graphite_server_id,
                   CASE 
                       WHEN b.name = 'shop' THEN 'JOB_GROUP'
                       WHEN b.name =  'page' THEN 'PAGE'
                       WHEN b.name = 'measuredEvent' THEN 'MEASURED_EVENT'
                       WHEN b.name = 'csiSystem' THEN 'CSI_SYSTEM'
                   END
                        AS aggregation_type,
                   a.version AS version
            FROM 
            (SELECT prefix, graphite_server_graphite_paths_id AS graphite_server_id, measurand_id, version 
                FROM graphite_path
                INNER JOIN graphite_server_graphite_path 
                ON graphite_path.id = graphite_server_graphite_path.graphite_path_id) AS a
                INNER JOIN 
                (SELECT id, name FROM aggregator_type 
                    WHERE measurand_group = 'NO_MEASURAND') AS b 
                ON a.measurand_id = b.id;
        ''')

    }

    changeSet(author: "mwg", id: "1499164819-2") {
        sql('''
            INSERT INTO graphite_path_raw_data ( prefix, graphite_server_id, cached_view, measurand, version)
            SELECT a.prefix AS prefix, 
                   a.graphite_server_id AS graphite_server_id,
                   CASE 
                        WHEN b.name LIKE '%Uncached' THEN 'UNCACHED'
                        WHEN b.name LIKE '%Cached' THEN 'CACHED'
                   END
                        AS cached_view,
                   CASE 
                        WHEN b.name LIKE 'docCompleteTimeInMillisecs%' THEN 'DOC_COMPLETE_TIME'
                        WHEN b.name LIKE 'domTimeInMillisecs%' THEN 'DOM_TIME'
                        WHEN b.name LIKE 'firstByteInMillisecs%' THEN 'FIRST_BYTE'
                        WHEN b.name LIKE 'fullyLoadedRequestCount%' THEN 'FULLY_LOADED_REQUEST_COUNT'
                        WHEN b.name LIKE 'fullyLoadedTimeInMillisecs%' THEN 'FULLY_LOADED_TIME'
                        WHEN b.name LIKE 'loadTimeInMillisecs%' THEN 'LOAD_TIME'
                        WHEN b.name LIKE 'startRenderInMillisecs%' THEN 'START_RENDER'
                        WHEN b.name LIKE 'docCompleteIncomingBytes%' THEN 'DOC_COMPLETE_INCOMING_BYTES'
                        WHEN b.name LIKE 'docCompleteRequests%' THEN 'DOC_COMPLETE_REQUESTS'
                        WHEN b.name LIKE 'fullyLoadedIncomingBytes%' THEN 'FULLY_LOADED_INCOMING_BYTES'
                        WHEN b.name LIKE 'speedIndex%' THEN 'SPEED_INDEX'
                        WHEN b.name LIKE 'visuallyCompleteInMillisecs%' THEN 'VISUALLY_COMPLETE'
                        WHEN b.name LIKE 'csByWptDocCompleteInPercent%' THEN 'CS_BY_WPT_DOC_COMPLETE'
                        WHEN b.name LIKE 'csByWptVisuallyCompleteInPercent%' THEN 'CS_BY_WPT_VISUALLY_COMPLETE'
                   END
                        AS measurand,
                   a.version AS version
            FROM 
            (SELECT prefix, graphite_server_graphite_paths_id AS graphite_server_id, measurand_id, version 
                FROM graphite_path 
                INNER JOIN graphite_server_graphite_path 
                ON id = graphite_path_id) AS a
                INNER JOIN 
                (SELECT id, name FROM aggregator_type 
                    WHERE measurand_group != 'NO_MEASURAND') AS b 
                ON a.measurand_id = b.id;
        ''')
    }
}
