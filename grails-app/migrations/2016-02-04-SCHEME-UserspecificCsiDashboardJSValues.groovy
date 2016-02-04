databaseChangeLog = {

	changeSet(author: "bwo (generated)", id: "1454590842460-1") {
		addColumn(tableName: "userspecific_csi_dashboard") {
			column(name: "chart_height", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1454590842460-2") {
		addColumn(tableName: "userspecific_csi_dashboard") {
			column(name: "chart_title", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1454590842460-3") {
		addColumn(tableName: "userspecific_csi_dashboard") {
			column(name: "chart_width", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1454590842460-4") {
		addColumn(tableName: "userspecific_csi_dashboard") {
			column(name: "load_time_maximum", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1454590842460-5") {
		addColumn(tableName: "userspecific_csi_dashboard") {
			column(name: "load_time_minimum", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1454590842460-6") {
		addColumn(tableName: "userspecific_csi_dashboard") {
			column(name: "show_data_labels", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1454590842460-7") {
		addColumn(tableName: "userspecific_csi_dashboard") {
			column(name: "show_data_markers", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
}
