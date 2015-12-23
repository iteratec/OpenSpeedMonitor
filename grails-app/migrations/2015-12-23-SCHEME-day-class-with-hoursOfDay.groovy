databaseChangeLog = {

	changeSet(author: "bwo (generated)", id: "1450880177365-1") {
		createTable(tableName: "day") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "dayPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1450880177365-2") {
		createTable(tableName: "day_hour_of_day") {
			column(name: "day_hours_of_day_id", type: "bigint")

			column(name: "hour_of_day_id", type: "bigint")
		}
	}

	changeSet(author: "bwo (generated)", id: "1450880177365-5") {
		createIndex(indexName: "FK188AB3CC4EA4F05D", tableName: "day_hour_of_day") {
			column(name: "day_hours_of_day_id")
		}
	}

	changeSet(author: "bwo (generated)", id: "1450880177365-6") {
		createIndex(indexName: "FK188AB3CCA33A543A", tableName: "day_hour_of_day") {
			column(name: "hour_of_day_id")
		}
	}

	changeSet(author: "bwo (generated)", id: "1450880177365-3") {
		addForeignKeyConstraint(baseColumnNames: "day_hours_of_day_id", baseTableName: "day_hour_of_day", constraintName: "FK188AB3CC4EA4F05D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "day", referencesUniqueColumn: "false")
	}

	changeSet(author: "bwo (generated)", id: "1450880177365-4") {
		addForeignKeyConstraint(baseColumnNames: "hour_of_day_id", baseTableName: "day_hour_of_day", constraintName: "FK188AB3CCA33A543A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "hour_of_day", referencesUniqueColumn: "false")
	}
}
