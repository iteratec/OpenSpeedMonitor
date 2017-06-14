databaseChangeLog = {
    changeSet(author: 'nkuhn', id: '1497365028000-1') {
        sql('''UPDATE aggregator_type SET
               `measurand_group`='LOAD_TIMES'
               WHERE (`name`='speedIndexUncached' or `name`='speedIndexCached') and `measurand_group`='UNDEFINED';''')
    }
    changeSet(author: 'nkuhn', id: '1497450101000-1') {
        sql('''UPDATE osm_configuration SET `min_doc_complete_time_in_millisecs`=10
            WHERE `min_doc_complete_time_in_millisecs`=250;''')
    }
}
