databaseChangeLog = {
    changeSet(author: "nkuhn (generated)", id: "1507639061332-01") {
        createTable(tableName: "graphite_server_graphite_path_csi_data") {
            column(name: "graphite_server_graphite_paths_csi_data_id", type: "BIGINT")

            column(name: "graphite_path_csi_data_id", type: "BIGINT")
        }
    }

    changeSet(author: "nkuhn (generated)", id: "1507639061332-02") {
        createTable(tableName: "graphite_server_graphite_path_raw_data") {
            column(name: "graphite_server_graphite_paths_raw_data_id", type: "BIGINT")

            column(name: "graphite_path_raw_data_id", type: "BIGINT")
        }
    }
    changeSet(author: "nkuhn (generated)", id: "1507639061332-03") {
        addForeignKeyConstraint(baseColumnNames: "graphite_server_graphite_paths_raw_data_id", baseTableName: "graphite_server_graphite_path_raw_data", constraintName: "FK_6y9ds0o7gjexkqalwh67d0htn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_server")
    }

    changeSet(author: "nkuhn (generated)", id: "1507639061332-04") {
        addForeignKeyConstraint(baseColumnNames: "graphite_server_graphite_paths_csi_data_id", baseTableName: "graphite_server_graphite_path_csi_data", constraintName: "FK_e71u23jcr4u3owsun9o16psg7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_server")
    }

    changeSet(author: "nkuhn (generated)", id: "1507639061332-05") {
        addForeignKeyConstraint(baseColumnNames: "graphite_path_raw_data_id", baseTableName: "graphite_server_graphite_path_raw_data", constraintName: "FK_ib7ao7ww1u7q22gwm1746l3r5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_path_raw_data")
    }
    changeSet(author: "nkuhn (generated)", id: "1507639061332-06") {
        addForeignKeyConstraint(baseColumnNames: "graphite_path_csi_data_id", baseTableName: "graphite_server_graphite_path_csi_data", constraintName: "FK_qxsyd8wtap9i8fiqj09kt2y7q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "graphite_path_csi_data")
    }
    changeSet(author: "nkuhn", id: "1507639061332-07") {
        sql('''insert into graphite_server_graphite_path_raw_data (graphite_server_graphite_paths_raw_data_id,
            graphite_path_raw_data_id) select graphite_server_id,id from graphite_path_raw_data;''')
        sql('''insert into graphite_server_graphite_path_csi_data (graphite_server_graphite_paths_csi_data_id,
            graphite_path_csi_data_id) select graphite_server_id,id from graphite_path_csi_data;''')
    }
    changeSet(author: "nkuhn", id: "1507639061332-08") {
        dropForeignKeyConstraint(baseTableName: "graphite_path_raw_data", constraintName: "FK_fkuu07082lf6j649w0o9d48dq")
        dropForeignKeyConstraint(baseTableName: "graphite_path_csi_data", constraintName: "FK_rq4upfotoftedbqjva8pn276d")
    }
    changeSet(author: "nkuhn", id: "1507639061332-09"){
        dropColumn(columnName: "graphite_server_id", tableName: "graphite_path_raw_data")
        dropColumn(columnName: "graphite_server_id", tableName: "graphite_path_csi_data")
    }
}
