databaseChangeLog = {
    changeSet(author: "nkuhn (generated)", id: "1556619486219-1") {
        sql("delete from performance_aspect" )
    }
    changeSet(author: "nkuhn (generated)", id: "1556619486219-2") {
        addColumn(tableName: "performance_aspect") {
            column(name: "browser_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }
}