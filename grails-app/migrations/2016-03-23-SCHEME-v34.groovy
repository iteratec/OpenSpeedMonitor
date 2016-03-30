databaseChangeLog = {

	changeSet(author: "bwo (generated)", id: "1458732389497-1") {
		addColumn(tableName: "job_group") {
			column(name: "persist_har", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "bwo (generated)", id: "1459322474247-1") {
		addColumn(tableName: "osm_configuration") {
			column(defaultValue: "13", name: "max_har_data_storage_time_in_months", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}
}
