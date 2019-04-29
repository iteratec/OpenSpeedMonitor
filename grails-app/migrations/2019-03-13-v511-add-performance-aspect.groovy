
databaseChangeLog = {
    changeSet(author: "mwg (generated)", id: "1552480742878-1") {
        createTable(tableName: "performance_aspect") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "performance_aspectPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cached_view", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "metric_identifier", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "performance_aspect_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "job_group_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "page_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "mwg (generated)", id: "1552480742878-5") {
        addForeignKeyConstraint(baseColumnNames: "job_group_id", baseTableName: "performance_aspect", constraintName: "FK3vofqctebdktl2d3bp73ftiim", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group")
    }

    changeSet(author: "mwg (generated)", id: "1552480742878-6") {
        addForeignKeyConstraint(baseColumnNames: "page_id", baseTableName: "performance_aspect", constraintName: "FKsc46o3c2wp3h1ixk5ixggmvmu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "page")
    }
}