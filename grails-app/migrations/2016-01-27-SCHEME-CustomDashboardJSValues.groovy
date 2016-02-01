databaseChangeLog = {

	changeSet(author: "bwo (generated)", id: "1453899727017-1") {
		addColumn(tableName: "userspecific_event_result_dashboard") {
			column(name: "chart_title", type: "varchar(255)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1453899727017-2") {
		addColumn(tableName: "userspecific_event_result_dashboard") {
			column(name: "chart_width", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1453899727017-3") {
		addColumn(tableName: "userspecific_event_result_dashboard") {
			column(name: "chart_height", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1453899727017-4") {
		addColumn(tableName: "userspecific_event_result_dashboard") {
			column(name: "load_time_maximum", type: "varchar(255)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1453899727017-5") {
		addColumn(tableName: "userspecific_event_result_dashboard") {
			column(name: "load_time_minimum", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1453899727017-6") {
		addColumn(tableName: "userspecific_event_result_dashboard") {
			column(name: "show_data_labels", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1453899727017-7") {
		addColumn(tableName: "userspecific_event_result_dashboard") {
			column(name: "show_data_markers", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1453899727017-8") {
		modifyDataType(columnName: "weight", newDataType: "double precision", tableName: "page")
	}

	changeSet(author: "bwo (generated)", id: "1453899727017-9") {
		dropNotNullConstraint(columnDataType: "double precision", columnName: "weight", tableName: "page")
	}

	changeSet(author: "bwo (generated)", id: "1453899727017-10") {
		dropIndex(indexName: "full_hour", tableName: "hour_of_day")
	}
}
