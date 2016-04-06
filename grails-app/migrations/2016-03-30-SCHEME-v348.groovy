databaseChangeLog = {

	changeSet(author: "bka", id: "1459346200213-1") {
		dropForeignKeyConstraint(baseTableName: "http_archive", constraintName: "FK86FC880BF0C41D41")
	}

	changeSet(author: "bka", id: "1459346200213-2") {
		dropTable(tableName: "http_archive")
	}
}
