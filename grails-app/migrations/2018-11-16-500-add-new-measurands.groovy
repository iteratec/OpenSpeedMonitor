databaseChangeLog = {
    changeSet(author: "fwieczorek", id: "1542369419628-23") {
        addColumn(tableName: "event_result") {
            column(name: "js_total", type: "INT")
            column(name: "image_total", type: "INT")
            column(name: "css_total", type: "INT")
            column(name: "html_total", type: "INT")
        }
    }
}