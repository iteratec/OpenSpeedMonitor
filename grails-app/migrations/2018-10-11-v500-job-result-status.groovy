databaseChangeLog = {
    changeSet(author: "pal (generated)", id: "1539251039241-1") {
        addColumn(tableName: "job_result") {
            column(name: "job_result_status", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "pal", id: "Task-1_transform_http_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'UNKNOWN'
            WHERE http_status_code = 0;
        ''')
    }

    changeSet(author: "pal", id: "Task-2_transform_http_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'PENDING'
            WHERE http_status_code = 100;
        ''')
    }

    changeSet(author: "pal", id: "Task-3_transform_http_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'IN_PROGRESS'
            WHERE http_status_code = 101;
        ''')
    }

    changeSet(author: "pal", id: "Task-4_transform_http_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'COMPLETED'
            WHERE http_status_code = 200;
        ''')
    }

    changeSet(author: "pal", id: "Task-5_transform_http_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'TESTED_APPLICATION_CLIENTERROR'
            WHERE http_status_code = 400;
        ''')
    }

    changeSet(author: "pal", id: "Task-6_transform_http_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'TEST_DID_NOT_START'
            WHERE http_status_code = 404;
        ''')
    }

    changeSet(author: "pal", id: "Task-7_transform_http_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'TEST_TIMED_OUT'
            WHERE http_status_code = 504;
        ''')
    }

    changeSet(author: "pal", id: "Task-8_transform_http_status") {
        sql('''
            UPDATE job_result
            SET job_result_status = 'LAUNCH_ERROR'
            WHERE http_status_code = 0;
        ''')
    }

    changeSet(author: "pal", id: "Task-9_transform_http_status") {
        sql('''
            UPDATE job_result
            SET job_result_status = 'WAITING'
            WHERE http_status_code = 100;
        ''')
    }

    changeSet(author: "pal", id: "Task-10_transform_http_status") {
        sql('''
            UPDATE job_result
            SET job_result_status = 'RUNNING'
            WHERE http_status_code = 101;
        ''')
    }

    changeSet(author: "pal", id: "Task-11_transform_http_status") {
        sql('''
            UPDATE job_result
            SET job_result_status = 'SUCCESS'
            WHERE http_status_code = 200;
        ''')
    }

    changeSet(author: "pal", id: "Task-12_transform_http_status") {
        sql('''
            UPDATE job_result
            SET job_result_status = 'SUCCESS'
            WHERE http_status_code = 400;
        ''')
    }

    changeSet(author: "pal", id: "Task-13_transform_http_status") {
        sql('''
            UPDATE job_result
            SET job_result_status = 'PERSISTANCE_ERROR'
            WHERE http_status_code = 404;
        ''')
    }

    changeSet(author: "pal", id: "Task-14_transform_http_status") {
        sql('''
            UPDATE job_result
            SET job_result_status = 'TIMEOUT'
            WHERE http_status_code = 504;
        ''')
    }

    changeSet(author: "pal", id: "Task-15_transform_http_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'TEST_DID_NOT_START'
            WHERE http_status_code = 500;
        ''')
    }

    changeSet(author: "pal", id: "Task-16_transform_http_status") {
        sql('''
            UPDATE job_result
            SET job_result_status = 'FAILED'
            WHERE http_status_code = 500;
        ''')
    }

    changeSet(author: "pal", id: "Task-17_transform_http_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'UNKNOWN'
            WHERE http_status_code = 900;
        ''')
    }

    changeSet(author: "pal", id: "Task-18_transform_http_status") {
        sql('''
            UPDATE job_result
            SET job_result_status = 'FAILED'
            WHERE http_status_code = 900;
        ''')
    }

    changeSet(author: "pal (generated)", id: "1539874031317-37") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "wpt_status", tableName: "job_result")
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
            SET wpt_status = 'TEST_TIMED_OUT'
            WHERE wpt_status = 'OUTDATED_JOB';
        ''')
    }

    changeSet(author: "pal", id: "Task-3_update_wpt_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'UNKNOWN'
            WHERE wpt_status = 'INVALID_TEST_ID';
        ''')
    }

    changeSet(author: "pal", id: "Task-4_update_wpt_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'IN_PROGRESS'
            WHERE wpt_status = 'RUNNING';
        ''')
    }

    changeSet(author: "pal", id: "Task-5_update_wpt_status") {
        sql('''
            UPDATE job_result
            SET wpt_status = 'WAITING'
            WHERE wpt_status = 'PENDING';
        ''')
    }

    changeSet(author: "pal", id: "Task-6_update_description_type") {
        sql('''
            ALTER TABLE job_result
            MODIFY COLUMN description LONGTEXT;
        ''')
    }

}