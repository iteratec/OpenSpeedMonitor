databaseChangeLog = {
    changeSet(author: "dkl", id: "1497618381-1") {
        addColumn(tableName: "job") {
            column(name: "trace_categories",defaultValue: "blink,v8,cc,gpu,blink.net,netlog,disabled-by-default-v8.runtime_stats", type: "varchar(255)")
        }
    }
}
