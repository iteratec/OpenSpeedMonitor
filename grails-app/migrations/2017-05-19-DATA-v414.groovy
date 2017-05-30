databaseChangeLog = {
    // so far only tcp connections were supported
    changeSet(author: 'dkl', id: '1495182423000-1') {
        sql('''UPDATE job SET save_bodies = 'ALL' WHERE bodies = 1''')
    }
}
