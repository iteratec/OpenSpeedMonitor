databaseChangeLog = {
    changeSet(author: "owe (generated)", id: "1529315093839-1") {
        addColumn(tableName: "script") {
            column(name: "updated", type: "boolean") {
                constraints(nullable: "false")
            }

            sql(''' update script set updated = false ''' )
        }
    }
}