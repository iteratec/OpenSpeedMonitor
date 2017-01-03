databaseChangeLog = {

    changeSet(author: "marko (generated)", id: "1482245814394-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "script_id", tableName: "job")
    }
    changeSet(author: "marko (generated)", id: "1482245814394-2") {
        sql('''
            UPDATE job SET script_id = NULL WHERE job.deleted = 1;
        ''')
    }

    changeSet(author: "marko (generated)", id: "1483441134102-6") {
        dropColumn(columnName: "valid", tableName: "location")
    }


}
