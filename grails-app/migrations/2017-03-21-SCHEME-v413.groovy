databaseChangeLog = {

    changeSet(author: "mmi", id: "1490080349980-1") {
        addColumn(tableName: "graphite_server") {
            column(name: "report_protocol", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

}
