databaseChangeLog = {

    changeSet(author: "pal (generated)", id: "1556791671349-1") {
        addColumn(tableName: "location") {
            column(name: "device_type", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "pal (generated)", id: "1556791671349-2") {
        addColumn(tableName: "location") {
            column(name: "operating_system", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }
}
