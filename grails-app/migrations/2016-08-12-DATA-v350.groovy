databaseChangeLog = {
    changeSet(author: "mmi", id: "1471947883000-1") {
        sql('''
            UPDATE job SET deleted=0;
        ''')
    }

    changeSet(author: "mmi", id: "1472133616000-1") {
        sql('''
            UPDATE job_group SET persist_detail_data=0;
        ''')
    }
}
