databaseChangeLog = {

    changeSet(author: "fabian (generated)", id: "1513675981094-1") {
        addColumn(tableName: "osm_configuration") {
            column(name: "global_user_agent_suffix", type: "varchar(255)")
        }
    }

}
