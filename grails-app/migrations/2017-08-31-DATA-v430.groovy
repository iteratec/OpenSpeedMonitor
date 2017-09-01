databaseChangeLog = {
    changeSet(author: "mwg", id: "1504188209-1") {
        sql('''
            UPDATE userspecific_dashboard_base
            SET   selected_aggr_group_values_un_cached =  CASE 
                        WHEN selected_aggr_group_values_un_cached LIKE 'docCompleteTimeInMillisecs%' THEN 'DOC_COMPLETE_TIME'
                        WHEN selected_aggr_group_values_un_cached LIKE 'domTimeInMillisecs%' THEN 'DOM_TIME'
                        WHEN selected_aggr_group_values_un_cached LIKE 'firstByteInMillisecs%' THEN 'FIRST_BYTE'
                        WHEN selected_aggr_group_values_un_cached LIKE 'fullyLoadedRequestCount%' THEN 'FULLY_LOADED_REQUEST_COUNT'
                        WHEN selected_aggr_group_values_un_cached LIKE 'fullyLoadedTimeInMillisecs%' THEN 'FULLY_LOADED_TIME'
                        WHEN selected_aggr_group_values_un_cached LIKE 'loadTimeInMillisecs%' THEN 'LOAD_TIME'
                        WHEN selected_aggr_group_values_un_cached LIKE 'startRenderInMillisecs%' THEN 'START_RENDER'
                        WHEN selected_aggr_group_values_un_cached LIKE 'docCompleteIncomingBytes%' THEN 'DOC_COMPLETE_INCOMING_BYTES'
                        WHEN selected_aggr_group_values_un_cached LIKE 'docCompleteRequests%' THEN 'DOC_COMPLETE_REQUESTS'
                        WHEN selected_aggr_group_values_un_cached LIKE 'fullyLoadedIncomingBytes%' THEN 'FULLY_LOADED_INCOMING_BYTES'
                        WHEN selected_aggr_group_values_un_cached LIKE 'speedIndex%' THEN 'SPEED_INDEX'
                        WHEN selected_aggr_group_values_un_cached LIKE 'visuallyCompleteInMillisecs%' THEN 'VISUALLY_COMPLETE'
                        WHEN selected_aggr_group_values_un_cached LIKE 'csByWptDocCompleteInPercent%' THEN 'CS_BY_WPT_DOC_COMPLETE'
                        WHEN selected_aggr_group_values_un_cached LIKE 'csByWptVisuallyCompleteInPercent%' THEN 'CS_BY_WPT_VISUALLY_COMPLETE'
                        ELSE selected_aggr_group_values_un_cached
                   END
            WHERE selected_aggr_group_values_un_cached IS NOT NULL
        ''')
    }

    changeSet(author: "mwg", id: "1504188209-2") {
        sql('''
            UPDATE userspecific_dashboard_base
            SET   selected_aggr_group_values_cached =  CASE 
                        WHEN selected_aggr_group_values_cached LIKE 'docCompleteTimeInMillisecs%' THEN 'DOC_COMPLETE_TIME'
                        WHEN selected_aggr_group_values_cached LIKE 'domTimeInMillisecs%' THEN 'DOM_TIME'
                        WHEN selected_aggr_group_values_cached LIKE 'firstByteInMillisecs%' THEN 'FIRST_BYTE'
                        WHEN selected_aggr_group_values_cached LIKE 'fullyLoadedRequestCount%' THEN 'FULLY_LOADED_REQUEST_COUNT'
                        WHEN selected_aggr_group_values_cached LIKE 'fullyLoadedTimeInMillisecs%' THEN 'FULLY_LOADED_TIME'
                        WHEN selected_aggr_group_values_cached LIKE 'loadTimeInMillisecs%' THEN 'LOAD_TIME'
                        WHEN selected_aggr_group_values_cached LIKE 'startRenderInMillisecs%' THEN 'START_RENDER'
                        WHEN selected_aggr_group_values_cached LIKE 'docCompleteIncomingBytes%' THEN 'DOC_COMPLETE_INCOMING_BYTES'
                        WHEN selected_aggr_group_values_cached LIKE 'docCompleteRequests%' THEN 'DOC_COMPLETE_REQUESTS'
                        WHEN selected_aggr_group_values_cached LIKE 'fullyLoadedIncomingBytes%' THEN 'FULLY_LOADED_INCOMING_BYTES'
                        WHEN selected_aggr_group_values_cached LIKE 'speedIndex%' THEN 'SPEED_INDEX'
                        WHEN selected_aggr_group_values_cached LIKE 'visuallyCompleteInMillisecs%' THEN 'VISUALLY_COMPLETE'
                        WHEN selected_aggr_group_values_cached LIKE 'csByWptDocCompleteInPercent%' THEN 'CS_BY_WPT_DOC_COMPLETE'
                        WHEN selected_aggr_group_values_cached LIKE 'csByWptVisuallyCompleteInPercent%' THEN 'CS_BY_WPT_VISUALLY_COMPLETE'
                        ELSE selected_aggr_group_values_cached
                   END
            WHERE selected_aggr_group_values_cached IS NOT NULL
        ''')
    }
}
