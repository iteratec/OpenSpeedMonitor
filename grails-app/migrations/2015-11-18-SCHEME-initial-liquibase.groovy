databaseChangeLog = {

	changeSet(author: "nkuhn (generated)", id: "1447946152150-1") {
		createTable(tableName: "aggregator_type") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "aggregator_tyPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "measurand_group", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-2") {
		createTable(tableName: "api_key") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "api_keyPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "allowed_for_create_event", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "allowed_for_job_activation", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "allowed_for_job_deactivation", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "allowed_for_job_set_execution_schedule", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "allowed_for_measurement_activation", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar(255)")

			column(name: "secret_key", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(defaultValue: "true", name: "valid", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-3") {
		createTable(tableName: "batch_activity") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "batch_activitPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "activity", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "domain", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "end_date", type: "datetime")

			column(name: "failures", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "id_within_domain", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "last_failure_message", type: "varchar(255)")

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "progress", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "progress_within_stage", type: "varchar(255)")

			column(name: "stage", type: "varchar(255)")

			column(name: "start_date", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "status", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "successful_actions", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-4") {
		createTable(tableName: "browser") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "browserPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "weight", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-5") {
		createTable(tableName: "browser_alias") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "browser_aliasPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "alias", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "browser_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-6") {
		createTable(tableName: "browser_connectivity_weight") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "browser_connePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "browser_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "connectivity_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "weight", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-7") {
		createTable(tableName: "connectivity_profile") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "connectivity_PK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(defaultValue: "true", name: "active", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "bandwidth_down", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "bandwidth_up", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "latency", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "packet_loss", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-8") {
		createTable(tableName: "cs_target_graph") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "cs_target_graPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "default_visibility", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "longtext")

			column(name: "label", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "point_one_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "point_two_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-9") {
		createTable(tableName: "cs_target_value") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "cs_target_valPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "cs_in_percent", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "date", type: "datetime") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-10") {
		createTable(tableName: "customer_frustration") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "customer_frusPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "investigation_version", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "load_time_in_milli_secs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "page_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-11") {
		createTable(tableName: "default_time_to_cs_mapping") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "default_time_PK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "customer_satisfaction_in_percent", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "load_time_in_milli_secs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-12") {
		createTable(tableName: "event") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "eventPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "longtext")

			column(name: "event_date", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "globally_visible", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "short_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-13") {
		createTable(tableName: "event_job_group") {
			column(name: "event_job_groups_id", type: "bigint")

			column(name: "job_group_id", type: "bigint")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-14") {
		createTable(tableName: "event_result") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "event_resultPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "cached_view", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "connectivity_profile_id", type: "bigint")

			column(name: "custom_connectivity_name", type: "varchar(255)")

			column(name: "customer_satisfaction_in_percent", type: "double precision")

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "doc_complete_incoming_bytes", type: "integer")

			column(name: "doc_complete_requests", type: "integer")

			column(name: "doc_complete_time_in_millisecs", type: "integer")

			column(name: "dom_time_in_millisecs", type: "integer")

			column(name: "download_attempts", type: "integer")

			column(name: "first_byte_in_millisecs", type: "integer")

			column(name: "first_status_update", type: "datetime")

			column(name: "fully_loaded_incoming_bytes", type: "integer")

			column(name: "fully_loaded_request_count", type: "integer")

			column(name: "fully_loaded_time_in_millisecs", type: "integer")

			column(name: "job_result_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "job_result_date", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "job_result_job_config_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "last_status_update", type: "datetime")

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "load_time_in_millisecs", type: "integer")

			column(name: "measured_event_id", type: "bigint")

			column(name: "median_value", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "no_traffic_shaping_at_all", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "number_of_wpt_run", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "speed_index", type: "integer")

			column(name: "start_render_in_millisecs", type: "integer")

			column(name: "tag", type: "varchar(255)")

			column(name: "test_agent", type: "varchar(255)")

			column(name: "test_details_waterfallurl", type: "varchar(255)")

			column(name: "validation_state", type: "varchar(255)")

			column(name: "visually_complete_in_millisecs", type: "integer")

			column(name: "web_performance_waterfall_id", type: "bigint")

			column(name: "wpt_status", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-15") {
		createTable(tableName: "graphite_event_source_path") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "graphite_evenPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "static_prefix", type: "varchar(255)")

			column(name: "target_metric_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-16") {
		createTable(tableName: "graphite_event_source_path_job_group") {
			column(name: "graphite_event_source_path_job_groups_id", type: "bigint")

			column(name: "job_group_id", type: "bigint")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-17") {
		createTable(tableName: "graphite_path") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "graphite_pathPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "measurand_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "prefix", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-18") {
		createTable(tableName: "graphite_server") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "graphite_servPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "port", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "server_adress", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "webapp_path_to_rendering_engine", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "webapp_protocol", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "webapp_url", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-19") {
		createTable(tableName: "graphite_server_graphite_event_source_path") {
			column(name: "graphite_server_graphite_event_source_paths_id", type: "bigint")

			column(name: "graphite_event_source_path_id", type: "bigint")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-20") {
		createTable(tableName: "graphite_server_graphite_path") {
			column(name: "graphite_server_graphite_paths_id", type: "bigint")

			column(name: "graphite_path_id", type: "bigint")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-21") {
		createTable(tableName: "hour_of_day") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "hour_of_dayPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "full_hour", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "weight", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-22") {
		createTable(tableName: "http_archive") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "http_archivePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "har_data", type: "blob")

			column(name: "job_result_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-23") {
		createTable(tableName: "job") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "jobPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "active", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "auth_password", type: "varchar(255)")

			column(name: "auth_username", type: "varchar(255)")

			column(name: "bandwidth_down", type: "integer")

			column(name: "bandwidth_up", type: "integer")

			column(name: "bodies", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "capture_video", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "clearcerts", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "connectivity_profile_id", type: "bigint")

			column(name: "continuous_video", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "custom_connectivity_name", type: "varchar(255)")

			column(name: "custom_connectivity_profile", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "event_name_if_unknown", type: "varchar(255)")

			column(name: "execution_schedule", type: "varchar(255)")

			column(name: "first_view_only", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "frequency_in_min", type: "integer")

			column(name: "ignoressl", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "job_group_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "keepua", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "label", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "last_run", type: "datetime")

			column(name: "latency", type: "integer")

			column(name: "location_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "max_download_time_in_minutes", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "no_traffic_shaping_at_all", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "noscript", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "packet_loss", type: "integer")

			column(defaultValue: "1", name: "persist_non_median_results", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "provide_authenticate_information", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "runs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "script_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "standards", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "tcpdump", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "web10", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-24") {
		createTable(tableName: "job_group") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "job_groupPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "group_type", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-25") {
		createTable(tableName: "job_group_graphite_server") {
			column(name: "job_group_graphite_servers_id", type: "bigint")

			column(name: "graphite_server_id", type: "bigint")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-26") {
		createTable(tableName: "job_result") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "job_resultPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "apply_validate_rule", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "auth_password", type: "varchar(255)")

			column(name: "auth_username", type: "varchar(255)")

			column(name: "capture_video", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "date", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "download_details", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "download_result_xml", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "event_result_id_from_sqlite", type: "bigint")

			column(name: "first_view_only", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "frequency_in_min", type: "integer")

			column(name: "http_status_code", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "job_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "job_config_label", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "job_config_runs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "job_group_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "location_browser", type: "varchar(255)")

			column(name: "location_label", type: "varchar(255)")

			column(name: "location_location", type: "varchar(255)")

			column(name: "location_unique_identifier_for_server", type: "varchar(255)")

			column(name: "max_download_attempts", type: "integer")

			column(name: "multistep", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "provide_authenticate_information", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "script_navigationscript", type: "longtext")

			column(name: "script_url", type: "longtext")

			column(name: "test_agent", type: "varchar(255)")

			column(name: "test_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "validation_markas", type: "integer")

			column(name: "validation_markaselse", type: "integer")

			column(name: "validation_request", type: "varchar(255)")

			column(name: "validation_type", type: "integer")

			column(name: "wpt_server_baseurl", type: "varchar(255)")

			column(name: "wpt_server_label", type: "varchar(255)")

			column(name: "wpt_status", type: "longtext")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-27") {
		createTable(tableName: "job_set") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "job_setPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-28") {
		createTable(tableName: "job_set_job") {
			column(name: "job_set_jobs_id", type: "bigint")

			column(name: "job_id", type: "bigint")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-29") {
		createTable(tableName: "job_variables") {
			column(name: "variables", type: "bigint")

			column(name: "variables_idx", type: "varchar(255)")

			column(name: "variables_elt", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-30") {
		createTable(tableName: "location") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "locationPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "active", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "activeagents", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "browser_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "label", type: "varchar(150)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "location", type: "varchar(100)") {
				constraints(nullable: "false")
			}

			column(name: "queuethreshold", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "queuethresholdgreenlimit", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "queuethresholdredlimit", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "queuethresholdyellowlimit", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "unique_identifier_for_server", type: "varchar(255)")

			column(name: "valid", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "wpt_server_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-31") {
		createTable(tableName: "measured_event") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "measured_evenPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "tested_page_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-32") {
		createTable(tableName: "measured_value") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "measured_valuPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "aggregator_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "closed_and_calculated", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "interval_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "result_ids", type: "longtext") {
				constraints(nullable: "false")
			}

			column(name: "started", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "tag", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "double precision")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-33") {
		createTable(tableName: "measured_value_interval") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "measured_valuPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "interval_in_minutes", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-34") {
		createTable(tableName: "measured_value_update_event") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "measured_valuPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_of_update", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "measured_value_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "update_cause", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-35") {
		createTable(tableName: "osm_configuration") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "osm_configuraPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "csi_transformation", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(defaultValue: "60", name: "default_max_download_time_in_minutes", type: "integer") {
				constraints(nullable: "false")
			}

			column(defaultValue: "2", name: "detail_data_storage_time_in_weeks", type: "integer") {
				constraints(nullable: "false")
			}

			column(defaultValue: "400", name: "initial_chart_height_in_pixels", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "main_url_under_test", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(defaultValue: "13", name: "max_data_storage_time_in_months", type: "integer") {
				constraints(nullable: "false")
			}

			column(defaultValue: "180000", name: "max_doc_complete_time_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(defaultValue: "250", name: "min_doc_complete_time_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-36") {
		createTable(tableName: "page") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "pagePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "weight", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-37") {
		createTable(tableName: "role") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "rolePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "authority", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-38") {
		createTable(tableName: "script") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "scriptPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "label", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "measured_events_count", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "navigation_script", type: "longtext") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-39") {
		createTable(tableName: "script_page") {
			column(name: "script_tested_pages_id", type: "bigint")

			column(name: "page_id", type: "bigint")

			column(name: "tested_pages_idx", type: "integer")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-40") {
		createTable(tableName: "tag_links") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tag_linksPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tag_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tag_ref", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-41") {
		createTable(tableName: "tags") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tagsPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-42") {
		createTable(tableName: "time_to_cs_mapping") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "time_to_cs_maPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "customer_satisfaction", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "load_time_in_milli_secs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "mapping_version", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "page_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-43") {
		createTable(tableName: "user") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "userPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "account_expired", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "account_locked", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "enabled", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "password", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "password_expired", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "username", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-44") {
		createTable(tableName: "user_role") {
			column(name: "role_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-45") {
		createTable(tableName: "userspecific_csi_dashboard") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "userspecific_PK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "aggr_group", type: "varchar(255)")

			column(name: "dashboard_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "debug", type: "bit")

			column(name: "from_date", type: "datetime")

			column(name: "from_hour", type: "varchar(255)")

			column(name: "include_interval", type: "bit")

			column(name: "overwrite_warning_about_long_processing_time", type: "bit")

			column(name: "publicly_visible", type: "bit")

			column(name: "selected_aggr_group_values_cached", type: "varchar(255)")

			column(name: "selected_aggr_group_values_un_cached", type: "varchar(255)")

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

			column(name: "to_date", type: "datetime")

			column(name: "to_hour", type: "varchar(255)")

			column(name: "username", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "wide_screen_diagram_montage", type: "bit")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-46") {
		createTable(tableName: "userspecific_event_result_dashboard") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "userspecific_PK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "custom_connectivity_name", type: "varchar(255)")

			column(name: "dashboard_name", type: "varchar(255)")

			column(name: "debug", type: "bit")

			column(name: "from_date", type: "datetime")

			column(name: "from_hour", type: "varchar(255)")

			column(name: "include_custom_connectivity", type: "bit")

			column(name: "include_native_connectivity", type: "bit")

			column(name: "overwrite_warning_about_long_processing_time", type: "bit")

			column(name: "publicly_visible", type: "bit")

			column(name: "selected_aggr_group_values_cached", type: "varchar(255)")

			column(name: "selected_aggr_group_values_un_cached", type: "varchar(255)")

			column(name: "selected_all_browsers", type: "bit")

			column(name: "selected_all_connectivity_profiles", type: "bit")

			column(name: "selected_all_locations", type: "bit")

			column(name: "selected_all_measured_events", type: "bit")

			column(name: "selected_browsers", type: "varchar(255)")

			column(name: "selected_connectivity_profiles", type: "varchar(255)")

			column(name: "selected_folder", type: "varchar(255)")

			column(name: "selected_interval", type: "integer")

			column(name: "selected_locations", type: "varchar(255)")

			column(name: "selected_measured_event_ids", type: "varchar(255)")

			column(name: "selected_pages", type: "varchar(255)")

			column(name: "selected_time_frame_interval", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "set_from_hour", type: "bit")

			column(name: "set_to_hour", type: "bit")

			column(name: "to_date", type: "datetime")

			column(name: "to_hour", type: "varchar(255)")

			column(name: "trim_above_load_times", type: "integer")

			column(name: "trim_above_request_counts", type: "integer")

			column(name: "trim_above_request_sizes", type: "integer")

			column(name: "trim_below_load_times", type: "integer")

			column(name: "trim_below_request_counts", type: "integer")

			column(name: "trim_below_request_sizes", type: "integer")

			column(name: "username", type: "varchar(255)")

			column(name: "wide_screen_diagram_montage", type: "bit")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-47") {
		createTable(tableName: "waterfall_entry") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "waterfall_entPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "blocked", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "dns_lookup_time_end_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "dns_lookup_time_start_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "download_time_end_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "download_time_start_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "downloaded_bytes", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "host", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "http_status", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "initial_connect_time_end_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "initial_connect_time_start_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "mime_type", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "one_based_index_in_waterfall", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "path", type: "longtext") {
				constraints(nullable: "false")
			}

			column(name: "ssl_negotation_time_end_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "ssl_negotation_time_start_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "start_offset", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "time_to_first_byte_end_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "time_to_first_byte_start_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "uploaded_bytes", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-48") {
		createTable(tableName: "web_page_test_server") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "web_page_testPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "active", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "base_url", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "contact_person_email", type: "varchar(255)")

			column(name: "contact_person_name", type: "varchar(200)")

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar(255)")

			column(name: "label", type: "varchar(150)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "proxy_identifier", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-49") {
		createTable(tableName: "web_performance_waterfall") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "web_performanPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "cached_view", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "doc_complete_time_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "dom_time_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "event_name", type: "varchar(255)")

			column(name: "fully_loaded_time_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "number_of_wpt_run", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "start_date", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "start_render_in_millisecs", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "title", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "url", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-50") {
		createTable(tableName: "web_performance_waterfall_waterfall_entry") {
			column(name: "web_performance_waterfall_waterfall_entries_id", type: "bigint")

			column(name: "waterfall_entry_id", type: "bigint")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-51") {
		addPrimaryKey(columnNames: "role_id, user_id", constraintName: "user_rolePK", tableName: "user_role")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-93") {
		createIndex(indexName: "name_uniq_1447946152016", tableName: "aggregator_type", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-94") {
		createIndex(indexName: "name_uniq_1447946152025", tableName: "browser", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-95") {
		createIndex(indexName: "FKA552267921FF13B0", tableName: "browser_alias") {
			column(name: "browser_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-96") {
		createIndex(indexName: "alias_uniq_1447946152026", tableName: "browser_alias", unique: "true") {
			column(name: "alias")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-97") {
		createIndex(indexName: "FK214D5E6921FF13B0", tableName: "browser_connectivity_weight") {
			column(name: "browser_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-98") {
		createIndex(indexName: "FK214D5E6948295CD", tableName: "browser_connectivity_weight") {
			column(name: "connectivity_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-99") {
		createIndex(indexName: "FKACA5788F81E39E01", tableName: "cs_target_graph") {
			column(name: "point_one_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-100") {
		createIndex(indexName: "FKACA5788F8AEF385B", tableName: "cs_target_graph") {
			column(name: "point_two_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-101") {
		createIndex(indexName: "FKF51108AC73976C8C", tableName: "customer_frustration") {
			column(name: "page_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-102") {
		createIndex(indexName: "FK6DCCE81848E56BA7", tableName: "event_job_group") {
			column(name: "job_group_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-103") {
		createIndex(indexName: "FK6DCCE818826DED2C", tableName: "event_job_group") {
			column(name: "event_job_groups_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-104") {
		createIndex(indexName: "FK3CA811623AA7BDEF", tableName: "event_result") {
			column(name: "web_performance_waterfall_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-105") {
		createIndex(indexName: "FK3CA8116263699C23", tableName: "event_result") {
			column(name: "connectivity_profile_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-106") {
		createIndex(indexName: "FK3CA81162CD0C7439", tableName: "event_result") {
			column(name: "measured_event_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-107") {
		createIndex(indexName: "FK3CA81162F0C41D41", tableName: "event_result") {
			column(name: "job_result_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-108") {
		createIndex(indexName: "GetLimitedMedianEventResultsBy", tableName: "event_result") {
			column(name: "cached_view")

			column(name: "connectivity_profile_id")

			column(name: "job_result_date")

			column(name: "median_value")

			column(name: "tag")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-109") {
		createIndex(indexName: "jobResultDate_and_jobResultJobConfigId_idx", tableName: "event_result") {
			column(name: "job_result_job_config_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-110") {
		createIndex(indexName: "wJRD_and_wJRJCId_and_mV_and_cV_idx", tableName: "event_result") {
			column(name: "job_result_job_config_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-111") {
		createIndex(indexName: "FK6A94D76F1B04519E", tableName: "graphite_event_source_path_job_group") {
			column(name: "graphite_event_source_path_job_groups_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-112") {
		createIndex(indexName: "FK6A94D76F48E56BA7", tableName: "graphite_event_source_path_job_group") {
			column(name: "job_group_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-113") {
		createIndex(indexName: "FK2FE067F8103BD51A", tableName: "graphite_path") {
			column(name: "measurand_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-114") {
		createIndex(indexName: "unique_server_adress", tableName: "graphite_server", unique: "true") {
			column(name: "port")

			column(name: "server_adress")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-115") {
		createIndex(indexName: "FK66653A3AC5F66B1", tableName: "graphite_server_graphite_event_source_path") {
			column(name: "graphite_event_source_path_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-116") {
		createIndex(indexName: "FK66653A3AE190E8F6", tableName: "graphite_server_graphite_event_source_path") {
			column(name: "graphite_server_graphite_event_source_paths_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-117") {
		createIndex(indexName: "FKBDE6F5CF259A1B79", tableName: "graphite_server_graphite_path") {
			column(name: "graphite_path_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-118") {
		createIndex(indexName: "FKBDE6F5CFE3F0D20B", tableName: "graphite_server_graphite_path") {
			column(name: "graphite_server_graphite_paths_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-119") {
		createIndex(indexName: "full_hour_uniq_1447946152077", tableName: "hour_of_day", unique: "true") {
			column(name: "full_hour")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-120") {
		createIndex(indexName: "FK86FC880BF0C41D41", tableName: "http_archive") {
			column(name: "job_result_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-121") {
		createIndex(indexName: "FK19BBD48E56BA7", tableName: "job") {
			column(name: "job_group_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-122") {
		createIndex(indexName: "FK19BBD63699C23", tableName: "job") {
			column(name: "connectivity_profile_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-123") {
		createIndex(indexName: "FK19BBD984F552E", tableName: "job") {
			column(name: "script_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-124") {
		createIndex(indexName: "FK19BBDC7666564", tableName: "job") {
			column(name: "location_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-125") {
		createIndex(indexName: "label_uniq_1447946152079", tableName: "job", unique: "true") {
			column(name: "label")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-126") {
		createIndex(indexName: "name_uniq_1447946152081", tableName: "job_group", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-127") {
		createIndex(indexName: "FK4CA1B9942DF50285", tableName: "job_group_graphite_server") {
			column(name: "job_group_graphite_servers_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-128") {
		createIndex(indexName: "FK4CA1B994379962F9", tableName: "job_group_graphite_server") {
			column(name: "graphite_server_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-129") {
		createIndex(indexName: "FK9E05EADFBD7A3DD2", tableName: "job_result") {
			column(name: "job_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-130") {
		createIndex(indexName: "date_idx", tableName: "job_result") {
			column(name: "date")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-131") {
		createIndex(indexName: "testId_and_jobConfigLabel_idx", tableName: "job_result") {
			column(name: "job_config_label")

			column(name: "test_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-132") {
		createIndex(indexName: "name_uniq_1447946152084", tableName: "job_set", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-133") {
		createIndex(indexName: "FK57A1BBDE53EF1C72", tableName: "job_set_job") {
			column(name: "job_set_jobs_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-134") {
		createIndex(indexName: "FK57A1BBDEBD7A3DD2", tableName: "job_set_job") {
			column(name: "job_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-135") {
		createIndex(indexName: "FK714F9FB521FF13B0", tableName: "location") {
			column(name: "browser_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-136") {
		createIndex(indexName: "FK714F9FB530E63F61", tableName: "location") {
			column(name: "wpt_server_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-137") {
		createIndex(indexName: "unique_unique_identifier_for_server", tableName: "location", unique: "true") {
			column(name: "browser_id")

			column(name: "wpt_server_id")

			column(name: "unique_identifier_for_server")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-138") {
		createIndex(indexName: "FKBDF2FE1AA7B46FE", tableName: "measured_event") {
			column(name: "tested_page_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-139") {
		createIndex(indexName: "name_uniq_1447946152086", tableName: "measured_event", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-140") {
		createIndex(indexName: "FKCC54EB8154D6F18", tableName: "measured_value") {
			column(name: "interval_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-141") {
		createIndex(indexName: "FKCC54EB877896741", tableName: "measured_value") {
			column(name: "aggregator_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-142") {
		createIndex(indexName: "started_and_iVal_and_aggr_and_tag_idx", tableName: "measured_value") {
			column(name: "aggregator_id")

			column(name: "interval_id")

			column(name: "started")

			column(name: "tag")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-143") {
		createIndex(indexName: "name_uniq_1447946152089", tableName: "page", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-144") {
		createIndex(indexName: "authority_uniq_1447946152090", tableName: "role", unique: "true") {
			column(name: "authority")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-145") {
		createIndex(indexName: "label_uniq_1447946152090", tableName: "script", unique: "true") {
			column(name: "label")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-146") {
		createIndex(indexName: "FKED13118373976C8C", tableName: "script_page") {
			column(name: "page_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-147") {
		createIndex(indexName: "FK7C35D6D45A3B441D", tableName: "tag_links") {
			column(name: "tag_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-148") {
		createIndex(indexName: "name_uniq_1447946152091", tableName: "tags", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-149") {
		createIndex(indexName: "FKF3A5125173976C8C", tableName: "time_to_cs_mapping") {
			column(name: "page_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-150") {
		createIndex(indexName: "username_uniq_1447946152092", tableName: "user", unique: "true") {
			column(name: "username")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-151") {
		createIndex(indexName: "FK143BF46A6DDCBBCB", tableName: "user_role") {
			column(name: "user_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-152") {
		createIndex(indexName: "FK143BF46AC8B1F7EB", tableName: "user_role") {
			column(name: "role_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-153") {
		createIndex(indexName: "dashboard_name_uniq_1447946152093", tableName: "userspecific_csi_dashboard", unique: "true") {
			column(name: "dashboard_name")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-154") {
		createIndex(indexName: "dashboard_name_uniq_1447946152096", tableName: "userspecific_event_result_dashboard", unique: "true") {
			column(name: "dashboard_name")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-155") {
		createIndex(indexName: "FK297D11DE57993D8B", tableName: "web_performance_waterfall_waterfall_entry") {
			column(name: "web_performance_waterfall_waterfall_entries_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-156") {
		createIndex(indexName: "FK297D11DE903CE91E", tableName: "web_performance_waterfall_waterfall_entry") {
			column(name: "waterfall_entry_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-52") {
		addForeignKeyConstraint(baseColumnNames: "browser_id", baseTableName: "browser_alias", constraintName: "FKA552267921FF13B0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "browser", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-53") {
		addForeignKeyConstraint(baseColumnNames: "browser_id", baseTableName: "browser_connectivity_weight", constraintName: "FK214D5E6921FF13B0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "browser", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-54") {
		addForeignKeyConstraint(baseColumnNames: "connectivity_id", baseTableName: "browser_connectivity_weight", constraintName: "FK214D5E6948295CD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "connectivity_profile", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-55") {
		addForeignKeyConstraint(baseColumnNames: "point_one_id", baseTableName: "cs_target_graph", constraintName: "FKACA5788F81E39E01", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "cs_target_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-56") {
		addForeignKeyConstraint(baseColumnNames: "point_two_id", baseTableName: "cs_target_graph", constraintName: "FKACA5788F8AEF385B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "cs_target_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-57") {
		addForeignKeyConstraint(baseColumnNames: "page_id", baseTableName: "customer_frustration", constraintName: "FKF51108AC73976C8C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-58") {
		addForeignKeyConstraint(baseColumnNames: "event_job_groups_id", baseTableName: "event_job_group", constraintName: "FK6DCCE818826DED2C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "event", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-59") {
		addForeignKeyConstraint(baseColumnNames: "job_group_id", baseTableName: "event_job_group", constraintName: "FK6DCCE81848E56BA7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-60") {
		addForeignKeyConstraint(baseColumnNames: "connectivity_profile_id", baseTableName: "event_result", constraintName: "FK3CA8116263699C23", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "connectivity_profile", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-61") {
		addForeignKeyConstraint(baseColumnNames: "job_result_id", baseTableName: "event_result", constraintName: "FK3CA81162F0C41D41", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_result", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-62") {
		addForeignKeyConstraint(baseColumnNames: "measured_event_id", baseTableName: "event_result", constraintName: "FK3CA81162CD0C7439", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "measured_event", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-63") {
		addForeignKeyConstraint(baseColumnNames: "web_performance_waterfall_id", baseTableName: "event_result", constraintName: "FK3CA811623AA7BDEF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "web_performance_waterfall", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-64") {
		addForeignKeyConstraint(baseColumnNames: "graphite_event_source_path_job_groups_id", baseTableName: "graphite_event_source_path_job_group", constraintName: "FK6A94D76F1B04519E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_event_source_path", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-65") {
		addForeignKeyConstraint(baseColumnNames: "job_group_id", baseTableName: "graphite_event_source_path_job_group", constraintName: "FK6A94D76F48E56BA7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-66") {
		addForeignKeyConstraint(baseColumnNames: "measurand_id", baseTableName: "graphite_path", constraintName: "FK2FE067F8103BD51A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "aggregator_type", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-67") {
		addForeignKeyConstraint(baseColumnNames: "graphite_event_source_path_id", baseTableName: "graphite_server_graphite_event_source_path", constraintName: "FK66653A3AC5F66B1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_event_source_path", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-68") {
		addForeignKeyConstraint(baseColumnNames: "graphite_server_graphite_event_source_paths_id", baseTableName: "graphite_server_graphite_event_source_path", constraintName: "FK66653A3AE190E8F6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_server", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-69") {
		addForeignKeyConstraint(baseColumnNames: "graphite_path_id", baseTableName: "graphite_server_graphite_path", constraintName: "FKBDE6F5CF259A1B79", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_path", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-70") {
		addForeignKeyConstraint(baseColumnNames: "graphite_server_graphite_paths_id", baseTableName: "graphite_server_graphite_path", constraintName: "FKBDE6F5CFE3F0D20B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_server", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-71") {
		addForeignKeyConstraint(baseColumnNames: "job_result_id", baseTableName: "http_archive", constraintName: "FK86FC880BF0C41D41", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_result", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-72") {
		addForeignKeyConstraint(baseColumnNames: "connectivity_profile_id", baseTableName: "job", constraintName: "FK19BBD63699C23", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "connectivity_profile", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-73") {
		addForeignKeyConstraint(baseColumnNames: "job_group_id", baseTableName: "job", constraintName: "FK19BBD48E56BA7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-74") {
		addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "job", constraintName: "FK19BBDC7666564", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "location", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-75") {
		addForeignKeyConstraint(baseColumnNames: "script_id", baseTableName: "job", constraintName: "FK19BBD984F552E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "script", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-76") {
		addForeignKeyConstraint(baseColumnNames: "graphite_server_id", baseTableName: "job_group_graphite_server", constraintName: "FK4CA1B994379962F9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_server", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-77") {
		addForeignKeyConstraint(baseColumnNames: "job_group_graphite_servers_id", baseTableName: "job_group_graphite_server", constraintName: "FK4CA1B9942DF50285", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-78") {
		addForeignKeyConstraint(baseColumnNames: "job_id", baseTableName: "job_result", constraintName: "FK9E05EADFBD7A3DD2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-79") {
		addForeignKeyConstraint(baseColumnNames: "job_id", baseTableName: "job_set_job", constraintName: "FK57A1BBDEBD7A3DD2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-80") {
		addForeignKeyConstraint(baseColumnNames: "job_set_jobs_id", baseTableName: "job_set_job", constraintName: "FK57A1BBDE53EF1C72", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_set", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-81") {
		addForeignKeyConstraint(baseColumnNames: "browser_id", baseTableName: "location", constraintName: "FK714F9FB521FF13B0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "browser", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-82") {
		addForeignKeyConstraint(baseColumnNames: "wpt_server_id", baseTableName: "location", constraintName: "FK714F9FB530E63F61", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "web_page_test_server", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-83") {
		addForeignKeyConstraint(baseColumnNames: "tested_page_id", baseTableName: "measured_event", constraintName: "FKBDF2FE1AA7B46FE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-84") {
		addForeignKeyConstraint(baseColumnNames: "aggregator_id", baseTableName: "measured_value", constraintName: "FKCC54EB877896741", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "aggregator_type", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-85") {
		addForeignKeyConstraint(baseColumnNames: "interval_id", baseTableName: "measured_value", constraintName: "FKCC54EB8154D6F18", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "measured_value_interval", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-86") {
		addForeignKeyConstraint(baseColumnNames: "page_id", baseTableName: "script_page", constraintName: "FKED13118373976C8C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-87") {
		addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "tag_links", constraintName: "FK7C35D6D45A3B441D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tags", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-88") {
		addForeignKeyConstraint(baseColumnNames: "page_id", baseTableName: "time_to_cs_mapping", constraintName: "FKF3A5125173976C8C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-89") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", constraintName: "FK143BF46AC8B1F7EB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "role", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-90") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", constraintName: "FK143BF46A6DDCBBCB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-91") {
		addForeignKeyConstraint(baseColumnNames: "waterfall_entry_id", baseTableName: "web_performance_waterfall_waterfall_entry", constraintName: "FK297D11DE903CE91E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "waterfall_entry", referencesUniqueColumn: "false")
	}

	changeSet(author: "nkuhn (generated)", id: "1447946152150-92") {
		addForeignKeyConstraint(baseColumnNames: "web_performance_waterfall_waterfall_entries_id", baseTableName: "web_performance_waterfall_waterfall_entry", constraintName: "FK297D11DE57993D8B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "web_performance_waterfall", referencesUniqueColumn: "false")
	}
}
