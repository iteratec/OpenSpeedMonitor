databaseChangeLog = {

	changeSet(author: "bka", id: "1458732389497-2") {
		preConditions (onFail: 'MARK_RAN'){
			primaryKeyExists(tableName: 'csi_aggregation', primaryKeyName: 'measured_valuPK')
		}
		dropPrimaryKey(tableName: 'csi_aggregation', primaryKeyName: 'measured_valuPK')
		createPrimaryKey(tableName: 'csi_aggregation', primaryKeyName: 'measured_valuPK1', columnNames: "id")
	}
	changeSet(author: "bka", id: "1458732389497-3") {
		preConditions (onFail: 'MARK_RAN'){
			primaryKeyExists(tableName: 'csi_aggregation_interval', primaryKeyName: 'measured_valuPK')
		}
		dropPrimaryKey(tableName: 'csi_aggregation_interval', primaryKeyName: 'measured_valuPK')
		createPrimaryKey(tableName: 'csi_aggregation_interval', primaryKeyName: 'measured_valuPK2', columnNames: "id")
	}
	changeSet(author: "bka", id: "1458732389497-4") {
		preConditions (onFail: 'MARK_RAN'){
			primaryKeyExists(tableName: 'csi_aggregation_update_event', primaryKeyName: 'measured_valuPK')
		}
		dropPrimaryKey(tableName: 'csi_aggregation_update_event', primaryKeyName: 'measured_valuPK')
		createPrimaryKey(tableName: 'csi_aggregation_update_event', primaryKeyName: 'measured_valuPK3', columnNames: "id")
	}
	changeSet(author: "bka", id: "1458732389497-5") {
		preConditions (onFail: 'MARK_RAN'){
			primaryKeyExists(tableName: 'userspecific_csi_dashboard', primaryKeyName: 'userspecific_PK')
		}
		dropPrimaryKey(tableName: 'userspecific_csi_dashboard', primaryKeyName: 'userspecific_PK')
		createPrimaryKey(tableName: 'userspecific_csi_dashboard', primaryKeyName: 'userspecific_PK1', columnNames: "id")
	}
	changeSet(author: "bka", id: "1458732389497-6") {
		preConditions (onFail: 'MARK_RAN'){
			primaryKeyExists(tableName: 'userspecific_event_result_dashboard', primaryKeyName: 'userspecific_PK')
		}
		dropPrimaryKey(tableName: 'userspecific_event_result_dashboard', primaryKeyName: 'userspecific_PK')
		createPrimaryKey(tableName: 'userspecific_event_result_dashboard', primaryKeyName: 'userspecific_PK2', columnNames: "id")
	}
}
