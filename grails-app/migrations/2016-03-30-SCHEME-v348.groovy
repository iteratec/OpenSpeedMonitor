databaseChangeLog = {

	changeSet(author: "bka", id: "1459346200213-1") {
		dropForeignKeyConstraint(baseTableName: "http_archive", constraintName: "FK86FC880BF0C41D41")
	}

	changeSet(author: "bka", id: "1459346200213-2") {
		dropTable(tableName: "http_archive")
	}
	changeSet(author: "bka", id: "1459346200213-2") {
		preConditions (onFail: 'MARK_RAN'){
			primaryKeyExists(tableName: 'csi_aggregation', primaryKeyName: 'measured_valuPK')
		}
		dropPrimaryKey(tableName: 'csi_aggregation', primaryKeyName: 'measured_valuPK')
		createPrimaryKey(tableName: 'csi_aggregation', primaryKeyName: 'measured_valuPK1', columnNames: "id")
	}
	changeSet(author: "bka", id: "1459346200213-3") {
		preConditions (onFail: 'MARK_RAN'){
			primaryKeyExists(tableName: 'csi_aggregation_interval', primaryKeyName: 'measured_valuPK')
		}
		dropPrimaryKey(tableName: 'csi_aggregation_interval', primaryKeyName: 'measured_valuPK')
		createPrimaryKey(tableName: 'csi_aggregation_interval', primaryKeyName: 'measured_valuPK2', columnNames: "id")
	}
	changeSet(author: "bka", id: "1459346200213-4") {
		preConditions (onFail: 'MARK_RAN'){
			primaryKeyExists(tableName: 'csi_aggregation_update_event', primaryKeyName: 'measured_valuPK')
		}
		dropPrimaryKey(tableName: 'csi_aggregation_update_event', primaryKeyName: 'measured_valuPK')
		createPrimaryKey(tableName: 'csi_aggregation_update_event', primaryKeyName: 'measured_valuPK3', columnNames: "id")
	}
	changeSet(author: "bka", id: "1459346200213-5") {
		preConditions (onFail: 'MARK_RAN'){
			primaryKeyExists(tableName: 'userspecific_csi_dashboard', primaryKeyName: 'userspecific_PK')
		}
		dropPrimaryKey(tableName: 'userspecific_csi_dashboard', primaryKeyName: 'userspecific_PK')
		createPrimaryKey(tableName: 'userspecific_csi_dashboard', primaryKeyName: 'userspecific_PK1', columnNames: "id")
	}
	changeSet(author: "bka", id: "1459346200213-6") {
		preConditions (onFail: 'MARK_RAN'){
			primaryKeyExists(tableName: 'userspecific_event_result_dashboard', primaryKeyName: 'userspecific_PK')
		}
		dropPrimaryKey(tableName: 'userspecific_event_result_dashboard', primaryKeyName: 'userspecific_PK')
		createPrimaryKey(tableName: 'userspecific_event_result_dashboard', primaryKeyName: 'userspecific_PK2', columnNames: "id")
	}
	changeSet(author: "msk", id: "1459346200213-7") {
		renameTable(oldTableName: "tag_links", newTableName: "tags_links")
	}
}
