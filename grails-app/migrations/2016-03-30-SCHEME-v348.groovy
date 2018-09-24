import de.iteratec.osm.csi.BrowserConnectivityWeight
import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.ConnectivityProfileController
import de.iteratec.osm.report.UserspecificCsiDashboard
import de.iteratec.osm.report.UserspecificEventResultDashboard

databaseChangeLog = {

    changeSet(author: "bka", id: "1459346200213-1") {
        dropForeignKeyConstraint(baseTableName: "http_archive", constraintName: "FK86FC880BF0C41D41")
    }

    changeSet(author: "bka", id: "1459346200213-2") {
        dropTable(tableName: "http_archive")
    }

//      ### UserspecificDashboard refactoring
    changeSet(author: "marcus (generated)", id: "1460099558388-1") {
        createTable(tableName: "userspecific_dashboard_base") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "userspecific_PK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "chart_height", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "chart_title", type: "varchar(255)")

            column(name: "chart_width", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "dashboard_name", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "debug", type: "bit")

            column(name: "from_date", type: "datetime")

            column(name: "from_hour", type: "varchar(255)")

            column(name: "load_time_maximum", type: "varchar(255)")

            column(name: "load_time_minimum", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "overwrite_warning_about_long_processing_time", type: "bit")

            column(name: "publicly_visible", type: "bit")

            column(name: "selected_all_browsers", type: "bit")

            column(name: "selected_all_locations", type: "bit")

            column(name: "selected_all_measured_events", type: "bit")

            column(name: "selected_browsers", type: "varchar(255)")

            column(name: "selected_folder", type: "varchar(255)")

            column(name: "selected_locations", type: "varchar(255)")

            column(name: "selected_measured_event_ids", type: "varchar(255)")

            column(name: "selected_pages", type: "varchar(255)")

            column(name: "selected_time_frame_interval", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "set_from_hour", type: "bit")

            column(name: "set_to_hour", type: "bit")

            column(name: "show_data_labels", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "show_data_markers", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "to_date", type: "datetime")

            column(name: "to_hour", type: "varchar(255)")

            column(name: "username", type: "varchar(255)")

            column(name: "wide_screen_diagram_montage", type: "bit")

            column(name: "class", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "custom_connectivity_name", type: "varchar(255)")

            column(name: "include_custom_connectivity", type: "bit")

            column(name: "include_native_connectivity", type: "bit")

            column(name: "selected_aggr_group_values_cached", type: "varchar(255)")

            column(name: "selected_aggr_group_values_un_cached", type: "varchar(255)")

            column(name: "selected_all_connectivity_profiles", type: "bit")

            column(name: "selected_connectivity_profiles", type: "varchar(255)")

            column(name: "selected_interval", type: "integer")

            column(name: "trim_above_load_times", type: "integer")

            column(name: "trim_above_request_counts", type: "integer")

            column(name: "trim_above_request_sizes", type: "integer")

            column(name: "trim_below_load_times", type: "integer")

            column(name: "trim_below_request_counts", type: "integer")

            column(name: "trim_below_request_sizes", type: "integer")

            column(name: "aggr_group", type: "varchar(255)")

            column(name: "csi_type_doc_complete", type: "bit")

            column(name: "csi_type_visually_complete", type: "bit")

            column(name: "include_interval", type: "bit")

            column(name: "selected_csi_systems", type: "varchar(255)")
        }
    }

    changeSet(author: "marcus (generated)", id: "1460099558388-2") {
        createTable(tableName: "userspecific_dashboard_base_graph_colors") {
            column(name: "graph_colors", type: "bigint")

            column(name: "graph_colors_idx", type: "varchar(255)")

            column(name: "graph_colors_elt", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1460099558388-3") {
        createTable(tableName: "userspecific_dashboard_base_graph_name_aliases") {
            column(name: "graph_name_aliases", type: "bigint")

            column(name: "graph_name_aliases_idx", type: "varchar(255)")

            column(name: "graph_name_aliases_elt", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

//      ### DATA MIGRATION ###
    changeSet(author: "mmi", id: "1460040092000-1") {
        sql('''
            insert into userspecific_dashboard_base (version, aggr_group, dashboard_name, debug, from_date, from_hour, include_interval, overwrite_warning_about_long_processing_time, publicly_visible, selected_all_browsers, selected_all_locations, selected_all_measured_events, selected_browsers, selected_folder, selected_locations, selected_measured_event_ids, selected_pages, selected_time_frame_interval, set_from_hour, set_to_hour, to_date, to_hour, username, wide_screen_diagram_montage, chart_height, chart_title, chart_width, load_time_maximum, load_time_minimum, show_data_labels, show_data_markers, csi_type_doc_complete, csi_type_visually_complete, selected_csi_systems, class)
                        select version, aggr_group, dashboard_name, debug, from_date, from_hour, include_interval, overwrite_warning_about_long_processing_time, publicly_visible, selected_all_browsers, selected_all_locations, selected_all_measured_events, selected_browsers, selected_folder, selected_locations, selected_measured_event_ids, selected_pages, selected_time_frame_interval, set_from_hour, set_to_hour, to_date, to_hour, username, wide_screen_diagram_montage, chart_height, chart_title, chart_width, load_time_maximum, load_time_minimum, show_data_labels, show_data_markers, csi_type_doc_complete, csi_type_visually_complete, selected_csi_systems, 'de.iteratec.osm.report.UserspecificCsiDashboard'
                        from userspecific_csi_dashboard;
        ''')
    }

    changeSet(author: "mmi", id: "1460040092000-2") {
        sql('''
            insert into userspecific_dashboard_base (version, custom_connectivity_name, dashboard_name, debug, from_date, from_hour, include_custom_connectivity, include_native_connectivity, overwrite_warning_about_long_processing_time, publicly_visible, selected_aggr_group_values_cached, selected_aggr_group_values_un_cached, selected_all_browsers, selected_all_connectivity_profiles, selected_all_locations, selected_all_measured_events, selected_browsers, selected_connectivity_profiles, selected_folder, selected_interval, selected_locations, selected_measured_event_ids, selected_pages, selected_time_frame_interval, set_from_hour, set_to_hour, to_date, to_hour, trim_above_load_times, trim_above_request_counts, trim_above_request_sizes, trim_below_load_times, trim_below_request_counts, trim_below_request_sizes, username, wide_screen_diagram_montage, chart_title, chart_width, chart_height, load_time_maximum, load_time_minimum, show_data_labels, show_data_markers, class)
                select version, custom_connectivity_name, dashboard_name, debug, from_date, from_hour, include_custom_connectivity, include_native_connectivity, overwrite_warning_about_long_processing_time, publicly_visible, selected_aggr_group_values_cached, selected_aggr_group_values_un_cached, selected_all_browsers, selected_all_connectivity_profiles, selected_all_locations, selected_all_measured_events, selected_browsers, selected_connectivity_profiles, selected_folder, selected_interval, selected_locations, selected_measured_event_ids, selected_pages, selected_time_frame_interval, set_from_hour, set_to_hour, to_date, to_hour, trim_above_load_times, trim_above_request_counts, trim_above_request_sizes, trim_below_load_times, trim_below_request_counts, trim_below_request_sizes, username, wide_screen_diagram_montage, chart_title, chart_width, chart_height, load_time_maximum, load_time_minimum, show_data_labels, show_data_markers, 'de.iteratec.osm.report.UserspecificEventResultDashboard'
                from userspecific_event_result_dashboard;
        ''')
    }

    changeSet(author: "bwo", id: "1460040092001-1"){
        /**
         * In the past we had grailschanges using GORM. There was a possible bug,
         * which prevents us from using GORM in grailschanges with java 8.
         * We had to delete the old entries and rewrite this changes.
         * Because there are instances which already ran the old changelog,
         * we first check if the changelog with the given id is already in the database. If this is not
         * the case we can safely execute the rewritten changelog
         **/
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "select count(*) from DATABASECHANGELOG where id = '1460040092000-3'")
        }
        grailsChange{
            change{
                def rows = sql.rows("SELECT * FROM userspecific_csi_dashboard")
                rows.each{
                    long oldId = it.id
                    long newId = sql.firstRow("SELECT id FROM userspecific_dashboard_base WHERE dashboard_name = :name " +
                                              "AND class = 'de.iteratec.osm.report.UserspecificCsiDashboard'",
                                              [name:it.dashboard_name]).id
                    def entries = sql.rows("select * from userspecific_csi_dashboard_graph_colors " +
                                           "where graph_colors = :oldID",[oldId:oldId])
                    if(entries.size() > 0){
                        sql.execute('insert into userspecific_dashboard_base_graph_colors ' +
                                '(graph_colors, graph_colors_idx, graph_colors_elt) ' +
                                'select :newID, graph_colors_idx, graph_colors_elt ' +
                                'from userspecific_csi_dashboard_graph_colors ' +
                                'where graph_colors = :oldID', [newID: newId.toString(), oldID: oldId.toString()])
                    }
                    entries = sql.rows("select * from userspecific_csi_dashboard_graph_name_aliases " +
                                       "where graph_name_aliases = :oldID",[oldID:oldId.toString()])
                    if(entries.size() > 0){
                        sql.execute('insert into userspecific_dashboard_base_graph_name_aliases ' +
                                '(graph_name_aliases, graph_name_aliases_idx, graph_name_aliases_elt) ' +
                                'select :newID, graph_name_aliases_idx, graph_name_aliases_elt ' +
                                'from userspecific_csi_dashboard_graph_name_aliases ' +
                                'where graph_name_aliases = :oldID',[newID: newId.toString(), oldID:oldId.toString()])
                    }
                }
                rows = sql.rows("SELECT * FROM userspecific_event_result_dashboard")
                rows.each{
                    long oldId = it.id
                    long newId = sql.firstRow("SELECT id FROM userspecific_dashboard_base " +
                                              "WHERE dashboard_name = :name " +
                                              "AND class = 'de.iteratec.osm.report.UserspecificEventResultDashboard'",
                                              [name:it.dashboard_name]).id
                    def entries = sql.rows("select * from userspecific_event_result_dashboard_graph_colors " +
                                           "where graph_colors = :oldID", [oldID: oldId.toString()])
                    if(entries.size() > 0){
                        sql.execute('insert into userspecific_dashboard_base_graph_colors ' +
                                    '(graph_colors, graph_colors_idx, graph_colors_elt) ' +
                                    'select :newID, graph_colors_idx, graph_colors_elt ' +
                                    'from userspecific_event_result_dashboard_graph_colors ' +
                                    'where graph_colors = :oldID', [newID: newId.toString(), oldID: oldId.toString()])
                    }

                    entries = sql.rows("select * from userspecific_event_result_dashboard_graph_name_aliases " +
                                       "where graph_name_aliases = :oldID", [oldID: oldId.toString()])
                    if(entries.size() > 0){
                        sql.execute('insert into userspecific_dashboard_base_graph_name_aliases ' +
                                '(graph_name_aliases, graph_name_aliases_idx, graph_name_aliases_elt) ' +
                                'select :newID, graph_name_aliases_idx, graph_name_aliases_elt ' +
                                'from userspecific_event_result_dashboard_graph_name_aliases ' +
                                'where graph_name_aliases = :oldID', [newID: newId.toString(), oldID:oldId.toString()])
                    }
                }
             }
        }
    }
    changeSet(author: "marcus (generated)", id: "1460040092001-2") {
        //All changes of id 1460040092001-X rely on 1460040092001-1.
        //So we had to make sure that this changes will be executed only, if the previous changeset was executed
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "select count(*) from DATABASECHANGELOG where id = '1460099558388-9'")
        }
        dropTable(tableName: "userspecific_csi_dashboard")
    }

    changeSet(author: "marcus (generated)", id: "1460040092001-3") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "select count(*) from DATABASECHANGELOG where id = '1460099558388-10'")
        }
        dropTable(tableName: "userspecific_csi_dashboard_graph_colors")
    }

    changeSet(author: "marcus (generated)", id: "1460040092001-4") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "select count(*) from DATABASECHANGELOG where id = '1460099558388-11'")
        }
        dropTable(tableName: "userspecific_csi_dashboard_graph_name_aliases")
    }

    changeSet(author: "marcus (generated)", id: "1460040092001-5") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "select count(*) from DATABASECHANGELOG where id = '1460099558388-12'")
        }
        dropTable(tableName: "userspecific_event_result_dashboard")
    }

    changeSet(author: "marcus (generated)", id: "1460040092001-6") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "select count(*) from DATABASECHANGELOG where id = '1460099558388-13'")
        }
        dropTable(tableName: "userspecific_event_result_dashboard_graph_colors")
    }

    changeSet(author: "marcus (generated)", id: "1460040092001-7") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "select count(*) from DATABASECHANGELOG where id = '1460099558388-14'")
        }
        dropTable(tableName: "userspecific_event_result_dashboard_graph_name_aliases")
    }

//      ### END UserspecificDashboard refactoring

//	    ### BEGIN BatchActivity refactoring
	changeSet(author: "bwo (generated)", id: "1463554586230-1") {
		addColumn(tableName: "batch_activity") {
			column(name: "actual_stage", type: "integer")
		}
	}

	changeSet(author: "bwo (generated)", id: "1463554586230-2") {
		renameColumn(tableName: "batch_activity", oldColumnName: "last_updated", newColumnName: "last_update", columnDataType: "datetime")
	}

	changeSet(author: "bwo (generated)", id: "1463554586230-3") {
		addColumn(tableName: "batch_activity") {
			column(name: "maximum_stages", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1463554586230-4") {
		addColumn(tableName: "batch_activity") {
			column(name: "maximum_steps_in_stage", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1463554586230-5") {
		addColumn(tableName: "batch_activity") {
			column(name: "stage_description", type: "varchar(255)")
		}
	}

	changeSet(author: "bwo (generated)", id: "1463554586230-6") {
		addColumn(tableName: "batch_activity") {
			column(name: "step_in_stage", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1463554586230-8") {
		dropColumn(columnName: "id_within_domain", tableName: "batch_activity")
	}

	changeSet(author: "bwo (generated)", id: "1463554586230-9") {
		dropColumn(columnName: "progress", tableName: "batch_activity")
	}

	changeSet(author: "bwo (generated)", id: "1463554586230-10") {
		dropColumn(columnName: "progress_within_stage", tableName: "batch_activity")
	}

	changeSet(author: "bwo (generated)", id: "1463554586230-11") {
		dropColumn(columnName: "stage", tableName: "batch_activity")
	}

	changeSet(author: "bwo (generated)", id: "1463554586230-12") {
		dropColumn(columnName: "successful_actions", tableName: "batch_activity")
	}

	changeSet(author: "bwo (generated)", id: "1463554586230-13") {
		sql('''
				UPDATE batch_activity set actual_stage = 1, maximum_stages = 1,
				step_in_stage= 1, maximum_steps_in_stage = 1
				WHERE actual_stage is null AND status = 'DONE';
			''')
	}

	changeSet(author: "bwo (generated)", id: "1463554586230-14") {
		sql('''
				UPDATE batch_activity set actual_stage = 0, maximum_stages = 0,
				step_in_stage= 0, maximum_steps_in_stage = 0
				WHERE actual_stage is null;
			''')
	}

	changeSet(author: "bwo (generated)", id: "1464157353626-1") {
		addColumn(tableName: "osm_configuration") {
			column(name: "max_batch_activity_storage_time_in_days", type: "integer", defaultValue: 30) {
				constraints(nullable: "false")
			}
		}
	}

//	  ### END BatchActivity Refactoring
}
