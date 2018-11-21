databaseChangeLog = {

    changeSet(author: "fmartens (generated)", id: "1542619311106-1") {
        addColumn(tableName: "job_group_graphite_server") {
            column(name: "job_group_job_health_graphite_servers_id", type: "bigint")
        }
    }

    changeSet(author: "fmartens (generated)", id: "1542619311106-2") {
        addColumn(tableName: "job_group_graphite_server") {
            column(name: "job_group_result_graphite_servers_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "fmartens", id: "Task-1_copy_existing_ids") {
        sql('''
            UPDATE job_group_graphite_server
            SET job_group_result_graphite_servers_id = job_group_graphite_servers_id;
        ''')
    }

    changeSet(author: "fmartens (generated)", id: "1542619311106-6") {
        addForeignKeyConstraint(baseColumnNames: "job_group_job_health_graphite_servers_id", baseTableName: "job_group_graphite_server", constraintName: "FKaleqoqn1ue7wyw4jhv4kpb800", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group")
    }

    changeSet(author: "fmartens (generated)", id: "1542619311106-7") {
        addForeignKeyConstraint(baseColumnNames: "job_group_result_graphite_servers_id", baseTableName: "job_group_graphite_server", constraintName: "FKpsg9lbtc0vi09j7pxl0kl7ljm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group")
    }

    changeSet(author: "fmartens (generated)", id: "1542619311106-8") {
        dropForeignKeyConstraint(baseTableName: "job_group_graphite_server", constraintName: "FK4CA1B9942DF50285")
    }

    changeSet(author: "fmartens (generated)", id: "1542619311106-9") {
        dropColumn(columnName: "job_group_graphite_servers_id", tableName: "job_group_graphite_server")
    }

    changeSet(author: "finn (generated)", id: "1542627777036-1") {
        addColumn(tableName: "graphite_server") {
            column(name: "prefix", type: "varchar(255)")
        }
    }
}