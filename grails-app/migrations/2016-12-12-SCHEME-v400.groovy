databaseChangeLog = {
    // ### END Refactoring of tag attribute ###
    changeSet(author: "sburnicki", id: "1481532907000-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'result_selection_information')
            }
        }
        createTable(tableName: "result_selection_information") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "result_selection_informationPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "job_result_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "job_group_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "page_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "measured_event_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "browser_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "location_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "connectivity_profile_id", type: "BIGINT")

            column(name: "custom_connectivity_name", type: "VARCHAR(255)")

            column(name: "no_traffic_shaping_at_all", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sburnicki", id: "1481532907000-2") {
        sql('''
            INSERT INTO result_selection_information
                (version, job_result_date, page_id, measured_event_id, job_group_id, location_id, browser_id,
                 connectivity_profile_id, custom_connectivity_name, no_traffic_shaping_at_all)
            SELECT
                0 as version,
                DATE(job_result_date) as job_result_date,
                page_id,
                measured_event_id,
                job_group_id,
                location_id,
                browser_id,
                connectivity_profile_id,
                custom_connectivity_name,
                no_traffic_shaping_at_all
            FROM
                event_result
            GROUP BY
                DATE(job_result_date), page_id, measured_event_id, job_group_id, location_id, browser_id,
                connectivity_profile_id, custom_connectivity_name, no_traffic_shaping_at_all
        ''')
    }

    changeSet(author: "sburnicki", id: "1481532907000-3") {
        createIndex(indexName: "get_job_groups", tableName: "result_selection_information") {
            column(name: "job_result_date")
            column(name: "job_group_id")
        }

        createIndex(indexName: "get_measured_events", tableName: "result_selection_information") {
            column(name: "job_result_date")
            column(name: "page_id")
            column(name: "measured_event_id")
        }

        createIndex(indexName: "get_locations", tableName: "result_selection_information") {
            column(name: "job_result_date")
            column(name: "location_id")
            column(name: "browser_id")
        }

        createIndex(indexName: "get_connectivity_profile", tableName: "result_selection_information") {
            column(name: "job_result_date")
            column(name: "connectivity_profile_id")
        }

        createIndex(indexName: "get_connectivity_custom", tableName: "result_selection_information") {
            column(name: "job_result_date")
            column(name: "custom_connectivity_name")
        }

        createIndex(indexName: "get_connectivity_native", tableName: "result_selection_information") {
            column(name: "job_result_date")
            column(name: "no_traffic_shaping_at_all")
        }
    }

    changeSet(author: "sburnicki", id: "1481532907000-4") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "result_selection_information", constraintName: "FK_4c1xac3qk90xo5tukpbacmt7b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "location")
        addForeignKeyConstraint(baseColumnNames: "page_id", baseTableName: "result_selection_information", constraintName: "FK_9cwa9qfpnogv9sb0djsisrm98", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page")
        addForeignKeyConstraint(baseColumnNames: "job_group_id", baseTableName: "result_selection_information", constraintName: "FK_ds21fqb2t1mtpcqaq19b58yye", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group")
        addForeignKeyConstraint(baseColumnNames: "browser_id", baseTableName: "result_selection_information", constraintName: "FK_duglorjv7b3t4kuk6x4047k4w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "browser")
        addForeignKeyConstraint(baseColumnNames: "connectivity_profile_id", baseTableName: "result_selection_information", constraintName: "FK_k70ecublopifycuvfky3s5vdo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "connectivity_profile")
        addForeignKeyConstraint(baseColumnNames: "measured_event_id", baseTableName: "result_selection_information", constraintName: "FK_k844bm1mhvebbikusqesa1ga6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "measured_event")
    }
}
