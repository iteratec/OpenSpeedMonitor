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

	// START IT-774 remove waterfall
	changeSet(author: "birger (generated)", id: "1456401696309-1") {
		dropForeignKeyConstraint(baseTableName: "event_result", baseTableSchemaName: "osm", constraintName: "FK3CA811623AA7BDEF")
	}

	changeSet(author: "birger (generated)", id: "1456401696309-2") {
		dropForeignKeyConstraint(baseTableName: "web_performance_waterfall_waterfall_entry", baseTableSchemaName: "osm", constraintName: "FK297D11DE5F1B69E4")
	}

	changeSet(author: "birger (generated)", id: "1456401696309-3") {
		dropForeignKeyConstraint(baseTableName: "web_performance_waterfall_waterfall_entry", baseTableSchemaName: "osm", constraintName: "FK297D11DEBD83B85")
	}

	changeSet(author: "birger (generated)", id: "1456401696309-4") {
		dropColumn(columnName: "web_performance_waterfall_id", tableName: "event_result")
	}

	changeSet(author: "birger (generated)", id: "1456401696309-5") {
		dropTable(tableName: "waterfall_entry")
	}

	changeSet(author: "birger (generated)", id: "1456401696309-6") {
		dropTable(tableName: "web_performance_waterfall")
	}

	changeSet(author: "birger (generated)", id: "1456401696309-7") {
		dropTable(tableName: "web_performance_waterfall_waterfall_entry")
	}
	// END IT-774 remove waterfall
}
