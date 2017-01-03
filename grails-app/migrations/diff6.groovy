databaseChangeLog = {

    changeSet(author: "marko (generated)", id: "1483463294023-1") {
        createTable(tableName: "archived_script") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "archived_scriptPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "archive_tag", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "label", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "navigation_script", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "script_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marko (generated)", id: "1483463294023-2") {
        addForeignKeyConstraint(baseColumnNames: "script_id", baseTableName: "archived_script", constraintName: "FK_qp572xq18h8ccjemkqgacq0x1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "script")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-3") {
        addDefaultValue(columnDataType: "boolean", columnName: "active", defaultValueBoolean: "true", tableName: "connectivity_profile")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-4") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "browser_id", tableName: "event_result")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-5") {
        dropNotNullConstraint(columnDataType: "int", columnName: "failures", tableName: "batch_activity")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-6") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "job_group_id", tableName: "event_result")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-7") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "location_id", tableName: "event_result")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-8") {
        dropNotNullConstraint(columnDataType: "int", columnName: "maximum_stages", tableName: "batch_activity")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-9") {
        dropNotNullConstraint(columnDataType: "int", columnName: "maximum_steps_in_stage", tableName: "batch_activity")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-10") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "measured_event_id", tableName: "event_result")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-11") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "page_id", tableName: "event_result")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-12") {
        addDefaultValue(columnDataType: "boolean", columnName: "persist_non_median_results", defaultValueComputed: "1", tableName: "job")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-13") {
        dropNotNullConstraint(columnDataType: "int", columnName: "step_in_stage", tableName: "batch_activity")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-14") {
        addDefaultValue(columnDataType: "boolean", columnName: "valid", defaultValueBoolean: "true", tableName: "api_key")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-15") {
        addDefaultValue(columnDataType: "boolean", columnName: "valid", defaultValueBoolean: "true", tableName: "micro_service_api_key")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-16") {
        dropPrimaryKey(tableName: "user_role")

        addPrimaryKey(columnNames: "user_id, role_id", constraintName: "user_rolePK", tableName: "user_role")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-17") {
        dropIndex(indexName: "GetLimitedMedianEventResultsBy", tableName: "event_result")

        createIndex(indexName: "GetLimitedMedianEventResultsBy", tableName: "event_result") {
            column(name: "job_result_date")
        }
    }

    changeSet(author: "marko (generated)", id: "1483463294023-18") {
        dropIndex(indexName: "closedAndCalculated_and_started_idx", tableName: "csi_aggregation")

        createIndex(indexName: "closedAndCalculated_and_started_idx", tableName: "csi_aggregation") {
            column(name: "closed_and_calculated")

            column(name: "started")
        }
    }

    changeSet(author: "marko (generated)", id: "1483463294023-19") {
        dropIndex(indexName: "csiAggregationId_idx", tableName: "csi_aggregation_update_event")

        createIndex(indexName: "csiAggregationId_idx", tableName: "csi_aggregation_update_event") {
            column(name: "csi_aggregation_id")
        }
    }

    changeSet(author: "marko (generated)", id: "1483463294023-20") {
        dropIndex(indexName: "date_idx", tableName: "job_result")

        createIndex(indexName: "date_idx", tableName: "job_result") {
            column(name: "date")
        }
    }

    changeSet(author: "marko (generated)", id: "1483463294023-21") {
        dropIndex(indexName: "jobResultDate_and_jobResultJobConfigId_idx", tableName: "event_result")

        createIndex(indexName: "jobResultDate_and_jobResultJobConfigId_idx", tableName: "event_result") {
            column(name: "job_result_date")

            column(name: "job_result_job_config_id")
        }
    }

    changeSet(author: "marko (generated)", id: "1483463294023-22") {
        dropIndex(indexName: "started_and_iVal_and_aggr_idx", tableName: "csi_aggregation")

        createIndex(indexName: "started_and_iVal_and_aggr_idx", tableName: "csi_aggregation") {
            column(name: "aggregator_id")

            column(name: "interval_id")

            column(name: "started")
        }
    }

    changeSet(author: "marko (generated)", id: "1483463294023-23") {
        dropIndex(indexName: "testId_and_jobConfigLabel_idx", tableName: "job_result")

        createIndex(indexName: "testId_and_jobConfigLabel_idx", tableName: "job_result") {
            column(name: "job_config_label")

            column(name: "test_id")
        }
    }

    changeSet(author: "marko (generated)", id: "1483463294023-24") {
        dropIndex(indexName: "wJRD_and_wJRJCId_and_mV_and_cV_idx", tableName: "event_result")

        createIndex(indexName: "wJRD_and_wJRJCId_and_mV_and_cV_idx", tableName: "event_result") {
            column(name: "cached_view")

            column(name: "job_result_date")

            column(name: "job_result_job_config_id")

            column(name: "median_value")
        }
    }
}
