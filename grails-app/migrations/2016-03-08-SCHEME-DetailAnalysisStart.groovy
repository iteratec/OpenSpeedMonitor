databaseChangeLog = {

	changeSet(author: "bwo", id: "1457452001734-1") {
		addColumn(tableName: "osm_configuration") {
			column(name: "detail_fetching_enabled", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "bwo (generated)", id: "1457452001734-2") {
		addColumn(tableName: "job_result") {
			column(name: "har_status", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}
}
