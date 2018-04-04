databaseChangeLog = {

    changeSet(author: "fabian (generated)", id: "1515502541622-1") {
        addColumn(tableName: "job") {
            column(name: "use_globaluasuffix", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }
}
