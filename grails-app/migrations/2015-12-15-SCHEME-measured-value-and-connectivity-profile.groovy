databaseChangeLog = {

	changeSet(author: "bwo (generated)", id: "1450175683946-1") {
		addColumn(tableName: "measured_value") {
			column(name: "connectivity_profile_id", type: "bigint")
		}
	}

	changeSet(author: "bwo (generated)", id: "1450175683946-3") {
		createIndex(indexName: "FKCC54EB863699C23", tableName: "measured_value") {
			column(name: "connectivity_profile_id")
		}
	}

	changeSet(author: "bwo (generated)", id: "1450175683946-2") {
		addForeignKeyConstraint(baseColumnNames: "connectivity_profile_id", baseTableName: "measured_value", constraintName: "FKCC54EB863699C23", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "connectivity_profile", referencesUniqueColumn: "false")
	}
}
