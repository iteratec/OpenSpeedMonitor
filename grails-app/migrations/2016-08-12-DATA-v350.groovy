databaseChangeLog = {
    changeSet(author: "mmi", id: "1471947883000-1") {
        sql('''
            UPDATE job SET deleted=0;
        ''')
    }
}
