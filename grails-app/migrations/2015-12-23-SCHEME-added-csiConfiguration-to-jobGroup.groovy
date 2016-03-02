databaseChangeLog = {

	changeSet(author: "marcus (generated)", id: "1450877332479-1") {
		addColumn(tableName: "job_group") {
			column(name: "csi_configuration_id", type: "bigint")
		}
	}

	changeSet(author: "marcus (generated)", id: "1450877332479-9") {
		createIndex(indexName: "FK5718737D302E2039", tableName: "job_group") {
			column(name: "csi_configuration_id")
		}
	}

	changeSet(author: "marcus (generated)", id: "1450877332479-8") {
		addForeignKeyConstraint(baseColumnNames: "csi_configuration_id", baseTableName: "job_group", constraintName: "FK5718737D302E2039", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "csi_configuration", referencesUniqueColumn: "false")
	}
}
