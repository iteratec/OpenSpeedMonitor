databaseChangeLog = {
    // so far only tcp connections were supported
    changeSet(author: 'mmi', id: '1490080349980-2') {
        sql('''UPDATE graphite_server SET report_protocol = 'TCP' ''')
    }
}
