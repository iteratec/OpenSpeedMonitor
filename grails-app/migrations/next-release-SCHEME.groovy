databaseChangeLog = {
    // nightlyDatabaseCleanups can be de-/activated by REST-Call
    changeSet(author: "bka (generated)", id: "1452546683118-1") {
        addColumn(tableName: "api_key") {
            column(name: "allowed_for_nightly_database_cleanup_activation", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-1") {
        createTable(tableName: "csi_system") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "csi_systemPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "label", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-2") {
        createTable(tableName: "csi_system_job_group_weight") {
            column(name: "csi_system_job_group_weights_id", type: "bigint")

            column(name: "job_group_weight_id", type: "bigint")

            column(name: "job_group_weights_idx", type: "integer")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-3") {
        createTable(tableName: "job_group_weight") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "job_group_weiPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "job_group_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "weight", type: "double precision") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-6") {
        createIndex(indexName: "label_uniq_1454506330929", tableName: "csi_system", unique: "true") {
            column(name: "label")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-7") {
        createIndex(indexName: "FK588975446326A28C", tableName: "csi_system_job_group_weight") {
            column(name: "job_group_weight_id")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-8") {
        createIndex(indexName: "FK5044557A48E56BA7", tableName: "job_group_weight") {
            column(name: "job_group_id")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-4") {
        addForeignKeyConstraint(baseColumnNames: "job_group_weight_id", baseTableName: "csi_system_job_group_weight", constraintName: "FK588975446326A28C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group_weight", referencesUniqueColumn: "false")
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-5") {
        addForeignKeyConstraint(baseColumnNames: "job_group_id", baseTableName: "job_group_weight", constraintName: "FK5044557A48E56BA7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group", referencesUniqueColumn: "false")
    }
}