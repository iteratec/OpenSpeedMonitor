databaseChangeLog = {
    changeSet(author: "mmi", id: "1450880177365-1") {
        createTable(tableName: "day") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "dayPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "hour0weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour10weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour11weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour12weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour13weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour14weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour15weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour16weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour17weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour18weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour19weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour1weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour20weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour21weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour22weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour23weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour2weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour3weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour4weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour5weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour6weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour7weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour8weight", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "hour9weight", type: "double precision") {
                constraints(nullable: "false")
            }
        }
    }
}