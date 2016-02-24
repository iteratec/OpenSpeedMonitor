databaseChangeLog = {

	changeSet(author: "bwo (generated)", id: "1456309666028-1") {
		addColumn(tableName: "userspecific_csi_dashboard") {
			column(name: "csi_type_doc_complete", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "bwo (generated)", id: "1456309666028-2") {
		addColumn(tableName: "userspecific_csi_dashboard") {
			column(name: "csi_type_visually_complete", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}
}
