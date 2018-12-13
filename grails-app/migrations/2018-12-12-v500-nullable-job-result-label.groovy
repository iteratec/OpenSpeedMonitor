databaseChangeLog = {
    changeSet(author: "bwo", id: "20181212-1") {
        dropNotNullConstraint(columnName: 'job_config_label', tableName: 'job_result', columnDataType: 'varchar(255)')
    }
}