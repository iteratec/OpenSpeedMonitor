databaseChangeLog = {

    // nightlyDatabaseCleanups can be de-/activated by REST-Call
    changeSet(author: "bka (generated)", id: "1452546683118-1") {
        addColumn(tableName: "api_key") {
            column(name: "allowed_for_nightly_database_cleanup_activation", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-1") {
        createTable(tableName: "csi_system") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "csi_systemPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "label", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-2") {
        createTable(tableName: "csi_system_job_group_weight") {
            column(name: "csi_system_job_group_weights_id", type: "bigint")

            column(name: "job_group_weight_id", type: "bigint")

            column(name: "job_group_weights_idx", type: "integer")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-3") {
        createTable(tableName: "job_group_weight") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "job_group_weiPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "job_group_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "weight", type: "double precision") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-6") {
        createIndex(indexName: "label_uniq_1454506330929", tableName: "csi_system", unique: "true") {
            column(name: "label")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-7") {
        createIndex(indexName: "FK588975446326A28C", tableName: "csi_system_job_group_weight") {
            column(name: "job_group_weight_id")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-8") {
        createIndex(indexName: "FK5044557A48E56BA7", tableName: "job_group_weight") {
            column(name: "job_group_id")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-4") {
        addForeignKeyConstraint(baseColumnNames: "job_group_weight_id", baseTableName: "csi_system_job_group_weight", constraintName: "FK588975446326A28C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group_weight", referencesUniqueColumn: "false")
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-5") {
        addForeignKeyConstraint(baseColumnNames: "job_group_id", baseTableName: "job_group_weight", constraintName: "FK5044557A48E56BA7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group", referencesUniqueColumn: "false")
    }

    // [BEGIN] changes for custom event result dashboard /////////////////////////////////////////////////////////////////////
    changeSet(author: "bwo (generated)", id: "1453899727017-1") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "chart_title", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-2") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "chart_width", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-3") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "chart_height", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-4") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "load_time_maximum", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-5") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "load_time_minimum", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-6") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "show_data_labels", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-7") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "show_data_markers", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-8") {
        modifyDataType(columnName: "weight", newDataType: "double precision", tableName: "page")
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-9") {
        dropNotNullConstraint(columnDataType: "double precision", columnName: "weight", tableName: "page")
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-10") {
        dropIndex(indexName: "full_hour", tableName: "hour_of_day")
    }

    // [END] changes for custom event result dashboard /////////////////////////////////////////////////////////////////////

    // [BEGIN] changes for custom csi dashboard /////////////////////////////////////////////////////////////////////

    changeSet(author: "bwo (generated)", id: "1454590842460-1") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "chart_height", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1454590842460-2") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "chart_title", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1454590842460-3") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "chart_width", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1454590842460-4") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "load_time_maximum", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1454590842460-5") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "load_time_minimum", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1454590842460-6") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "show_data_labels", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1454590842460-7") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "show_data_markers", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    // [END] changes for custom csi dashboard /////////////////////////////////////////////////////////////////////

    // add initial chart width to OsmConfiguration
    changeSet(author: "bwo (generated)", id: "1454675900207-1") {
        addColumn(tableName: "osm_configuration") {
            column(defaultValue: "1000", name: "initial_chart_width_in_pixels", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

//    ### add csiSystem to MeasuredValue
    changeSet(author: "marcus (generated)", id: "1454929626682-1") {
        addColumn(tableName: "measured_value") {
            column(name: "csi_system_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454929626682-3") {
        createIndex(indexName: "FKCC54EB8B8A7ADFB", tableName: "measured_value") {
            column(name: "csi_system_id")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454929626682-2") {
        addForeignKeyConstraint(baseColumnNames: "csi_system_id", baseTableName: "measured_value", constraintName: "FKCC54EB8B8A7ADFB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "csi_system", referencesUniqueColumn: "false")
    }
//    ### end add csiSystem to MeasuredValue
}