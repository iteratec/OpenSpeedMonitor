databaseChangeLog = {

	changeSet(author: "bwo (generated)", id: "1454675900207-1") {
		addColumn(tableName: "osm_configuration") {
			column(defaultValue: "1000", name: "initial_chart_width_in_pixels", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}
}
