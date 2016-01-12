databaseChangeLog = {

	changeSet(author: "bka (generated)", id: "1452546683118-1") {
		addColumn(tableName: "api_key") {
			column(name: "allowed_for_nightly_database_cleanup_activation", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
}
