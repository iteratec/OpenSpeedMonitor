databaseChangeLog = {
    changeSet(author: "sburnicki (generated)", id: "1493813874383-4") {
        dropColumn(columnName: "debug", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "sburnicki (generated)", id: "1493813874383-6") {
        dropColumn(columnName: "from_hour", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "sburnicki (generated)", id: "1493813874383-7") {
        dropColumn(columnName: "selected_all_browsers", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "sburnicki (generated)", id: "1493813874383-8") {
        dropColumn(columnName: "selected_all_connectivity_profiles", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "sburnicki (generated)", id: "1493813874383-9") {
        dropColumn(columnName: "selected_all_locations", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "sburnicki (generated)", id: "1493813874383-10") {
        dropColumn(columnName: "selected_all_measured_events", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "sburnicki (generated)", id: "1493813874383-11") {
        dropColumn(columnName: "set_from_hour", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "sburnicki (generated)", id: "1493813874383-12") {
        dropColumn(columnName: "set_to_hour", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "sburnicki (generated)", id: "1493813874383-14") {
        dropColumn(columnName: "to_hour", tableName: "userspecific_dashboard_base")
    }

    changeSet(author: "sburnicki (generated)", id: "1493813874383-15") {
        dropColumn(columnName: "wide_screen_diagram_montage", tableName: "userspecific_dashboard_base")
    }
}
