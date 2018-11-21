databaseChangeLog = {
    changeSet(author: "sbr", id: "1541511234000-1") {
        dropColumn(columnName: "capture_video", tableName: "job_result")
        dropColumn(columnName: "download_result_xml", tableName: "job_result")
        dropColumn(columnName: "download_details", tableName: "job_result")
        dropColumn(columnName: "frequency_in_min", tableName: "job_result")
        dropColumn(columnName: "max_download_attempts", tableName: "job_result")
        dropColumn(columnName: "script_url", tableName: "job_result")
        dropColumn(columnName: "script_navigationscript", tableName: "job_result")
        dropColumn(columnName: "provide_authenticate_information", tableName: "job_result")
        dropColumn(columnName: "auth_username", tableName: "job_result")
        dropColumn(columnName: "auth_password", tableName: "job_result")
        dropColumn(columnName: "multistep", tableName: "job_result")
        dropColumn(columnName: "apply_validate_rule", tableName: "job_result")
        dropColumn(columnName: "validation_request", tableName: "job_result")
        dropColumn(columnName: "validation_type", tableName: "job_result")
        dropColumn(columnName: "validation_markas", tableName: "job_result")
        dropColumn(columnName: "validation_markaselse", tableName: "job_result")
        addColumn(tableName: "job_result") {
            column(name: "expected_steps", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }
}
