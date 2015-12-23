databaseChangeLog = {

	changeSet(author: "bwo (generated)", id: "1450880600666-1") {
		addColumn(tableName: "csi_configuration") {
			column(name: "day_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1450880600666-2") {
		dropForeignKeyConstraint(baseTableName: "csi_configuration_hour_of_day", baseTableSchemaName: "osm", constraintName: "FKD71CD360A33A543A")
	}

	changeSet(author: "bwo (generated)", id: "1450880600666-4") {
		createIndex(indexName: "FKA5B76130EF4854C8", tableName: "csi_configuration") {
			column(name: "day_id")
		}
	}

	changeSet(author: "bwo (generated)", id: "1450880600666-5") {
		dropTable(tableName: "csi_configuration_hour_of_day")
	}

	changeSet(author: "bwo (generated)", id: "1450880600666-3") {
		addForeignKeyConstraint(baseColumnNames: "day_id", baseTableName: "csi_configuration", constraintName: "FKA5B76130EF4854C8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "day", referencesUniqueColumn: "false")
	}
}
