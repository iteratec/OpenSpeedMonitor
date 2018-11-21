databaseChangeLog = {
    changeSet(author: "fwieczorek (generated)", id: "1542641527133-44") {
        createTable(tableName: "missing_job_result_check") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "missing_job_result_checkPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "missing_results", type: "int") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "fwieczorek", id: "201811201007") {
        addColumn(tableName: "job_result") {
            column(name: "execution_date", type: "datetime")
        }
    }
}
