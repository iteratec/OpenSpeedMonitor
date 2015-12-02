databaseChangeLog = {

	changeSet(author: "bwollmer (generated)", id: "1449057174870-1") {
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

	changeSet(author: "bwollmer (generated)", id: "1449057174870-2") {
		createTable(tableName: "csi_configuration_browser_connectivity_weight") {
			column(name: "csi_configuration_browser_connectivity_weights_id", type: "bigint")

			column(name: "browser_connectivity_weight_id", type: "bigint")

			column(name: "browser_connectivity_weights_idx", type: "integer")
		}
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-3") {
		createTable(tableName: "csi_configuration_hour_of_day") {
			column(name: "csi_configuration_hour_of_days_id", type: "bigint")

			column(name: "hour_of_day_id", type: "bigint")

			column(name: "hour_of_days_idx", type: "integer")
		}
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-4") {
		createTable(tableName: "csi_configuration_page_weight") {
			column(name: "csi_configuration_page_weights_id", type: "bigint")

			column(name: "page_weight_id", type: "bigint")

			column(name: "page_weights_idx", type: "integer")
		}
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-5") {
		createTable(tableName: "csi_configuration_time_to_cs_mapping") {
			column(name: "csi_configuration_time_to_cs_mappings_id", type: "bigint")

			column(name: "time_to_cs_mapping_id", type: "bigint")

			column(name: "time_to_cs_mappings_idx", type: "integer")
		}
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-6") {
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

	changeSet(author: "bwollmer (generated)", id: "1449057174870-7") {
		addColumn(tableName: "measured_value") {
			column(name: "connectivity_profile_id", type: "bigint")
		}
	}
	
	changeSet(author: "bwollmer (generated)", id: "1449057174870-20") {
		createIndex(indexName: "label_uniq_1449057174292", tableName: "csi_configuration", unique: "true") {
			column(name: "label")
		}
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-21") {
		createIndex(indexName: "FKA5D3DA4C6B8916", tableName: "csi_configuration_browser_connectivity_weight") {
			column(name: "browser_connectivity_weight_id")
		}
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-22") {
		createIndex(indexName: "FKD71CD360A33A543A", tableName: "csi_configuration_hour_of_day") {
			column(name: "hour_of_day_id")
		}
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-23") {
		createIndex(indexName: "FKCDAE2599F68F682B", tableName: "csi_configuration_page_weight") {
			column(name: "page_weight_id")
		}
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-24") {
		createIndex(indexName: "FK7F5A89C0B27DA4D", tableName: "csi_configuration_time_to_cs_mapping") {
			column(name: "time_to_cs_mapping_id")
		}
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-25") {
		createIndex(indexName: "FKCC54EB863699C23", tableName: "measured_value") {
			column(name: "connectivity_profile_id")
		}
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-26") {
		createIndex(indexName: "FKC982AE2873976C8C", tableName: "page_weight") {
			column(name: "page_id")
		}
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-14") {
		addForeignKeyConstraint(baseColumnNames: "browser_connectivity_weight_id", baseTableName: "csi_configuration_browser_connectivity_weight", constraintName: "FKA5D3DA4C6B8916", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "browser_connectivity_weight", referencesUniqueColumn: "false")
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-15") {
		addForeignKeyConstraint(baseColumnNames: "hour_of_day_id", baseTableName: "csi_configuration_hour_of_day", constraintName: "FKD71CD360A33A543A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "hour_of_day", referencesUniqueColumn: "false")
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-16") {
		addForeignKeyConstraint(baseColumnNames: "page_weight_id", baseTableName: "csi_configuration_page_weight", constraintName: "FKCDAE2599F68F682B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page_weight", referencesUniqueColumn: "false")
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-17") {
		addForeignKeyConstraint(baseColumnNames: "time_to_cs_mapping_id", baseTableName: "csi_configuration_time_to_cs_mapping", constraintName: "FK7F5A89C0B27DA4D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "time_to_cs_mapping", referencesUniqueColumn: "false")
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-18") {
		addForeignKeyConstraint(baseColumnNames: "connectivity_profile_id", baseTableName: "measured_value", constraintName: "FKCC54EB863699C23", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "connectivity_profile", referencesUniqueColumn: "false")
	}

	changeSet(author: "bwollmer (generated)", id: "1449057174870-19") {
		addForeignKeyConstraint(baseColumnNames: "page_id", baseTableName: "page_weight", constraintName: "FKC982AE2873976C8C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page", referencesUniqueColumn: "false")
	}
}
