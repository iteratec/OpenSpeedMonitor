databaseChangeLog = {

	changeSet(author: "bwo (generated)", id: "1449655148021-1") {
		createTable(tableName: "csi_configuration") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "csi_configuraPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "longtext")

			column(name: "label", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-2") {
		createTable(tableName: "csi_configuration_browser_connectivity_weight") {
			column(name: "csi_configuration_browser_connectivity_weights_id", type: "bigint")

			column(name: "browser_connectivity_weight_id", type: "bigint")

			column(name: "browser_connectivity_weights_idx", type: "integer")
		}
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-3") {
		createTable(tableName: "csi_configuration_hour_of_day") {
			column(name: "csi_configuration_hour_of_days_id", type: "bigint")

			column(name: "hour_of_day_id", type: "bigint")

			column(name: "hour_of_days_idx", type: "integer")
		}
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-4") {
		createTable(tableName: "csi_configuration_page_weight") {
			column(name: "csi_configuration_page_weights_id", type: "bigint")

			column(name: "page_weight_id", type: "bigint")

			column(name: "page_weights_idx", type: "integer")
		}
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-5") {
		createTable(tableName: "csi_configuration_time_to_cs_mapping") {
			column(name: "csi_configuration_time_to_cs_mappings_id", type: "bigint")

			column(name: "time_to_cs_mapping_id", type: "bigint")

			column(name: "time_to_cs_mappings_idx", type: "integer")
		}
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-6") {
		createTable(tableName: "page_weight") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "page_weightPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "page_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "weight", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-14") {
		createIndex(indexName: "label_uniq_1449655147548", tableName: "csi_configuration", unique: "true") {
			column(name: "label")
		}
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-15") {
		createIndex(indexName: "FKA5D3DA4C6B8916", tableName: "csi_configuration_browser_connectivity_weight") {
			column(name: "browser_connectivity_weight_id")
		}
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-16") {
		createIndex(indexName: "FKD71CD360A33A543A", tableName: "csi_configuration_hour_of_day") {
			column(name: "hour_of_day_id")
		}
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-17") {
		createIndex(indexName: "FKCDAE2599F68F682B", tableName: "csi_configuration_page_weight") {
			column(name: "page_weight_id")
		}
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-18") {
		createIndex(indexName: "FK7F5A89C0B27DA4D", tableName: "csi_configuration_time_to_cs_mapping") {
			column(name: "time_to_cs_mapping_id")
		}
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-20") {
		createIndex(indexName: "FKC982AE2873976C8C", tableName: "page_weight") {
			column(name: "page_id")
		}
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-8") {
		addForeignKeyConstraint(baseColumnNames: "browser_connectivity_weight_id", baseTableName: "csi_configuration_browser_connectivity_weight", constraintName: "FKA5D3DA4C6B8916", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "browser_connectivity_weight", referencesUniqueColumn: "false")
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-9") {
		addForeignKeyConstraint(baseColumnNames: "hour_of_day_id", baseTableName: "csi_configuration_hour_of_day", constraintName: "FKD71CD360A33A543A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "hour_of_day", referencesUniqueColumn: "false")
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-10") {
		addForeignKeyConstraint(baseColumnNames: "page_weight_id", baseTableName: "csi_configuration_page_weight", constraintName: "FKCDAE2599F68F682B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page_weight", referencesUniqueColumn: "false")
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-11") {
		addForeignKeyConstraint(baseColumnNames: "time_to_cs_mapping_id", baseTableName: "csi_configuration_time_to_cs_mapping", constraintName: "FK7F5A89C0B27DA4D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "time_to_cs_mapping", referencesUniqueColumn: "false")
	}

	changeSet(author: "bwo (generated)", id: "1449655148021-13") {
		addForeignKeyConstraint(baseColumnNames: "page_id", baseTableName: "page_weight", constraintName: "FKC982AE2873976C8C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page", referencesUniqueColumn: "false")
	}
}
