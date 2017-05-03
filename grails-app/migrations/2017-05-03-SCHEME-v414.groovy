databaseChangeLog = {

    changeSet(author: "dkl", id: "1493799755-1") {
        addColumn(tableName: "osm_configuration") {
            column(defaultValue: "FALSE", name: "infrastructure_setup_ran", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

}