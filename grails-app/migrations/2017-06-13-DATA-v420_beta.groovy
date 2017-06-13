databaseChangeLog = {
    changeSet(author: 'nkuhn', id: '1497365028000-1') {
        sql('''UPDATE aggregator_type SET
               `measurand_group`='LOAD_TIMES'
               WHERE (`name`='speedIndexUncached' or `name`='speedIndexCached') and `measurand_group`='UNDEFINED';''')
    }
}
