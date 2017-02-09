databaseChangeLog = {

    changeSet(author: "marcus (generated)", id: "1485435173761-1") {
        createTable(tableName: "userspecific_dashboard_base_selected_connectivities") {
            column(name: "userspecific_dashboard_base_id", type: "BIGINT")

            column(name: "selected_connectivities_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "marcus (generated)", id: "1485435173761-2") {
        addForeignKeyConstraint(baseColumnNames: "userspecific_dashboard_base_id", baseTableName: "userspecific_dashboard_base_selected_connectivities", constraintName: "FK_2bk3dba8jur3bdc769isdramn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "userspecific_dashboard_base")
    }

    changeSet(author: "mmi", id: "1485425536551-3") {
        grailsChange {
            change {
                sql.eachRow('''SELECT id, selected_connectivity_profiles 
                            FROM userspecific_dashboard_base 
                                WHERE selected_connectivity_profiles IS NOT NULL 
                                AND selected_connectivity_profiles <> '';''') { result ->
                    def id = result[0]
                    // connectivityProfile used to be stored as comma seperated string
                    def profileIds = result[1].split(",")
                    profileIds.each {
                        sql.executeInsert('''INSERT INTO userspecific_dashboard_base_selected_connectivities VALUES (:id, :profile)''', [id: id, profile: it])
                    }
                }
            }
        }
    }

    changeSet(author: "mmi", id: "1485425536551-4") {
        sql('''INSERT INTO userspecific_dashboard_base_selected_connectivities
                                SELECT id, custom_connectivity_name FROM userspecific_dashboard_base 
                                    WHERE custom_connectivity_name is not null
                                    AND custom_connectivity_name !='' ''')
    }

    changeSet(author: "mmi", id: "1485425536551-5") {
        sql('''INSERT INTO userspecific_dashboard_base_selected_connectivities
                                SELECT id, 'native' FROM userspecific_dashboard_base 
                                    WHERE include_native_connectivity = true''')
    }

    changeSet(author: "mmi (generated)", id: "1485425536551-4") {
        dropColumn(columnName: "selected_connectivity_profiles", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "mmi (generated)", id: "1485425536551-5") {
        dropColumn(columnName: "custom_connectivity_name", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "mmi (generated)", id: "1485425536551-7") {
        dropColumn(columnName: "include_native_connectivity", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "marcus (generated)", id: "1486570637625-3") {
        dropColumn(columnName: "include_custom_connectivity", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "marcus (generated)", id: "1486570637625-1") {
        addColumn(tableName: "job_result") {
            column(name: "wpt_version", type: "varchar(255)")
        }
    }

    changeSet(author: "marcus (generated)", id: "1486570637625-2") {
        dropColumn(columnName: "event_result_id_from_sqlite", tableName: "job_result")
    }

}
