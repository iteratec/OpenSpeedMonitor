import de.iteratec.osm.csi.BrowserConnectivityWeight
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.ConnectivityProfileController
import de.iteratec.osm.report.UserspecificCsiDashboard
import de.iteratec.osm.report.UserspecificEventResultDashboard

databaseChangeLog = {

    changeSet(author: "bka", id: "1459346200213-3") {
        preConditions(onFail: 'MARK_RAN') {
            primaryKeyExists(tableName: 'csi_aggregation', primaryKeyName: 'measured_valuPK')
        }
        dropPrimaryKey(tableName: 'csi_aggregation', primaryKeyName: 'measured_valuPK')
        createPrimaryKey(tableName: 'csi_aggregation', primaryKeyName: 'measured_valuPK1', columnNames: "id")
    }
    changeSet(author: "bka", id: "1459346200213-4") {
        preConditions(onFail: 'MARK_RAN') {
            primaryKeyExists(tableName: 'csi_aggregation_interval', primaryKeyName: 'measured_valuPK')
        }
        dropPrimaryKey(tableName: 'csi_aggregation_interval', primaryKeyName: 'measured_valuPK')
        createPrimaryKey(tableName: 'csi_aggregation_interval', primaryKeyName: 'measured_valuPK2', columnNames: "id")
    }
    changeSet(author: "bka", id: "1459346200213-5") {
        preConditions(onFail: 'MARK_RAN') {
            primaryKeyExists(tableName: 'csi_aggregation_update_event', primaryKeyName: 'measured_valuPK')
        }
        dropPrimaryKey(tableName: 'csi_aggregation_update_event', primaryKeyName: 'measured_valuPK')
        createPrimaryKey(tableName: 'csi_aggregation_update_event', primaryKeyName: 'measured_valuPK3', columnNames: "id")
    }

    changeSet(author: "msk", id: "1459346200213-8") {
        renameTable(oldTableName: "tag_links", newTableName: "tags_links")
    }
    changeSet(author: "msk", id: "1459346200213-9") {
        renameColumn(tableName: "job_variables", oldColumnName: "variables", newColumnName: "job_id", columnDataType: "bigint")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-10") {
        addColumn(tableName: "job_variables") {
            column(name: "variables_string", type: "varchar(255)")
        }
    }
    changeSet(author: "msk", id: "1459346200213-11") {
        renameColumn(tableName: "userspecific_dashboard_base_graph_colors", oldColumnName: "graph_colors", newColumnName: "userspecific_dashboard_base_id", columnDataType: "bigint")
    }
    changeSet(author: "msk", id: "1459346200213-12") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "userspecific_dashboard_base_id", tableName: "userspecific_dashboard_base_graph_colors")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-13") {
        addColumn(tableName: "userspecific_dashboard_base_graph_colors") {
            column(name: "graph_colors_object", type: "varchar(255)")
        }
    }

    changeSet(author: "msk", id: "1459346200213-17") {
        renameColumn(tableName: "userspecific_dashboard_base_graph_name_aliases", oldColumnName: "graph_name_aliases", newColumnName: "userspecific_dashboard_base_id", columnDataType: "bigint")
    }
    changeSet(author: "msk", id: "1459346200213-18") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "userspecific_dashboard_base_id", tableName: "userspecific_dashboard_base_graph_name_aliases")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-19") {
        addColumn(tableName: "userspecific_dashboard_base_graph_name_aliases") {
            column(name: "graph_name_aliases_object", type: "varchar(255)")
        }
    }

    changeSet(author: "marko (generated)", id: "1459346200213-23") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "bodies", tableName: "job")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-24") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "capture_video", tableName: "job")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-25") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "capture_video", tableName: "job_result")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-26") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "clearcerts", tableName: "job")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-27") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "continuous_video", tableName: "job")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-28") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "csi_aggregation_underlying_event_results_by_visually_complete_id", tableName: "csi_aggregation_event_result")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-29") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "csi_configuration_browser_connectivity_weights_id", tableName: "csi_configuration_browser_connectivity_weight")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-30") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "first_view_only", tableName: "job")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-31") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "first_view_only", tableName: "job_result")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-32") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "graphite_event_source_path_job_groups_id", tableName: "graphite_event_source_path_job_group")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-33") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "graphite_server_graphite_event_source_paths_id", tableName: "graphite_server_graphite_event_source_path")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-34") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "graphite_server_graphite_paths_id", tableName: "graphite_server_graphite_path")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-36") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "ignoressl", tableName: "job")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-37") {
        addDefaultValue(columnDataType: "int", columnName: "initial_chart_height_in_pixels", defaultValueNumeric: "400", tableName: "osm_configuration")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-38") {
        addDefaultValue(columnDataType: "int", columnName: "initial_chart_width_in_pixels", defaultValueNumeric: "400", tableName: "osm_configuration")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-39") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "job_group_graphite_servers_id", tableName: "job_group_graphite_server")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-40") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "job_set_jobs_id", tableName: "job_set_job")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-41") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "keepua", tableName: "job")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-42") {
        addDefaultValue(columnDataType: "int", columnName: "max_data_storage_time_in_months", defaultValueNumeric: "13", tableName: "osm_configuration")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-43") {
        addDefaultValue(columnDataType: "int", columnName: "max_doc_complete_time_in_millisecs", defaultValueNumeric: "180000", tableName: "osm_configuration")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-44") {
        addDefaultValue(columnDataType: "int", columnName: "min_doc_complete_time_in_millisecs", defaultValueNumeric: "250", tableName: "osm_configuration")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-45") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "multistep", tableName: "job_result")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-46") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "noscript", tableName: "job")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-47") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "passwd", tableName: "user")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-48") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "event_job_groups_id", tableName: "event_job_group")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-49") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "provide_authenticate_information", tableName: "job_result")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-50") {
        dropNotNullConstraint(columnDataType: "int", columnName: "queuethreshold", tableName: "location")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-51") {
        dropNotNullConstraint(columnDataType: "int", columnName: "queuethresholdgreenlimit", tableName: "location")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-52") {
        dropNotNullConstraint(columnDataType: "int", columnName: "queuethresholdredlimit", tableName: "location")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-53") {
        dropNotNullConstraint(columnDataType: "int", columnName: "queuethresholdyellowlimit", tableName: "location")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-54") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "script_tested_pages_id", tableName: "script_page")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-55") {
        dropNotNullConstraint(columnDataType: "int", columnName: "selected_time_frame_interval", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-57") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "standards", tableName: "job")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-58") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "tcpdump", tableName: "job")
    }


    changeSet(author: "marko (generated)", id: "1459346200213-59") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "web10", tableName: "job")
    }
    changeSet(author: "marko (generated)", id: "1459346200213-60") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "csi_configuration_page_weights_id", tableName: "csi_configuration_page_weight")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-61") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "csi_configuration_time_to_cs_mappings_id", tableName: "csi_configuration_time_to_cs_mapping")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-62") {
        addDefaultValue(columnDataType: "int", columnName: "default_max_download_time_in_minutes", defaultValueNumeric: "60", tableName: "osm_configuration")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-63") {
        addDefaultValue(columnDataType: "int", columnName: "detail_data_storage_time_in_weeks", defaultValueNumeric: "2", tableName: "osm_configuration")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-64") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "download_details", tableName: "job_result")
    }

    changeSet(author: "marko (generated)", id: "1459346200213-65") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "download_result_xml", tableName: "job_result")
    }
    changeSet(author: "marko (generated)", id: "1459346200213-66") {
        dropNotNullConstraint(columnDataType: "int", columnName: "activeagents", tableName: "location")
    }
    changeSet(author: "marko (generated)", id: "1459346200213-67") {
        dropNotNullConstraint(columnDataType: "boolean", columnName: "apply_validate_rule", tableName: "job_result")
    }
    changeSet(author: "marko (generated)", id: "1459346200213-68") {
        addDefaultValue(columnDataType: "boolean", columnName: "valid", defaultValueBoolean: "true", tableName: "api_key")
    }

    /**
     * In the past we had grailschanges using GORM. There was a possible bug,
     * which prevents us from using GORM in grailschanges with java 8.
     * We had to delete the old entries and rewrite this changes.
     * Because there are instances which already ran the old changelog,
     * we first check if the changelog with the given id is already in the database. If this is not
     * the case we can safely execute the rewritten changelog
     **/
    changeSet(author: "mmi", id: "1465407008000-1") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 0, "select count(*) from DATABASECHANGELOG where id = '1459346200213-69'")
        }
        grailsChange{
            change{
                sql.eachRow("SELECT * FROM connectivity_profile") { connectivityProfile ->
                    if(!connectivityProfile.active && !connectivityProfile.name.endsWith("_old_"+String.valueOf(connectivityProfile.id))) {
                        def activeConnectivityProfileId = sql.firstRow("SELECT id FROM connectivity_profile WHERE name = :name AND active = true", [name:connectivityProfile.name]).id
                        sql.eachRow("SELECT * FROM browser_connectivity_weight where connectivity_id = :conn_id", [conn_id:connectivityProfile.id]) { oldBrowserConnectivityWeight ->
                            def browserId = oldBrowserConnectivityWeight.browser_id
                            def newBrowserConnectivityWeightID = sql.executeInsert("INSERT INTO browser_connectivity_weight (version, browser_id, connectivity_id, weight) VALUES (0, :browser_id, :conn_id, :weight)",
                                    [browser_id:browserId, conn_id: activeConnectivityProfileId, weight:oldBrowserConnectivityWeight.weight])[0][0] // gets id of inserted object
                            sql.eachRow("SELECT * FROM csi_configuration") {currentCsiConfiguration ->
                                // if there was the old connectivity weight connected to the current csi configuration, add the new csi configuration, too
                                def oldBrowserWeightInCsiConfiguration = sql.firstRow(
                                        "SELECT * FROM csi_configuration_browser_connectivity_weight where csi_configuration_browser_connectivity_weights_id = :current_csi_conf_id AND browser_connectivity_weight_id = :old_id",
                                        [current_csi_conf_id:currentCsiConfiguration.id, old_id: oldBrowserConnectivityWeight.id])
                                if(oldBrowserWeightInCsiConfiguration) {
                                    sql.executeInsert("INSERT INTO csi_configuration_browser_connectivity_weight (csi_configuration_browser_connectivity_weights_id, browser_connectivity_weight_id) VALUES (:conf_id, :new_weight_id)",
                                            [conf_id: currentCsiConfiguration.id, new_weight_id:newBrowserConnectivityWeightID])
                                }
                            }
                        }

                        String newName = connectivityProfile.name + "_old_" + String.valueOf(connectivityProfile.id)
                        sql.executeUpdate("UPDATE connectivity_profile SET name = :new_name WHERE id = :old_id", [new_name:newName, old_id: connectivityProfile.id])
                    }
                }
            }
        }
    }

    changeSet(author: "marko (generated)", id: "1459346200213-70") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_CONNECTIVITY_PROFILENAME_COL", tableName: "connectivity_profile")
    }
    changeSet(author: "marko (generated)", id: "1459346200213-71") {
        addColumn(tableName: "graphite_server") {
            column(name: "garbage_collector_prefix", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marko (generated)", id: "1459346200213-72") {
        addColumn(tableName: "graphite_server") {
            column(name: "health_metrics_report_prefix", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marko (generated)", id: "1459346200213-73") {
        addColumn(tableName: "graphite_server") {
            column(name: "memory_report_prefix", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marko (generated)", id: "1459346200213-74") {
        addColumn(tableName: "graphite_server") {
            column(name: "report_health_metrics", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marko (generated)", id: "1459346200213-75") {
        addColumn(tableName: "graphite_server") {
            column(name: "thread_states_report_prefix", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "marko (generated)", id: "1459346200213-76") {
        addColumn(tableName: "graphite_server") {
            column(name: "process_cpu_load_prefix", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "marko (generated)", id: "1459346200213-77") {
        addColumn(tableName: "graphite_server") {
            column(name: "time_between_reports_in_seconds", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marko", id: "1459346200213-78"){
        sql('''
				UPDATE graphite_server SET garbage_collector_prefix = 'jvm.gc', health_metrics_report_prefix = 'osm.healthmetrics',
				 thread_states_report_prefix = 'jvm.threads', report_health_metrics = 0, memory_report_prefix= 'jvm.mem',
				 time_between_reports_in_seconds = 300, process_cpu_load_prefix= 'cpu.processCpuLoadPrefix'
			''')

    }
}
