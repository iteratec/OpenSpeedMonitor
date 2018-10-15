databaseChangeLog = {
    changeSet(author: "pal (generated)", id: "1539251039241-1") {
        addColumn(tableName: "job_result") {
            column(name: "job_result_status", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }
}