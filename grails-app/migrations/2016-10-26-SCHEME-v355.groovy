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
}