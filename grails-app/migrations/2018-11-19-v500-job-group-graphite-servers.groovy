databaseChangeLog = {

    changeSet(author: "fmartens (generated)", id: "1542818276176-1") {
        createTable(tableName: "job_group_job_health_graphite_server") {
            column(name: "job_group_job_health_graphite_servers_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "graphite_server_id", type: "BIGINT")
        }
    }

    changeSet(author: "fmartens (generated)", id: "1542818276176-2") {
        createTable(tableName: "job_group_result_graphite_server") {
            column(name: "job_group_result_graphite_servers_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "graphite_server_id", type: "BIGINT")
        }
    }

    changeSet(author: "fmartens (generated)", id: "1542818276176-6") {
        addForeignKeyConstraint(baseColumnNames: "graphite_server_id", baseTableName: "job_group_result_graphite_server", constraintName: "FK5tujea0954ys419lpvuwh7j7v", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_server")
    }

    changeSet(author: "fmartens (generated)", id: "1542818276176-7") {
        addForeignKeyConstraint(baseColumnNames: "job_group_job_health_graphite_servers_id", baseTableName: "job_group_job_health_graphite_server", constraintName: "FK9u6fbxgma0mkqt5ty19cca3c1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group")
    }

    changeSet(author: "fmartens (generated)", id: "1542818276176-8") {
        addForeignKeyConstraint(baseColumnNames: "job_group_result_graphite_servers_id", baseTableName: "job_group_result_graphite_server", constraintName: "FKbwn4g8e5i52gqkj0blbg9nnpg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group")
    }

    changeSet(author: "fmartens (generated)", id: "1542818276176-9") {
        addForeignKeyConstraint(baseColumnNames: "graphite_server_id", baseTableName: "job_group_job_health_graphite_server", constraintName: "FKtr53abjdih801s0wir346d7wk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_server")
    }

    changeSet(author: "fmartens", id: "Task-1_drop_all_foreign_keys") {
        dropAllForeignKeyConstraints(baseTableName: "job_group_graphite_server")
    }

    changeSet(author: "fmartens", id: "Task-2_copy_existing_ids") {
        sql('''
            INSERT INTO job_group_result_graphite_server (job_group_result_graphite_servers_id, graphite_server_id)
            SELECT job_group_graphite_servers_id, graphite_server_id
            FROM job_group_graphite_server;
        ''')
    }

    changeSet(author: "fmartens (generated)", id: "1542818276176-12") {
        dropTable(tableName: "job_group_graphite_server")
    }

    changeSet(author: "fmartens (generated)", id: "1542806179265-1") {
        addColumn(tableName: "graphite_server") {
            column(name: "prefix", type: "varchar(255)")
        }
    }
}