databaseChangeLog = {
    changeSet(author: "sburnicki", id: "1477485207405-1") {
        dropColumn(columnName: "initial_chart_width_in_pixels", tableName: "osm_configuration")
    }
}