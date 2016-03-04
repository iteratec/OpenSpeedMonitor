databaseChangeLog = {

	changeSet(author: "marcus (generated)", id: "1457079344288-1") {
		addColumn(tableName: "userspecific_csi_dashboard") {
			column(name: "selected_csi_systems", type: "varchar(255)")
		}
	}
}
