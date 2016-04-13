import de.iteratec.osm.report.UserspecificCsiDashboard
import de.iteratec.osm.report.UserspecificDashboardBase
import de.iteratec.osm.report.UserspecificEventResultDashboard

databaseChangeLog = {

    changeSet(author: "mmi (generated)", id: "1460032938971-1") {
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

    changeSet(author: "mmi (generated)", id: "1460032938971-2") {
        createTable(tableName: "userspecific_dashboard_base_graph_colors") {
            column(name: "graph_colors", type: "bigint")

            column(name: "graph_colors_idx", type: "varchar(255)")

            column(name: "graph_colors_elt", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mmi (generated)", id: "1460032938971-3") {
        createTable(tableName: "userspecific_dashboard_base_graph_name_aliases") {
            column(name: "graph_name_aliases", type: "bigint")

            column(name: "graph_name_aliases_idx", type: "varchar(255)")

            column(name: "graph_name_aliases_elt", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mmi (generated)", id: "1460032938971-5") {
        dropIndex(indexName: "dashboard_name", tableName: "userspecific_csi_dashboard")
    }

    changeSet(author: "mmi (generated)", id: "1460032938971-6") {
        dropIndex(indexName: "dashboard_name", tableName: "userspecific_event_result_dashboard")
    }

//	### DATA MIGRATION ###
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

    changeSet(author: "mmi", id: "1460040092000-3") {
        grailsChange {
            change {
                println "starting data mirgration for dependent tables"
                UserspecificCsiDashboard.withNewSession { session ->
                    UserspecificCsiDashboard.list().each {
                        println "migrating csi dashboard: " + it.dashboardName
                        def query = session.createSQLQuery("select id from userspecific_csi_dashboard where dashboard_name = :name")
                        query.setString("name", it.dashboardName)
                        def oldId = query.list()[0]


                        query = session.createSQLQuery('insert into userspecific_dashboard_base_graph_colors (graph_colors, graph_colors_idx, graph_colors_elt) ' +
                                'select :newID, graph_colors_idx, graph_colors_elt from userspecific_csi_dashboard_graph_colors ' +
                                'where graph_colors = :oldID')
                        query.setString("newID", it.id.toString())
                        query.setString("oldID", oldId.toString())
                        query.executeUpdate()

                        query = session.createSQLQuery('insert into userspecific_dashboard_base_graph_name_aliases (graph_name_aliases, graph_name_aliases_idx, graph_name_aliases_elt) ' +
                                'select :newID, graph_name_aliases_idx, graph_name_aliases_elt from userspecific_csi_dashboard_graph_name_aliases ' +
                                'where graph_name_aliases = :oldID')
                        query.setString("newID", it.id.toString())
                        query.setString("oldID", oldId.toString())
                        query.executeUpdate()
                    }
                }

                UserspecificEventResultDashboard.withNewSession {session ->
                    UserspecificEventResultDashboard.list().each {
                        println "migrating event result dashboard: " + it.dashboardName
                        def query = session.createSQLQuery("select id from userspecific_csi_dashboard where dashboard_name = :name")
                        query.setString("name", it.dashboardName)
                        def oldId = query.list()[0]

                        query = session.createSQLQuery('insert into userspecific_dashboard_base_graph_colors (graph_colors, graph_colors_idx, graph_colors_elt) ' +
                                'select :newID, graph_colors_idx, graph_colors_elt from userspecific_event_result_dashboard_graph_colors ' +
                                'where graph_colors = :oldID')
                        query.setString("newID", it.id.toString())
                        query.setString("oldID", oldId.toString())
                        query.executeUpdate()

                        query = session.createSQLQuery('insert into userspecific_dashboard_base_graph_name_aliases (graph_name_aliases, graph_name_aliases_idx, graph_name_aliases_elt) ' +
                                'select :newID, graph_name_aliases_idx, graph_name_aliases_elt from userspecific_event_result_dashboard_graph_name_aliases ' +
                                'where graph_name_aliases = :oldID')
                        query.setString("newID", it.id.toString())
                        query.setString("oldID", oldId.toString())
                        query.executeUpdate()
                    }
                }
            }
        }
    }

//	changeSet(author: "mmi (generated)", id: "1460032938971-8") {
//		dropTable(tableName: "userspecific_csi_dashboard")
//	}
//
//	changeSet(author: "mmi (generated)", id: "1460032938971-9") {
//		dropTable(tableName: "userspecific_csi_dashboard_graph_colors")
//	}
//
//	changeSet(author: "mmi (generated)", id: "1460032938971-10") {
//		dropTable(tableName: "userspecific_csi_dashboard_graph_name_aliases")
//	}
//
//	changeSet(author: "mmi (generated)", id: "1460032938971-11") {
//		dropTable(tableName: "userspecific_event_result_dashboard")
//	}
//
//	changeSet(author: "mmi (generated)", id: "1460032938971-12") {
//		dropTable(tableName: "userspecific_event_result_dashboard_graph_colors")
//	}
//
//	changeSet(author: "mmi (generated)", id: "1460032938971-13") {
//		dropTable(tableName: "userspecific_event_result_dashboard_graph_name_aliases")
//	}
}
