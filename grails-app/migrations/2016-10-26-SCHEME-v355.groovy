databaseChangeLog = {

    changeSet(author: "sburnicki", id: "1477485207405-1") {
        dropColumn(columnName: "initial_chart_width_in_pixels", tableName: "osm_configuration")
    }
    changeSet(author: "marko (generated)", id: "1478707539184-2") {
        dropColumn(columnName: "weight", tableName: "browser")
    }
    changeSet(author: "marko (generated)", id: "1478711739662-2") {
        dropColumn(columnName: "weight", tableName: "page")
    }

    // ### BEGIN Refactoring of tag attribute ###

    changeSet(author: "marcus (generated)", id: "1473397000359-1") {
        addColumn(tableName: "event_result") {
            column(name: "browser_id", type: "bigint")
            column(name: "job_group_id", type: "bigint")
            column(name: "location_id", type: "bigint")
            column(name: "page_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1473079647061-1") {
        addColumn(tableName: "csi_aggregation") {
            column(name: "browser_id", type: "bigint")
            column(name: "job_group_id", type: "bigint")
            column(name: "location_id", type: "bigint")
            column(name: "measured_event_id", type: "bigint")
            column(name: "page_id", type: "bigint")
        }
    }

    // ### END Refactoring of tag attribute ###
}
