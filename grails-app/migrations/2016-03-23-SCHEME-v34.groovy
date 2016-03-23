databaseChangeLog = {

	changeSet(author: "bwo (generated)", id: "1458732389497-1") {
		addColumn(tableName: "job_group") {
			column(name: "persist_har", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
}
