databaseChangeLog = {
    changeSet(author: "mwg", id: "1500367884-1") {
        sql('''
            UPDATE event_result
            SET speed_index = null
            WHERE speed_index = -1;
        ''')
    }
}
