databaseChangeLog = {
    changeSet(author: "pal (generated)", id: "1539251039241-1") {
        addColumn(tableName: "job_result") {
            column(name: "job_result_status", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "pal (generated)", id: "1539617050967-4") {
        dropColumn(columnName: "http_status_code", tableName: "job_result")
    }

    changeSet(author: "pal", id: "Task-1_update_wpt_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'TEST_TIMED_OUT'
            WHERE wpt_status = 'TIME_OUT';
        ''')
    }

    changeSet(author: "pal", id: "Task-2_update_wpt_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = ''
            WHERE wpt_status = 'OUTDATED_JOB';
        ''')
    }

    changeSet(author: "pal", id: "Task-2_update_wpt_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'UNKNOWN'
            WHERE wpt_status = 'INVALID_TEST_ID';
        ''')
    }

    changeSet(author: "pal", id: "Task-2_update_wpt_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'UNKNOWN'
            WHERE wpt_status = 'INVALID_TEST_ID';
        ''')
    }

    changeSet(author: "pal", id: "Task-2_update_wpt_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'UNKNOWN'
            WHERE wpt_status = 'INVALID_TEST_ID';
        ''')
    }

    changeSet(author: "pal", id: "Task-2_update_wpt_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'UNKNOWN'
            WHERE wpt_status = 'INVALID_TEST_ID';
        ''')
    }
}